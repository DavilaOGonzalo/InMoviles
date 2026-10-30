package com.essaid.user.presentation.profile

import com.essaid.user.data.repository.PatientProfileRepository
import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PatientProfileUpdateForm
import com.essaid.user.domain.model.PatientProfileUpdateOutcome
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    fun `updates profile successfully and exits editing mode`() = runTest {
        val initialProfile = PatientProfile(
            personal = PersonalPatientInfo(email = "test@tecsup.edu.pe", nombre = "Ana", apellido = "García", tipoDocumento = "DNI", numeroDocumento = "87654321", celular = "999888777", fechaNacimiento = "10/10/1995"),
            medical = MedicalPatientInfo(tipoSangre = "A+")
        )
        val updatedProfile = initialProfile.copy(
            personal = initialProfile.personal.copy(celular = "911222333")
        )
        val repository = RecordingPatientProfileRepository(
            outcome = PatientProfileOutcome.Success(initialProfile),
            updateOutcome = PatientProfileUpdateOutcome.Success(updatedProfile, "Actualizado")
        )
        val viewModel = PatientProfileViewModel(repository)

        viewModel.loadProfile("token-123")
        advanceUntilIdle()

        viewModel.startEditing()
        assertTrue(viewModel.uiState.value.isEditing)
        viewModel.onCelularChanged("911222333")

        viewModel.updateProfile("token-123")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEditing)
        assertEquals("911222333", viewModel.uiState.value.profile?.personal?.celular)
        assertEquals("Actualizado", viewModel.uiState.value.successMessage)
        assertTrue(repository.updateCalled)
    }

    @Test
    fun `rejects invalid update form without calling repository`() = runTest {
        val initialProfile = PatientProfile(
            personal = PersonalPatientInfo(email = "test@tecsup.edu.pe", nombre = "Ana", apellido = "García", tipoDocumento = "DNI", numeroDocumento = "87654321", celular = "999888777", fechaNacimiento = "10/10/1995"),
            medical = MedicalPatientInfo(tipoSangre = "A+")
        )
        val repository = RecordingPatientProfileRepository(PatientProfileOutcome.Success(initialProfile))
        val viewModel = PatientProfileViewModel(repository)

        viewModel.loadProfile("token-123")
        advanceUntilIdle()

        viewModel.startEditing()
        viewModel.onCelularChanged("123") // invalid phone
        viewModel.updateProfile("token-123")

        assertFalse(repository.updateCalled)
        assertNotNull(viewModel.uiState.value.errorFor("celular"))
        assertTrue(viewModel.uiState.value.isEditing)
    }

    private class RecordingPatientProfileRepository(
        private val outcome: PatientProfileOutcome,
        private val updateOutcome: PatientProfileUpdateOutcome = PatientProfileUpdateOutcome.Success(
            PatientProfile(
                PersonalPatientInfo(email = "test@tecsup.edu.pe", nombre = "Ana", apellido = "García", tipoDocumento = "DNI", numeroDocumento = "87654321", celular = "911222333", fechaNacimiento = "10/10/1995"),
                MedicalPatientInfo(tipoSangre = "A+")
            )
        )
    ) : PatientProfileRepository {
        var called: Boolean = false
        var updateCalled: Boolean = false
        var receivedForm: PatientProfileUpdateForm? = null

        override suspend fun getProfile(token: String): PatientProfileOutcome {
            called = true
            return outcome
        }

        override suspend fun updateProfile(token: String, form: PatientProfileUpdateForm): PatientProfileUpdateOutcome {
            updateCalled = true
            receivedForm = form
            return updateOutcome
        }
    }
}
