package litun.uxinnovator.data.api

import litun.uxinnovator.data.model.CreateUserRequest
import litun.uxinnovator.data.model.UserDto
import litun.uxinnovator.data.model.UsersPage

interface UserApiService {
    suspend fun getUsers(page: Int, perPage: Int = 10): UsersPage
    suspend fun createUser(request: CreateUserRequest): UserDto
    suspend fun deleteUser(id: Long)
}
