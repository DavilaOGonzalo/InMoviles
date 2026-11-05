package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientAtencionesDataSource
import com.essaid.user.domain.model.CreateAtencionRequest
import com.essaid.user.domain.model.PatientAtencionesOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface PatientAtencionesRepository {
    suspend fun getAtenciones(
        token: String,
        patientId: Long,
        desde: String? = null,
        hasta: String? = null,
        page: Int = 1,
    ): PatientAtencionesOutcome

    suspend fun createAtencion(
        token: String,
        patientId: Long,
        request: CreateAtencionRequest,
    ): PatientAtencionesOutcome
}

class DefaultPatientAtencionesRepository(
    private val dataSource: PatientAtencionesDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PatientAtencionesRepository {
    override suspend fun getAtenciones(
        token: String,
        patientId: Long,
        desde: String?,
        hasta: String?,
        page: Int,
    ): PatientAtencionesOutcome = withContext(ioDispatcher) {
        try {
            dataSource.getAtenciones(token, patientId, desde, hasta, page)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            PatientAtencionesOutcome.NetworkError
        } catch (_: Exception) {
            PatientAtencionesOutcome.ServerError()
        }
    }

    override suspend fun createAtencion(
        token: String,
        patientId: Long,
        request: CreateAtencionRequest,
    ): PatientAtencionesOutcome = withContext(ioDispatcher) {
        try {
            dataSource.createAtencion(token, patientId, request)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            PatientAtencionesOutcome.NetworkError
        } catch (_: Exception) {
            PatientAtencionesOutcome.ServerError()
        }
    }
}
