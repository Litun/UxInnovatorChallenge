package litun.uxinnovator.ui.feed

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.usecase.GetUsersUseCase

data class UserFeedState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class UserFeedComponent(
    componentContext: ComponentContext,
    private val getUsersUseCase: GetUsersUseCase,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ComponentContext by componentContext {

    // Automatically cancelled when the component is destroyed (Essenty lifecycle-coroutines)
    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val _state = MutableValue(UserFeedState(isLoading = true))
    val state: Value<UserFeedState> = _state

    private var loadJob: Job? = null

    init {
        loadUsers()
    }

    fun refresh() = loadUsers()

    private fun loadUsers() {
        loadJob?.cancel()
        loadJob = scope.launch {
            _state.value = UserFeedState(isLoading = true)
            try {
                val users = getUsersUseCase()
                _state.value = UserFeedState(users = users)
            } catch (e: Exception) {
                _state.value = UserFeedState(error = e.message ?: "Unknown error")
            }
        }
    }
}
