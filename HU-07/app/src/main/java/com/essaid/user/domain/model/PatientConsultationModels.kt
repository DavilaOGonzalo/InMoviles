package com.essaid.user.domain.model

data class ConsultationDoctor(
    val id: Long,
    val nombre: String,
    val apellido: String,
) {
    val fullName: String
        get() = listOf(nombre, apellido).filter(String::isNotBlank).joinToString(" ").ifBlank { "Médico" }
}

data class ConsultationEntry(
    val id: Long,
    val paciente: Long,
    val medico: ConsultationDoctor?,
    val fechaAtencion: String,
    val motivoConsulta: String,
    val presionArterial: String?,
    val frecuenciaCardiaca: Int?,
    val temperatura: Double?,
    val saturacionO2: Int?,
    val pesoKg: Double?,
    val tallaCm: Double?,
    val diagnostico: String,
    val codigoCie10: String,
    val tratamiento: String,
    val notas: String?,
    val creadoEn: String?,
)

data class PatientConsultations(
    val count: Int,
    val next: String?,
    val previous: String?,
    val entries: List<ConsultationEntry>,
) {
    val isEmpty: Boolean
        get() = entries.isEmpty()

    companion object {
        fun empty(): PatientConsultations = PatientConsultations(0, null, null, emptyList())
    }
}

sealed interface PatientConsultationsOutcome {
    data class Success(val consultations: PatientConsultations) : PatientConsultationsOutcome
    data object Unauthorized : PatientConsultationsOutcome
    data object Forbidden : PatientConsultationsOutcome
    data object NotFound : PatientConsultationsOutcome
    data object NetworkError : PatientConsultationsOutcome
    data class ServerError(val statusCode: Int? = null) : PatientConsultationsOutcome
}
