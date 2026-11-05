package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientMedicalHistoryDataSource
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PatientMedicalHistoryRepositoryTest {
    @Test
    fun `maps IO failures to a network outcome`() = runTest {
        val repository = DefaultPatientMedicalHistoryRepository(
            dataSource = object : PatientMedicalHistoryDataSource {
                override suspend fun getHistory(
                    token: String,
                    patientId: Long,
                ): PatientMedicalHistoryOutcome = throw IOException("offline")
            },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        assertSame(
            PatientMedicalHistoryOutcome.NetworkError,
            repository.getHistory("access-token", 42),
        )
    }
}
