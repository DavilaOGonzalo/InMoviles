package com.essaid.user.presentation.patienthistory

import com.essaid.user.data.repository.PatientAtencionesRepository
import com.essaid.user.domain.model.AttendingDoctor
import com.essaid.user.domain.model.CreateAtencionRequest
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        val repository = RecordingPatientAtencionesRepository(getOutcome = PatientAtencionesOutcome.Success(response))
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
        val repository = RecordingPatientAtencionesRepository(getOutcome = PatientAtencionesOutcome.Success(response))
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.loadAtenciones("access-token", 42)
        advanceUntilIdle()

        assertEquals(PatientAtencionesStatus.Empty, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.response)
    }

    @Test
    fun `handles unauthorized outcome correctly`() = runTest(dispatcher) {
        val repository = RecordingPatientAtencionesRepository(getOutcome = PatientAtencionesOutcome.Unauthorized)
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.loadAtenciones("expired-token", 42)
        advanceUntilIdle()

        assertEquals(PatientAtencionesStatus.Unauthorized, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.message)
    }

    @Test
    fun `rejects blank token without calling repository`() = runTest(dispatcher) {
        val repository = RecordingPatientAtencionesRepository(
            getOutcome = PatientAtencionesOutcome.Success(PatientAtencionesResponse(0, null, null, emptyList())),
        )
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.loadAtenciones("", 42)
        advanceUntilIdle()

        assertEquals(PatientAtencionesStatus.Unauthorized, viewModel.uiState.value.status)
        assertEquals(0, repository.getRequests.size)
    }

    @Test
    fun `submits valid new atencion, shows success, resets form, and reloads history`() = runTest(dispatcher) {
        val createdAtencion = PatientAtencion(
            id = 200L,
            patientId = 42L,
            medico = AttendingDoctor(7L, "Dra.", "Prueba"),
            fechaAtencion = "2026-03-31T12:00:00Z",
            motivoConsulta = "Fiebre persistente",
            presionArterial = "110/70",
            frecuenciaCardiaca = 80,
            temperatura = "38.5",
            saturacionO2 = 98,
            pesoKg = "65.0",
            tallaCm = "165.0",
            diagnostico = "Síndrome febril",
            codigoCie10 = "R50.9",
            tratamiento = "Paracetamol 500mg c/8h",
            notas = "Guardar reposo",
            creadoEn = "2026-03-31T12:00:00Z",
        )
        val getResponse = PatientAtencionesResponse(count = 1, next = null, previous = null, results = listOf(createdAtencion))
        val repository = RecordingPatientAtencionesRepository(
            getOutcome = PatientAtencionesOutcome.Success(getResponse),
            createOutcome = PatientAtencionesOutcome.CreateSuccess(createdAtencion),
        )
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.toggleFormVisibility(true)
        viewModel.onMotivoChanged("Fiebre persistente")
        viewModel.onDiagnosticoChanged("Síndrome febril")
        viewModel.onTratamientoChanged("Paracetamol 500mg c/8h")
        viewModel.onCodigoCie10Changed("R50.9")
        viewModel.onPresionArterialChanged("110/70")
        viewModel.onFrecuenciaCardiacaChanged("80")
        viewModel.onTemperaturaChanged("38.5")
        viewModel.onSaturacionO2Changed("98")
        viewModel.onPesoKgChanged("65.0")
        viewModel.onTallaCmChanged("165.0")
        viewModel.onNotasChanged("Guardar reposo")

        viewModel.submitNewAtencion("access-token", 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertFalse(state.isFormVisible)
        assertEquals("Atención médica registrada exitosamente.", state.createSuccessMessage)
        assertEquals("", state.motivoInput)
        assertEquals(1, repository.createRequests.size)
        val req = repository.createRequests.single()
        assertEquals("Fiebre persistente", req.third.motivoConsulta)
        assertEquals("Síndrome febril", req.third.diagnostico)
        assertEquals("Paracetamol 500mg c/8h", req.third.tratamiento)
        assertEquals(1, repository.getRequests.size)
    }

    @Test
    fun `shows local validation errors and avoids repository submission`() = runTest(dispatcher) {
        val repository = RecordingPatientAtencionesRepository(getOutcome = PatientAtencionesOutcome.Unauthorized)
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.onMotivoChanged("") // Blank
        viewModel.onDiagnosticoChanged("") // Blank
        viewModel.onTratamientoChanged("") // Blank

        viewModel.submitNewAtencion("access-token", 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertTrue(state.formFieldErrors.containsKey("motivo_consulta"))
        assertTrue(state.formFieldErrors.containsKey("diagnostico"))
        assertTrue(state.formFieldErrors.containsKey("tratamiento"))
        assertEquals(0, repository.createRequests.size)
    }

    @Test
    fun `handles server validation errors correctly`() = runTest(dispatcher) {
        val serverErrors = mapOf("codigo_cie10" to "Código CIE-10 no existente.")
        val repository = RecordingPatientAtencionesRepository(
            createOutcome = PatientAtencionesOutcome.ValidationError(serverErrors),
        )
        val viewModel = PatientAtencionesViewModel(repository)

        viewModel.onMotivoChanged("Consulta rutinaria")
        viewModel.onDiagnosticoChanged("Saludable")
        viewModel.onTratamientoChanged("Ninguno")
        viewModel.onCodigoCie10Changed("INVALID")

        viewModel.submitNewAtencion("access-token", 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Código CIE-10 no existente.", state.formFieldErrors["codigo_cie10"])
    }

    private class RecordingPatientAtencionesRepository(
        private val getOutcome: PatientAtencionesOutcome = PatientAtencionesOutcome.Unauthorized,
        private val createOutcome: PatientAtencionesOutcome = PatientAtencionesOutcome.Unauthorized,
    ) : PatientAtencionesRepository {
        val getRequests = mutableListOf<Triple<String, Long, String?>>()
        val createRequests = mutableListOf<Triple<String, Long, CreateAtencionRequest>>()

        override suspend fun getAtenciones(
            token: String,
            patientId: Long,
            desde: String?,
            hasta: String?,
            page: Int,
        ): PatientAtencionesOutcome {
            getRequests += Triple(token, patientId, desde)
            return getOutcome
        }

        override suspend fun createAtencion(
            token: String,
            patientId: Long,
            request: CreateAtencionRequest,
        ): PatientAtencionesOutcome {
            createRequests += Triple(token, patientId, request)
            return createOutcome
        }
    }
}
