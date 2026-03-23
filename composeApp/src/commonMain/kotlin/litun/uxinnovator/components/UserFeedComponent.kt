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
    val pendingDeletes: Set<Long> = emptySet(),
    val snackbarUser: User? = null,
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

        @Serializable
        data class DeleteConfirmation(val userId: Long, val userName: String) : ModalConfig()
    }

    sealed class ModalChild {
        class AddUser(val component: AddUserComponent) : ModalChild()
        class DeleteConfirmation(
            val userName: String,
            val onConfirm: () -> Unit,
            val onDismiss: () -> Unit,
        ) : ModalChild()
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
        childFactory = { config, ctx ->
            when (config) {
                is ModalConfig.AddUser -> ModalChild.AddUser(
                    AddUserComponent(
                        componentContext = ctx,
                        repository = repository,
                        onUserCreated = { slotNavigation.dismiss() },
                        onDismiss = { slotNavigation.dismiss() },
                    )
                )

                is ModalConfig.DeleteConfirmation -> ModalChild.DeleteConfirmation(
                    userName = config.userName,
                    onConfirm = {
                        val user = _state.value.users.find { it.id == config.userId }
                            ?: return@DeleteConfirmation
                        onConfirmDelete(user)
                    },
                    onDismiss = { slotNavigation.dismiss() },
                )
            }
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

    fun onLongPressUser(user: User) =
        slotNavigation.activate(ModalConfig.DeleteConfirmation(user.id, user.name))

    fun onConfirmDelete(user: User) {
        slotNavigation.dismiss()
        _state.value = _state.value.copy(
            pendingDeletes = _state.value.pendingDeletes + user.id,
            snackbarUser = user,
        )
    }

    fun onUndoDelete() {
        val user = _state.value.snackbarUser ?: return
        _state.value = _state.value.copy(
            pendingDeletes = _state.value.pendingDeletes - user.id,
            snackbarUser = null,
        )
    }

    fun onFinalizeDelete(userId: Long) {
        _state.value = _state.value.copy(snackbarUser = null)
        scope.launch {
            try {
                repository.deleteUser(userId)
                _state.value =
                    _state.value.copy(pendingDeletes = _state.value.pendingDeletes - userId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    pendingDeletes = _state.value.pendingDeletes - userId,
                    error = e.message ?: "Failed to delete user",
                )
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
