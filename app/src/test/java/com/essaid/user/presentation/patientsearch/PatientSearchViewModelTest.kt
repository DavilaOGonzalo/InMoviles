package com.essaid.user.presentation.patientsearch

import com.essaid.user.data.repository.PatientSearchRepository
import com.essaid.user.domain.model.PatientSearchOutcome
import com.essaid.user.domain.model.PatientSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PatientSearchViewModelTest {
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
    fun `debounces search and forwards session token`() = runTest(dispatcher) {
        val patient = samplePatient()
        val repository = RecordingPatientSearchRepository(PatientSearchOutcome.Success(listOf(patient)))
        val viewModel = PatientSearchViewModel(repository, debounceMillis = 300)

        viewModel.setSessionToken("doctor-token")
        viewModel.onQueryChanged("Paciente Ficticio")
        advanceTimeBy(299)

        assertTrue(repository.queries.isEmpty())
        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(listOf("doctor-token" to "Paciente Ficticio"), repository.queries)
        assertEquals(PatientSearchStatus.Success, viewModel.uiState.value.status)
        assertEquals(listOf(patient), viewModel.uiState.value.patients)
    }

    @Test
    fun `validates partial numeric DNI without making a request`() = runTest(dispatcher) {
        val repository = RecordingPatientSearchRepository(PatientSearchOutcome.Success(emptyList()))
        val viewModel = PatientSearchViewModel(repository, debounceMillis = 0)

        viewModel.setSessionToken("doctor-token")
        viewModel.onQueryChanged("1234")
        advanceUntilIdle()

        assertTrue(repository.queries.isEmpty())
        assertEquals(PatientSearchStatus.ValidationError, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.message.orEmpty().contains("8 dígitos"))
    }

    @Test
    fun `shows empty state when backend returns no patients`() = runTest(dispatcher) {
        val repository = RecordingPatientSearchRepository(PatientSearchOutcome.Success(emptyList()))
        val viewModel = PatientSearchViewModel(repository, debounceMillis = 0)

        viewModel.setSessionToken("doctor-token")
        viewModel.onQueryChanged("NoExiste")
        advanceUntilIdle()

        assertEquals(PatientSearchStatus.Empty, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.patients.isNotEmpty())
    }

    @Test
    fun `distinguishes unauthorized and forbidden responses`() = runTest(dispatcher) {
        val unauthorizedViewModel = PatientSearchViewModel(
            RecordingPatientSearchRepository(PatientSearchOutcome.Unauthorized),
            debounceMillis = 0,
        )
        unauthorizedViewModel.setSessionToken("expired-token")
        unauthorizedViewModel.onQueryChanged("Paciente")
        advanceUntilIdle()
        assertEquals(PatientSearchStatus.Unauthorized, unauthorizedViewModel.uiState.value.status)

        val forbiddenViewModel = PatientSearchViewModel(
            RecordingPatientSearchRepository(PatientSearchOutcome.Forbidden),
            debounceMillis = 0,
        )
        forbiddenViewModel.setSessionToken("patient-token")
        forbiddenViewModel.onQueryChanged("Paciente")
        advanceUntilIdle()
        assertEquals(PatientSearchStatus.Forbidden, forbiddenViewModel.uiState.value.status)
    }

    private fun samplePatient() = PatientSearchResult(
        id = 9001,
        nombre = "Paciente Ficticio",
        apellido = "Prueba",
        dni = "00000001",
        fechaNacimiento = null,
        sexo = null,
        tipoSangre = null,
        alergias = emptyList(),
        condicionesCronicas = emptyList(),
        medicamentosActuales = emptyList(),
        contactoEmergencia = null,
        telefonoEmergencia = null,
        observacionesMedicas = null,
        informacionMedicaActualizada = null,
    )

    private class RecordingPatientSearchRepository(
        private val outcome: PatientSearchOutcome,
    ) : PatientSearchRepository {
        val queries = mutableListOf<Pair<String, String>>()

        override suspend fun searchPatients(token: String, query: String): PatientSearchOutcome {
            queries += token to query
            return outcome
        }
    }
}
