package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientSearchDataSource
import com.essaid.user.domain.model.PatientSearchOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface PatientSearchRepository {
    suspend fun searchPatients(token: String, query: String): PatientSearchOutcome
}

class DefaultPatientSearchRepository(
    private val dataSource: PatientSearchDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PatientSearchRepository {
    override suspend fun searchPatients(token: String, query: String): PatientSearchOutcome =
        withContext(ioDispatcher) {
            try {
                dataSource.searchPatients(token, query)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                PatientSearchOutcome.NetworkError
            } catch (_: Exception) {
                PatientSearchOutcome.ServerError()
            }
        }
}
