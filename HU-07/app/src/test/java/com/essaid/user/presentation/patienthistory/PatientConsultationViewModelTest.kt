package com.essaid.user.presentation.patienthistory

import com.essaid.user.data.repository.PatientConsultationRepository
import com.essaid.user.domain.model.ConsultationEntry
import com.essaid.user.domain.model.PatientConsultations
import com.essaid.user.domain.model.PatientConsultationsOutcome
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
class PatientConsultationViewModelTest {
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
    fun `loads consultations for selected patient and marks success`() = runTest(dispatcher) {
        val entry = ConsultationEntry(
            id = 101,
            paciente = 42,
            medico = null,
            fechaAtencion = "2026-03-01T10:00:00-05:00",
            motivoConsulta = "Control",
            presionArterial = "120/80",
            frecuenciaCardiaca = 72,
            temperatura = 36.5,
            saturacionO2 = 98,
            pesoKg = 70.0,
            tallaCm = 170.0,
            diagnostico = "Sano",
            codigoCie10 = "Z00",
            tratamiento = "Ninguno",
            notas = null,
            creadoEn = null,
        )
        val consultations = PatientConsultations(1, null, null, listOf(entry))
        val repository = RecordingPatientConsultationRepository(
            PatientConsultationsOutcome.Success(consultations),
        )
        val viewModel = PatientConsultationViewModel(repository)

        viewModel.loadConsultations("access-token", 42)
        advanceUntilIdle()

        assertEquals(PatientConsultationStatus.Success, viewModel.uiState.value.status)
        assertEquals(listOf(entry), viewModel.uiState.value.consultations?.entries)
    }

    @Test
    fun `shows empty state when consultations list is empty`() = runTest(dispatcher) {
        val viewModel = PatientConsultationViewModel(
            RecordingPatientConsultationRepository(
                PatientConsultationsOutcome.Success(PatientConsultations.empty()),
            ),
        )

        viewModel.loadConsultations("access-token", 42)
        advanceUntilIdle()

        assertEquals(PatientConsultationStatus.Empty, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.consultations)
    }

    @Test
    fun `handles unauthorized and network error states`() = runTest(dispatcher) {
        val unauthorizedVM = PatientConsultationViewModel(
            RecordingPatientConsultationRepository(PatientConsultationsOutcome.Unauthorized),
        )
        unauthorizedVM.loadConsultations("bad-token", 42)
        advanceUntilIdle()
        assertEquals(PatientConsultationStatus.Unauthorized, unauthorizedVM.uiState.value.status)

        val netErrorVM = PatientConsultationViewModel(
            RecordingPatientConsultationRepository(PatientConsultationsOutcome.NetworkError),
        )
        netErrorVM.loadConsultations("access-token", 42)
        advanceUntilIdle()
        assertEquals(PatientConsultationStatus.NetworkError, netErrorVM.uiState.value.status)
    }

    @Test
    fun `rejects missing session without calling repository`() = runTest(dispatcher) {
        val repository = RecordingPatientConsultationRepository(
            PatientConsultationsOutcome.Success(PatientConsultations.empty()),
        )
        val viewModel = PatientConsultationViewModel(repository)

        viewModel.loadConsultations("", 42)
        advanceUntilIdle()

        assertEquals(PatientConsultationStatus.Unauthorized, viewModel.uiState.value.status)
        assertEquals(emptyList<Triple<String, Long, String?>>(), repository.requests)
        assertNull(viewModel.uiState.value.consultations)
    }

    private class RecordingPatientConsultationRepository(
        private val outcome: PatientConsultationsOutcome,
    ) : PatientConsultationRepository {
        val requests = mutableListOf<Triple<String, Long, String?>>()

        override suspend fun getConsultations(
            token: String,
            patientId: Long,
            desde: String?,
            hasta: String?,
            page: Int?,
        ): PatientConsultationsOutcome {
            requests += Triple(token, patientId, desde)
            return outcome
        }
    }
}
