package litun.uxinnovator.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import litun.uxinnovator.data.model.CreateUserRequest
import litun.uxinnovator.data.model.UserDto
import litun.uxinnovator.data.model.UsersPage
import litun.uxinnovator.domain.model.GoRestException
import litun.uxinnovator.domain.model.ValidationFieldError

class GoRestApiService(
    private val token: String,
    private val baseUrl: String = "https://gorest.co.in/public/v2",
) : UserApiService {
    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = false
    }

    override suspend fun getUsers(page: Int, perPage: Int): UsersPage {
        val response = client.get("$baseUrl/users") {
            parameter("page", page)
            parameter("per_page", perPage)
        }
        response.throwIfError()
        return UsersPage(
            users = response.body(),
            currentPage = response.headers["x-pagination-page"]?.toInt() ?: page,
            totalPages = response.headers["x-pagination-pages"]?.toInt() ?: 1,
            totalCount = response.headers["x-pagination-total"]?.toInt() ?: 0,
        )
    }

    override suspend fun createUser(request: CreateUserRequest): UserDto {
        val response = client.post("$baseUrl/users") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        response.throwIfError()
        return response.body()
    }

    override suspend fun deleteUser(id: Long) {
        val response = client.delete("$baseUrl/users/$id") {
            bearerAuth(token)
        }
        response.throwIfError()
    }

    fun close() = client.close()

    private suspend fun HttpResponse.throwIfError() {
        if (status.isSuccess()) return
        val body = bodyAsText()
        throw when (status.value) {
            401 -> GoRestException.AuthError(parseMessageBody(body))
            404 -> GoRestException.NotFound(parseMessageBody(body))
            422 -> GoRestException.ValidationError(
                json.decodeFromString<List<ApiErrorItem>>(body)
                    .map { ValidationFieldError(it.field, it.message) }
            )

            else -> GoRestException.UnknownError(status.value, body)
        }
    }

    private fun parseMessageBody(body: String): String =
        runCatching { json.decodeFromString<ApiMessageError>(body).message }.getOrDefault(body)

    @Serializable
    private data class ApiErrorItem(val field: String, val message: String)

    @Serializable
    private data class ApiMessageError(val message: String)
}
