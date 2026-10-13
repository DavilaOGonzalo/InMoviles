package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientProfileDataSource
import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PersonalPatientInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PatientProfileRepositoryTest {
    @Test
    fun `returns success outcome when data source succeeds`() = runTest {
        val profile = PatientProfile(
            personal = PersonalPatientInfo(email = "test@tecsup.edu.pe", nombre = "Juan", apellido = "Pérez", tipoDocumento = "DNI", numeroDocumento = "12345678", celular = "987654321", fechaNacimiento = "01/01/2000"),
            medical = MedicalPatientInfo(tipoSangre = "O+")
        )
        val dataSource = FakePatientProfileDataSource(PatientProfileOutcome.Success(profile))
        val repository = DefaultPatientProfileRepository(dataSource)

        val result = repository.getProfile("token-valid")

        assert(result is PatientProfileOutcome.Success)
        assertEquals(profile, (result as PatientProfileOutcome.Success).profile)
    }

    @Test
    fun `catches IOException and returns NetworkError`() = runTest {
        val dataSource = FakePatientProfileDataSource(null, throwIo = true)
        val repository = DefaultPatientProfileRepository(dataSource)

        val result = repository.getProfile("token-valid")

        assert(result is PatientProfileOutcome.NetworkError)
    }

    private class FakePatientProfileDataSource(
        private val outcome: PatientProfileOutcome?,
        private val throwIo: Boolean = false,
    ) : PatientProfileDataSource {
        override suspend fun getProfile(token: String): PatientProfileOutcome {
            if (throwIo) throw IOException("Network error")
            return outcome ?: PatientProfileOutcome.ServerError()
        }
    }
}
