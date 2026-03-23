package litun.uxinnovator.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import litun.uxinnovator.domain.model.Gender
import litun.uxinnovator.domain.model.GoRestException
import litun.uxinnovator.domain.model.UserStatus
import litun.uxinnovator.domain.repository.UserRepository

data class AddUserState(
    val name: String = "",
    val email: String = "",
    val gender: Gender = Gender.MALE,
    val status: UserStatus = UserStatus.ACTIVE,
    val nameError: String? = null,
    val emailError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
)

class AddUserComponent(
    componentContext: ComponentContext,
    private val repository: UserRepository,
    private val onUserCreated: () -> Unit,
    val onDismiss: () -> Unit,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ComponentContext by componentContext {

    private val scope = coroutineScope(mainDispatcher + SupervisorJob())

    private val _state = MutableValue(AddUserState())
    val state: Value<AddUserState> = _state

    fun onNameChanged(name: String) {
        _state.value = _state.value.copy(name = name, nameError = validateName(name))
    }

    fun onEmailChanged(email: String) {
        _state.value = _state.value.copy(email = email, emailError = validateEmail(email))
    }

    fun onGenderChanged(gender: Gender) {
        _state.value = _state.value.copy(gender = gender)
    }

    fun onStatusChanged(status: UserStatus) {
        _state.value = _state.value.copy(status = status)
    }

    fun onSubmit() {
        val current = _state.value
        val nameError = validateName(current.name)
        val emailError = validateEmail(current.email)
        if (nameError != null || emailError != null) {
            _state.value = current.copy(nameError = nameError, emailError = emailError)
            return
        }
        scope.launch {
            _state.value = _state.value.copy(isSubmitting = true, submitError = null)
            try {
                repository.createUser(
                    name = current.name.trim(),
                    email = current.email.trim(),
                    gender = current.gender,
                    status = current.status,
                )
                onUserCreated()
            } catch (e: GoRestException.ValidationError) {
                val byField = e.errors.groupBy { it.field }
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    nameError = byField["name"]?.firstOrNull()?.message,
                    emailError = byField["email"]?.firstOrNull()?.message,
                    submitError = if (byField.keys.any { it != "name" && it != "email" }) "Validation failed" else null,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    submitError = e.message ?: "Unknown error",
                )
            }
        }
    }

    private fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> if (name.isEmpty()) null else "Name is required"
            trimmed.length < 2 -> "Name must be at least 2 characters"
            trimmed.length > 100 -> "Name must be 100 characters or fewer"
            !trimmed.matches(NAME_REGEX) -> "Name contains invalid characters"
            else -> null
        }
    }

    private fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isBlank() -> if (email.isEmpty()) null else "Email is required"
            !trimmed.matches(EMAIL_REGEX) -> "Enter a valid email address"
            else -> null
        }
    }

    companion object {
        private val NAME_REGEX = Regex("""^[a-zA-Z][a-zA-Z\s\-'.]*$""")
        private val EMAIL_REGEX = Regex(
            """^[a-zA-Z0-9.!#${'$'}%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\.[a-zA-Z]{2,}${'$'}"""
        )
    }
}
