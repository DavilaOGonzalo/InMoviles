package com.essaid.user.domain.model

data class RegistrationForm(
    val email: String = "",
    val password1: String = "",
    val password2: String = "",
    val tipoDocumento: String = "",
    val numeroDocumento: String = "",
    val caracterVerificador: String = "",
    val fechaNacimiento: String = "",
    val celular: String = "",
    val terminosAceptados: Boolean = false,
    val tratamientoDatosAutorizado: Boolean = false,
) {
    fun toRequest(): UserRegistrationRequest = UserRegistrationRequest(
        email = email.trim(),
        password1 = password1,
        password2 = password2,
        tipoDocumento = tipoDocumento,
        numeroDocumento = numeroDocumento.trim(),
        caracterVerificador = caracterVerificador.trim(),
        fechaNacimiento = fechaNacimiento.trim(),
        celular = celular.trim(),
        terminosAceptados = terminosAceptados,
        tratamientoDatosAutorizado = tratamientoDatosAutorizado,
    )
}

/** Request field names mirror the public USER registration contract. */
data class UserRegistrationRequest(
    val email: String,
    val password1: String,
    val password2: String,
    val tipoDocumento: String,
    val numeroDocumento: String,
    val caracterVerificador: String,
    val fechaNacimiento: String,
    val celular: String,
    val terminosAceptados: Boolean,
    val tratamientoDatosAutorizado: Boolean,
)

data class RegisteredUser(
    val id: Long? = null,
    val email: String,
)

enum class RegistrationField(val wireName: String) {
    Email("email"),
    Password1("password1"),
    Password2("password2"),
    TipoDocumento("tipo_documento"),
    NumeroDocumento("numero_documento"),
    CaracterVerificador("caracter_verificador"),
    FechaNacimiento("fecha_nacimiento"),
    Celular("celular"),
    TerminosAceptados("terminos_aceptados"),
    TratamientoDatosAutorizado("tratamiento_datos_autorizado"),
    General("__all__"),
    ;

    companion object {
        fun fromWireName(name: String): RegistrationField =
            entries.firstOrNull { it.wireName == name } ?: General
    }
}

sealed interface RegistrationOutcome {
    data class Success(
        val message: String = "Tu cuenta se registró correctamente.",
        val user: RegisteredUser? = null,
    ) : RegistrationOutcome

    data class ValidationError(
        val errors: Map<String, List<String>>,
        val detail: String = "Revisa los datos enviados.",
    ) : RegistrationOutcome

    data class Duplicate(
        val errors: Map<String, List<String>>,
        val detail: String = "Ya existe una cuenta o documento con esos datos.",
    ) : RegistrationOutcome

    data class NetworkError(
        val message: String = "No se pudo conectar con EssAid. Revisa tu conexión e inténtalo de nuevo.",
    ) : RegistrationOutcome

    data class ServerError(
        val message: String = "EssAid no pudo completar el registro. Inténtalo nuevamente.",
        val statusCode: Int? = null,
    ) : RegistrationOutcome
}
