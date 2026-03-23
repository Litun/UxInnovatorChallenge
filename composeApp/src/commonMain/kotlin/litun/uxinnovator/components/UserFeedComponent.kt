package litun.uxinnovator.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import litun.uxinnovator.domain.model.User
import litun.uxinnovator.domain.repository.UserRepository

data class UserFeedState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class UserFeedComponent(
    componentContext: ComponentContext,
    private val repository: UserRepository,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ComponentContext by componentContext {

    @Serializable
    sealed class ModalConfig {
        @Serializable
        data object AddUser : ModalConfig()
    }

    sealed class ModalChild {
        class AddUser(val component: AddUserComponent) : ModalChild()
    }

    // Automatically cancelled when the component is destroyed (Essenty lifecycle-coroutines)
    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val _state = MutableValue(UserFeedState(isLoading = true))
    val state: Value<UserFeedState> = _state

    private val slotNavigation = SlotNavigation<ModalConfig>()

    val modalSlot: Value<ChildSlot<*, ModalChild>> = childSlot(
        source = slotNavigation,
        serializer = ModalConfig.serializer(),
        handleBackButton = true,
        childFactory = { _, ctx ->
            ModalChild.AddUser(
                AddUserComponent(
                    componentContext = ctx,
                    repository = repository,
                    onUserCreated = { slotNavigation.dismiss() },
                    onDismiss = { slotNavigation.dismiss() },
                )
            )
        },
    )

    init {
        scope.launch {
            repository.observeUsers().collect { users ->
                _state.value = _state.value.copy(users = users)
            }
        }
        loadUsers()
    }

    fun refresh() = loadUsers()

    fun openAddUser() = slotNavigation.activate(ModalConfig.AddUser)

    fun onDeleteUser(id: Long) {
        scope.launch {
            try {
                repository.deleteUser(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Unknown error")
            }
        }
    }

    private fun loadUsers() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                repository.refreshUsers()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Unknown error")
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
