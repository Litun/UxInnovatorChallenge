package litun.uxinnovator.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import litun.uxinnovator.data.api.UserApiService
import litun.uxinnovator.data.db.AppDatabase
import litun.uxinnovator.data.db.CachedUser
import litun.uxinnovator.data.model.CreateUserRequest
import litun.uxinnovator.data.model.UserDto
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus
import litun.uxinnovator.domain.repository.UserRepository

class UserRepositoryImpl(
    private val apiService: UserApiService,
    private val database: AppDatabase,
) : UserRepository {

    override fun observeUsers(): Flow<List<User>> =
        database.userQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override suspend fun refreshUsers() {
        val firstPage = apiService.getUsers(page = 1, perPage = 100)
        val lastPage = if (firstPage.totalPages > 1) {
            apiService.getUsers(page = firstPage.totalPages, perPage = 100)
        } else {
            firstPage
        }
        database.userQueries.transaction {
            lastPage.users.forEach { upsertUser(it) }
        }
    }

    override suspend fun createUser(
        name: String,
        email: String,
        gender: Gender,
        status: UserStatus
    ): User {
        val dto = apiService.createUser(
            CreateUserRequest(name = name, email = email, gender = gender, status = status)
        )
        upsertUser(dto)
        return dto.toDomain()
    }

    override suspend fun deleteUser(id: Long) {
        apiService.deleteUser(id)
        database.userQueries.deleteById(id)
    }

    private fun upsertUser(dto: UserDto) {
        database.userQueries.upsert(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            gender = dto.gender.name.lowercase(),
            status = dto.status.name.lowercase(),
        )
    }

    private fun UserDto.toDomain() = User(
        id = id,
        name = name,
        email = email,
        gender = gender,
        status = status,
    )

    private fun CachedUser.toDomain() = User(
        id = id,
        name = name,
        email = email,
        gender = Gender.valueOf(gender.uppercase()),
        status = UserStatus.valueOf(status.uppercase()),
    )
}
