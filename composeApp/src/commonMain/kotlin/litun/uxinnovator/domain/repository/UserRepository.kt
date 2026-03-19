package litun.uxinnovator.domain.repository

import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.model.UserStatus

interface UserRepository {
    suspend fun getLastPageUsers(): List<User>
    suspend fun createUser(name: String, email: String, gender: Gender = Gender.MALE, status: UserStatus = UserStatus.ACTIVE): User
    suspend fun deleteUser(id: Long)
}
