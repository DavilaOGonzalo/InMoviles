package com.essaid.user.data.repository

import com.essaid.user.data.remote.PatientProfileDataSource
import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PatientProfileUpdateForm
import com.essaid.user.domain.model.PatientProfileUpdateOutcome
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
        val dataSource = FakePatientProfileDataSource(profileOutcome = PatientProfileOutcome.Success(profile))
        val repository = DefaultPatientProfileRepository(dataSource)

        val result = repository.getProfile("token-valid")

        assert(result is PatientProfileOutcome.Success)
        assertEquals(profile, (result as PatientProfileOutcome.Success).profile)
    }

    @Test
    fun `returns success outcome when updating profile succeeds`() = runTest {
        val profile = PatientProfile(
            personal = PersonalPatientInfo(email = "test@tecsup.edu.pe", nombre = "Juan", apellido = "Pérez", tipoDocumento = "DNI", numeroDocumento = "12345678", celular = "911222333", fechaNacimiento = "01/01/2000"),
            medical = MedicalPatientInfo(tipoSangre = "O+")
        )
        val dataSource = FakePatientProfileDataSource(updateOutcome = PatientProfileUpdateOutcome.Success(profile))
        val repository = DefaultPatientProfileRepository(dataSource)

        val result = repository.updateProfile("token-valid", PatientProfileUpdateForm(celular = "911222333"))

        assert(result is PatientProfileUpdateOutcome.Success)
        assertEquals("911222333", (result as PatientProfileUpdateOutcome.Success).profile.personal.celular)
    }

    @Test
    fun `catches IOException and returns NetworkError`() = runTest {
        val dataSource = FakePatientProfileDataSource(throwIo = true)
        val repository = DefaultPatientProfileRepository(dataSource)

        val result = repository.getProfile("token-valid")

        assert(result is PatientProfileOutcome.NetworkError)
    }

    private class FakePatientProfileDataSource(
        private val profileOutcome: PatientProfileOutcome? = null,
        private val updateOutcome: PatientProfileUpdateOutcome? = null,
        private val throwIo: Boolean = false,
    ) : PatientProfileDataSource {
        override suspend fun getProfile(token: String): PatientProfileOutcome {
            if (throwIo) throw IOException("Network error")
            return profileOutcome ?: PatientProfileOutcome.ServerError()
        }

        override suspend fun updateProfile(token: String, form: PatientProfileUpdateForm): PatientProfileUpdateOutcome {
            if (throwIo) throw IOException("Network error")
            return updateOutcome ?: PatientProfileUpdateOutcome.ServerError()
        }
    }
}
