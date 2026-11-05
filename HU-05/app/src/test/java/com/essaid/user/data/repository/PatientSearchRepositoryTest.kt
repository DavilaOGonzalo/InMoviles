package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientSearchDataSource
import com.essaid.user.domain.model.PatientSearchOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PatientSearchRepositoryTest {
    @Test
    fun `maps IO failures to a network outcome`() = runTest {
        val repository = DefaultPatientSearchRepository(
            dataSource = object : PatientSearchDataSource {
                override suspend fun searchPatients(token: String, query: String): PatientSearchOutcome {
                    throw IOException("offline")
                }
            },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        assertSame(PatientSearchOutcome.NetworkError, repository.searchPatients("token", "query"))
    }
}
