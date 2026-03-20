package litun.uxinnovator.domain.usecase

import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.repository.UserRepository

class GetUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): List<User> = repository.getLastPageUsers()
}
