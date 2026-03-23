package litun.uxinnovator.domain.repository

import kotlinx.coroutines.flow.Flow
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus

interface UserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun refreshUsers()
    suspend fun createUser(
        name: String,
        email: String,
        gender: Gender = Gender.MALE,
        status: UserStatus = UserStatus.ACTIVE
    ): User

    suspend fun deleteUser(id: Long)
}
