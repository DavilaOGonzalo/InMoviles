package com.essaid.user.domain.model

data class PatientSearchResult(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val dni: String?,
    val fechaNacimiento: String?,
    val sexo: String?,
    val tipoSangre: String?,
    val alergias: List<String>,
    val condicionesCronicas: List<String>,
    val medicamentosActuales: List<String>,
    val contactoEmergencia: String?,
    val telefonoEmergencia: String?,
    val observacionesMedicas: String?,
    val informacionMedicaActualizada: String?,
)

sealed interface PatientSearchOutcome {
    data class Success(val patients: List<PatientSearchResult>) : PatientSearchOutcome
    data object Unauthorized : PatientSearchOutcome
    data object Forbidden : PatientSearchOutcome
    data object NetworkError : PatientSearchOutcome
    data class ServerError(val statusCode: Int? = null) : PatientSearchOutcome
}
