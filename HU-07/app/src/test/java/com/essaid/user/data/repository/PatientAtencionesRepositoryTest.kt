package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientAtencionesDataSource
import com.essaid.user.domain.model.PatientAtencionesOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PatientAtencionesRepositoryTest {
    @Test
    fun `maps IO failures to network outcome`() = runTest {
        val repository = DefaultPatientAtencionesRepository(
            dataSource = object : PatientAtencionesDataSource {
                override suspend fun getAtenciones(
                    token: String,
                    patientId: Long,
                    desde: String?,
                    hasta: String?,
                    page: Int,
                ): PatientAtencionesOutcome = throw IOException("offline")
            },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        assertSame(
            PatientAtencionesOutcome.NetworkError,
            repository.getAtenciones("access-token", 42),
        )
    }
}
