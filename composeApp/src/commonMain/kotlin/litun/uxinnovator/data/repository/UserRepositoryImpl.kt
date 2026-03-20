package litun.uxinnovator.data.repository

import litun.uxinnovator.data.api.GoRestApiService
import litun.uxinnovator.data.model.CreateUserRequest
import litun.uxinnovator.data.model.UserDto
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus
import litun.uxinnovator.domain.repository.UserRepository

class UserRepositoryImpl(
    private val apiService: GoRestApiService,
) : UserRepository {

    override suspend fun getLastPageUsers(): List<User> {
        val firstPage = apiService.getUsers(page = 1, perPage = 100)
        val lastPage = if (firstPage.totalPages > 1) {
            apiService.getUsers(page = firstPage.totalPages, perPage = 100)
        } else {
            firstPage
        }
        return lastPage.users.map { it.toDomain() }
    }

    override suspend fun createUser(name: String, email: String, gender: Gender, status: UserStatus): User {
        return apiService.createUser(
            CreateUserRequest(name = name, email = email, gender = gender, status = status)
        ).toDomain()
    }

    override suspend fun deleteUser(id: Long) {
        apiService.deleteUser(id)
    }

    private fun UserDto.toDomain() = User(
        id = id,
        name = name,
        email = email,
        gender = gender,
        status = status,
    )
}
