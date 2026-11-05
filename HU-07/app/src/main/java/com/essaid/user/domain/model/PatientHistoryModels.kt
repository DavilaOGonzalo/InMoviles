package com.essaid.user.domain.model

enum class MedicalHistoryCategory(
    val wireValue: String,
    val displayName: String,
) {
    Allergy("alergia", "Alergias"),
    Condition("patologia", "Patologías"),
    Surgery("cirugia", "Cirugías"),
    Family("familiar", "Antecedentes familiares"),
    Medication("medicamento", "Medicamentos"),
    ;

    companion object {
        fun fromWireValue(value: String): MedicalHistoryCategory =
            entries.firstOrNull { it.wireValue == value }
                ?: throw IllegalArgumentException("Categoría de antecedente desconocida: $value")
    }
}

data class MedicalHistoryEntry(
    val id: Long,
    val description: String,
    val approximateDate: String?,
    val active: Boolean,
    val recordedBy: String?,
    val createdAt: String?,
)

data class PatientMedicalHistory(
    val entriesByCategory: Map<MedicalHistoryCategory, List<MedicalHistoryEntry>>,
) {
    val isEmpty: Boolean
        get() = entriesByCategory.values.all(List<MedicalHistoryEntry>::isEmpty)

    companion object {
        fun empty(): PatientMedicalHistory =
            PatientMedicalHistory(MedicalHistoryCategory.entries.associateWith { emptyList() })
    }
}

sealed interface PatientMedicalHistoryOutcome {
    data class Success(val history: PatientMedicalHistory) : PatientMedicalHistoryOutcome
    data object Unauthorized : PatientMedicalHistoryOutcome
    data object Forbidden : PatientMedicalHistoryOutcome
    data object NotFound : PatientMedicalHistoryOutcome
    data object NetworkError : PatientMedicalHistoryOutcome
    data class ServerError(val statusCode: Int? = null) : PatientMedicalHistoryOutcome
}
