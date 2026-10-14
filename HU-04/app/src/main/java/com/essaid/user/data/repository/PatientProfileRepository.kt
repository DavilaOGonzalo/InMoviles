package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientProfileDataSource
import com.essaid.user.domain.model.PatientProfileOutcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface PatientProfileRepository {
    suspend fun getProfile(token: String): PatientProfileOutcome
}

class DefaultPatientProfileRepository(
    private val dataSource: PatientProfileDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PatientProfileRepository {
    override suspend fun getProfile(token: String): PatientProfileOutcome =
        withContext(ioDispatcher) {
            try {
                dataSource.getProfile(token)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                PatientProfileOutcome.NetworkError()
            } catch (_: Exception) {
                PatientProfileOutcome.ServerError()
            }
        }
}
