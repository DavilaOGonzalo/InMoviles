package com.essaid.user.domain.model

data class LoginForm(
    val email: String = "",
    val password: String = "",
) {
    fun toRequest(): UserLoginRequest = UserLoginRequest(
        email = email.trim(),
        password = password,
    )
}

data class UserLoginRequest(
    val email: String,
    val password: String,
)

data class AuthenticatedUser(
    val id: Long? = null,
    val email: String,
    val name: String? = null,
    val role: String? = null,
)

data class AuthSession(
    val token: String,
    val user: AuthenticatedUser,
)

enum class LoginField(val wireName: String) {
    Email("email"),
    Password("password"),
    General("__all__"),
    ;

    companion object {
        fun fromWireName(name: String): LoginField =
            entries.firstOrNull { it.wireName == name } ?: General
    }
}

sealed interface LoginOutcome {
    data class Success(
        val session: AuthSession,
        val message: String = "Inicio de sesión exitoso.",
    ) : LoginOutcome

    data class InvalidCredentials(
        val message: String = "Correo o contraseña incorrectos.",
    ) : LoginOutcome

    data class InactiveAccount(
        val message: String = "Tu cuenta se encuentra inactiva. Contacta al administrador.",
    ) : LoginOutcome

    data class ValidationError(
        val errors: Map<String, List<String>>,
        val detail: String = "Revisa los datos ingresados.",
    ) : LoginOutcome

    data class NetworkError(
        val message: String = "No se pudo conectar con EssAid. Revisa tu conexión.",
    ) : LoginOutcome

    data class ServerError(
        val message: String = "Ocurrió un error en el servidor. Inténtalo más tarde.",
        val statusCode: Int? = null,
    ) : LoginOutcome
}
