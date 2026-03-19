package litun.uxinnovator.data.model

import kotlinx.serialization.Serializable
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.UserStatus

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val gender: Gender,
    val status: UserStatus,
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val gender: Gender,
    val status: UserStatus,
)

data class UsersPage(
    val users: List<UserDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalCount: Int,
)
