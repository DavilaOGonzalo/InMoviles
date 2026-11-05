package com.essaid.user.presentation.patienthistory

import com.essaid.user.data.repository.PatientAtencionesRepository
import com.essaid.user.domain.model.AttendingDoctor
import com.essaid.user.domain.model.PatientAtencion
import com.essaid.user.domain.model.PatientAtencionesOutcome
import com.essaid.user.domain.model.PatientAtencionesResponse
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PatientAtencionesViewModelTest {
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
    fun `loads atenciones successfully and sets success status`() = runTest(dispatcher) {
        val atencion = PatientAtencion(
            id = 101L,
            patientId = 42L,
            medico = AttendingDoctor(7L, "Dra.", "Prueba"),
            fechaAtencion = "2026-03-01T10:00:00Z",
            motivoConsulta = "Motivo de prueba",
            presionArterial = "120/80",
            frecuenciaCardiaca = 70,
            temperatura = "36.5",
            saturacionO2 = 98,
            pesoKg = "70.0",
            tallaCm = "170.0",
            diagnostico = "Diagnóstico de prueba",
            codigoCie10 = "J00",
            tratamiento = "Tratamiento de prueba",
            notas = "Notas de prueba",
            creadoEn = "2026-03-01T10:30:00Z",
        )
        val response = PatientAtencionesResponse(count = 1, next = null, previous = null, results = listOf(atencion))
        val repository = RecordingPatientAtencionesRepository(PatientAtencionesOutcome.Success(response))
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.loadAtenciones("access-token", 42)
        advanceUntilIdle()

        assertEquals(PatientAtencionesStatus.Success, viewModel.uiState.value.status)
        assertEquals(listOf(atencion), viewModel.uiState.value.atenciones)
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `shows empty state when atenciones list is empty`() = runTest(dispatcher) {
        val response = PatientAtencionesResponse(count = 0, next = null, previous = null, results = emptyList())
        val repository = RecordingPatientAtencionesRepository(PatientAtencionesOutcome.Success(response))
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.loadAtenciones("access-token", 42)
        advanceUntilIdle()

        assertEquals(PatientAtencionesStatus.Empty, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.response)
    }

    @Test
    fun `handles unauthorized outcome correctly`() = runTest(dispatcher) {
        val repository = RecordingPatientAtencionesRepository(PatientAtencionesOutcome.Unauthorized)
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.loadAtenciones("expired-token", 42)
        advanceUntilIdle()

        assertEquals(PatientAtencionesStatus.Unauthorized, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.message)
    }

    @Test
    fun `rejects blank token without calling repository`() = runTest(dispatcher) {
        val repository = RecordingPatientAtencionesRepository(
            PatientAtencionesOutcome.Success(PatientAtencionesResponse(0, null, null, emptyList())),
        )
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.loadAtenciones("", 42)
        advanceUntilIdle()

        assertEquals(PatientAtencionesStatus.Unauthorized, viewModel.uiState.value.status)
        assertEquals(0, repository.requests.size)
    }

    private class RecordingPatientAtencionesRepository(
        private val outcome: PatientAtencionesOutcome,
    ) : PatientAtencionesRepository {
        val requests = mutableListOf<Triple<String, Long, String?>>()

        override suspend fun getAtenciones(
            token: String,
            patientId: Long,
            desde: String?,
            hasta: String?,
            page: Int,
        ): PatientAtencionesOutcome {
            requests += Triple(token, patientId, desde)
            return outcome
        }
    }
}
