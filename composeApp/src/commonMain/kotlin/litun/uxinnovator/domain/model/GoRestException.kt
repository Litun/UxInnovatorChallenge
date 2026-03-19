package litun.uxinnovator.domain.model

sealed class GoRestException(message: String) : Exception(message) {
    class AuthError(message: String) : GoRestException(message)
    class NotFound(message: String) : GoRestException(message)
    class ValidationError(val errors: List<ValidationFieldError>) : GoRestException(
        "Validation failed: ${errors.joinToString { "${it.field} ${it.message}" }}"
    )
    class UnknownError(val statusCode: Int, message: String) : GoRestException("HTTP $statusCode: $message")
}

data class ValidationFieldError(val field: String, val message: String)
