package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientConsultationDataSource
import com.essaid.user.domain.model.PatientConsultationsOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PatientConsultationRepositoryTest {
    @Test
    fun `maps IO failures to a network outcome for consultations`() = runTest {
        val repository = DefaultPatientConsultationRepository(
            dataSource = object : PatientConsultationDataSource {
                override suspend fun getConsultations(
                    token: String,
                    patientId: Long,
                    desde: String?,
                    hasta: String?,
                    page: Int?,
                ): PatientConsultationsOutcome = throw IOException("offline")
            },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        assertSame(
            PatientConsultationsOutcome.NetworkError,
            repository.getConsultations("access-token", 42),
        )
    }
}
