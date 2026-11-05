package com.essaid.user.domain.model

data class PersonalPatientInfo(
    val id: Long? = null,
    val email: String,
    val nombre: String,
    val apellido: String,
    val tipoDocumento: String,
    val numeroDocumento: String,
    val celular: String,
    val fechaNacimiento: String,
    val genero: String? = null,
)

data class MedicalPatientInfo(
    val tipoSangre: String? = null,
    val alergias: List<String> = emptyList(),
    val condicionesCronicas: List<String> = emptyList(),
    val medicamentosActuales: List<String> = emptyList(),
    val contactoEmergenciaNombre: String? = null,
    val contactoEmergenciaTelefono: String? = null,
    val observacionesMedicas: String? = null,
    val ultimaActualizacion: String? = null,
)

data class PatientProfile(
    val personal: PersonalPatientInfo,
    val medical: MedicalPatientInfo,
)

sealed interface PatientProfileOutcome {
    data class Success(
        val profile: PatientProfile,
        val message: String = "Información personal y médica cargada exitosamente.",
    ) : PatientProfileOutcome

    data class Empty(
        val message: String = "No se encontró información personal o médica registrada.",
    ) : PatientProfileOutcome

    data class Unauthorized(
        val message: String = "Sesión expirada o no autorizada.",
    ) : PatientProfileOutcome

    data class ValidationError(
        val message: String = "Solicitud inválida.",
    ) : PatientProfileOutcome

    data class NetworkError(
        val message: String = "No se pudo conectar con EssAid. Revisa tu conexión.",
    ) : PatientProfileOutcome

    data class ServerError(
        val message: String = "Ocurrió un error en el servidor al consultar la información.",
        val statusCode: Int? = null,
    ) : PatientProfileOutcome
}

data class PatientProfileUpdateForm(
    val celular: String = "",
    val contactoEmergenciaNombre: String = "",
    val contactoEmergenciaTelefono: String = "",
    val alergias: String = "",
    val condicionesCronicas: String = "",
    val medicamentosActuales: String = "",
    val observacionesMedicas: String = "",
) {
    fun toMedicalList(text: String): List<String> =
        text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

sealed interface PatientProfileUpdateOutcome {
    data class Success(
        val profile: PatientProfile,
        val message: String = "Información actualizada exitosamente.",
    ) : PatientProfileUpdateOutcome

    data class ValidationError(
        val message: String = "Errores de validación en los campos editables.",
        val fieldErrors: Map<String, String> = emptyMap(),
    ) : PatientProfileUpdateOutcome

    data class Unauthorized(
        val message: String = "Sesión expirada o no autorizada.",
    ) : PatientProfileUpdateOutcome

    data class NetworkError(
        val message: String = "No se pudo conectar con el servidor. Revisa tu conexión.",
    ) : PatientProfileUpdateOutcome

    data class ServerError(
        val message: String = "Ocurrió un error en el servidor al actualizar la información.",
        val statusCode: Int? = null,
    ) : PatientProfileUpdateOutcome
}
