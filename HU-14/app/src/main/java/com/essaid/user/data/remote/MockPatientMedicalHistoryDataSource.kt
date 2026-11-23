package com.essaid.user.data.remote

import com.essaid.user.domain.model.PatientMedicalHistory
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome

class MockPatientMedicalHistoryDataSource : PatientMedicalHistoryDataSource {
    override suspend fun getHistory(token: String, patientId: Long): PatientMedicalHistoryOutcome =
        when {
            token.isBlank() || token == "invalid-token" -> PatientMedicalHistoryOutcome.Unauthorized
            token == "forbidden-token" -> PatientMedicalHistoryOutcome.Forbidden
            else -> PatientMedicalHistoryOutcome.Success(PatientMedicalHistory.empty())
        }
}
