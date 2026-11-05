package com.essaid.user.presentation.profile

import com.essaid.user.data.repository.PatientProfileRepository
import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PersonalPatientInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PatientProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads profile successfully and updates state`() = runTest {
        val profile = PatientProfile(
            personal = PersonalPatientInfo(email = "test@tecsup.edu.pe", nombre = "Ana", apellido = "García", tipoDocumento = "DNI", numeroDocumento = "87654321", celular = "999888777", fechaNacimiento = "10/10/1995"),
            medical = MedicalPatientInfo(tipoSangre = "A+")
        )
        val repository = RecordingPatientProfileRepository(PatientProfileOutcome.Success(profile))
        val viewModel = PatientProfileViewModel(repository)

        viewModel.loadProfile("token-123")

        assertEquals(ProfileStatus.Loading, viewModel.uiState.value.status)
        advanceUntilIdle()

        assertEquals(ProfileStatus.Success, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.profile)
        assertEquals("Ana", viewModel.uiState.value.profile?.personal?.nombre)
    }

    @Test
    fun `handles unauthorized outcome`() = runTest {
        val repository = RecordingPatientProfileRepository(PatientProfileOutcome.Unauthorized())
        val viewModel = PatientProfileViewModel(repository)

        viewModel.loadProfile("token-expired")
        advanceUntilIdle()

        assertEquals(ProfileStatus.Unauthorized, viewModel.uiState.value.status)
    }

    @Test
    fun `handles blank token with unauthorized status`() = runTest {
        val repository = RecordingPatientProfileRepository(PatientProfileOutcome.Success(PatientProfile(
            PersonalPatientInfo(email = "e", nombre = "n", apellido = "a", tipoDocumento = "D", numeroDocumento = "1", celular = "1", fechaNacimiento = "1"),
            MedicalPatientInfo()
        )))
        val viewModel = PatientProfileViewModel(repository)

        viewModel.loadProfile("")

        assertEquals(ProfileStatus.Unauthorized, viewModel.uiState.value.status)
        assert(repository.called == false)
    }

    private class RecordingPatientProfileRepository(
        private val outcome: PatientProfileOutcome,
    ) : PatientProfileRepository {
        var called: Boolean = false

        override suspend fun getProfile(token: String): PatientProfileOutcome {
            called = true
            return outcome
        }
    }
}
