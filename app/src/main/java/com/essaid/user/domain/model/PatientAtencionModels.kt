package com.essaid.user.domain.model

data class AttendingDoctor(
    val id: Long,
    val nombre: String,
    val apellido: String,
) {
    val fullName: String
        get() = listOf(nombre, apellido).filter(String::isNotBlank).joinToString(" ")
}

data class PatientAtencion(
    val id: Long,
    val patientId: Long,
    val medico: AttendingDoctor?,
    val fechaAtencion: String,
    val motivoConsulta: String,
    val presionArterial: String?,
    val frecuenciaCardiaca: Int?,
    val temperatura: String?,
    val saturacionO2: Int?,
    val pesoKg: String?,
    val tallaCm: String?,
    val diagnostico: String,
    val codigoCie10: String?,
    val tratamiento: String,
    val notas: String?,
    val creadoEn: String?,
)

data class PatientAtencionesResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PatientAtencion>,
) {
    val isEmpty: Boolean
        get() = results.isEmpty()
}

data class CreateAtencionRequest(
    val motivoConsulta: String,
    val diagnostico: String,
    val tratamiento: String,
    val presionArterial: String? = null,
    val frecuenciaCardiaca: Int? = null,
    val temperatura: String? = null,
    val saturacionO2: Int? = null,
    val pesoKg: String? = null,
    val tallaCm: String? = null,
    val codigoCie10: String? = null,
    val notas: String? = null,
    val fechaAtencion: String? = null,
)

sealed interface PatientAtencionesOutcome {
    data class Success(val response: PatientAtencionesResponse) : PatientAtencionesOutcome
    data class CreateSuccess(val atencion: PatientAtencion) : PatientAtencionesOutcome
    data object Unauthorized : PatientAtencionesOutcome
    data object Forbidden : PatientAtencionesOutcome
    data object NotFound : PatientAtencionesOutcome
    data class ValidationError(val fieldErrors: Map<String, String>) : PatientAtencionesOutcome
    data object NetworkError : PatientAtencionesOutcome
    data class ServerError(val statusCode: Int? = null) : PatientAtencionesOutcome
}
