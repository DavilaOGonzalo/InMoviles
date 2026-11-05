package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientConsultationDataSource
import com.essaid.user.domain.model.PatientConsultationsOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface PatientConsultationRepository {
    suspend fun getConsultations(
        token: String,
        patientId: Long,
        desde: String? = null,
        hasta: String? = null,
        page: Int? = null,
    ): PatientConsultationsOutcome
}

class DefaultPatientConsultationRepository(
    private val dataSource: PatientConsultationDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PatientConsultationRepository {
    override suspend fun getConsultations(
        token: String,
        patientId: Long,
        desde: String?,
        hasta: String?,
        page: Int?,
    ): PatientConsultationsOutcome = withContext(ioDispatcher) {
        try {
            dataSource.getConsultations(token, patientId, desde, hasta, page)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            PatientConsultationsOutcome.NetworkError
        } catch (_: Exception) {
            PatientConsultationsOutcome.ServerError()
        }
    }
}
