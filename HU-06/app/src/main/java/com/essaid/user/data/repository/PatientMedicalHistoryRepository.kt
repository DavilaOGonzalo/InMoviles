package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientMedicalHistoryDataSource
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface PatientMedicalHistoryRepository {
    suspend fun getHistory(token: String, patientId: Long): PatientMedicalHistoryOutcome
}

class DefaultPatientMedicalHistoryRepository(
    private val dataSource: PatientMedicalHistoryDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PatientMedicalHistoryRepository {
    override suspend fun getHistory(
        token: String,
        patientId: Long,
    ): PatientMedicalHistoryOutcome = withContext(ioDispatcher) {
        try {
            dataSource.getHistory(token, patientId)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            PatientMedicalHistoryOutcome.NetworkError
        } catch (_: Exception) {
            PatientMedicalHistoryOutcome.ServerError()
        }
    }
}
