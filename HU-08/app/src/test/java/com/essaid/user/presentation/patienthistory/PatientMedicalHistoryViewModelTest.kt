package com.essaid.user.presentation.patienthistory

import com.essaid.user.data.repository.PatientMedicalHistoryRepository
import com.essaid.user.domain.model.MedicalHistoryCategory
import com.essaid.user.domain.model.MedicalHistoryEntry
import com.essaid.user.domain.model.PatientMedicalHistory
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome
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
class PatientMedicalHistoryViewModelTest {
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
    fun `loads antecedents for selected patient and marks nonempty response`() = runTest(dispatcher) {
        val entry = MedicalHistoryEntry(
            id = 1,
            description = "Registro ficticio de prueba",
            approximateDate = null,
            active = true,
            recordedBy = null,
            createdAt = null,
        )
        val history = PatientMedicalHistory.empty().copy(
            entriesByCategory = PatientMedicalHistory.empty().entriesByCategory +
                (MedicalHistoryCategory.Allergy to listOf(entry)),
        )
        val repository = RecordingPatientMedicalHistoryRepository(
            PatientMedicalHistoryOutcome.Success(history),
        )
        val viewModel = PatientMedicalHistoryViewModel(repository)

        viewModel.loadHistory("access-token", 42)
        advanceUntilIdle()

        assertEquals(listOf("access-token" to 42L), repository.requests)
        assertEquals(PatientMedicalHistoryStatus.Success, viewModel.uiState.value.status)
        assertEquals(listOf(entry), viewModel.uiState.value.history?.entriesByCategory?.get(MedicalHistoryCategory.Allergy))
    }

    @Test
    fun `shows empty state when all categories have no antecedents`() = runTest(dispatcher) {
        val viewModel = PatientMedicalHistoryViewModel(
            RecordingPatientMedicalHistoryRepository(
                PatientMedicalHistoryOutcome.Success(PatientMedicalHistory.empty()),
            ),
        )

        viewModel.loadHistory("access-token", 42)
        advanceUntilIdle()

        assertEquals(PatientMedicalHistoryStatus.Empty, viewModel.uiState.value.status)
        assertNotNull(viewModel.uiState.value.history)
    }

    @Test
    fun `distinguishes unauthorized and forbidden access`() = runTest(dispatcher) {
        val unauthorized = PatientMedicalHistoryViewModel(
            RecordingPatientMedicalHistoryRepository(PatientMedicalHistoryOutcome.Unauthorized),
        )
        unauthorized.loadHistory("expired-token", 42)
        advanceUntilIdle()
        assertEquals(PatientMedicalHistoryStatus.Unauthorized, unauthorized.uiState.value.status)

        val forbidden = PatientMedicalHistoryViewModel(
            RecordingPatientMedicalHistoryRepository(PatientMedicalHistoryOutcome.Forbidden),
        )
        forbidden.loadHistory("patient-token", 42)
        advanceUntilIdle()
        assertEquals(PatientMedicalHistoryStatus.Forbidden, forbidden.uiState.value.status)
    }

    @Test
    fun `rejects missing session without calling repository`() = runTest(dispatcher) {
        val repository = RecordingPatientMedicalHistoryRepository(
            PatientMedicalHistoryOutcome.Success(PatientMedicalHistory.empty()),
        )
        val viewModel = PatientMedicalHistoryViewModel(repository)

        viewModel.loadHistory("", 42)
        advanceUntilIdle()

        assertEquals(PatientMedicalHistoryStatus.Unauthorized, viewModel.uiState.value.status)
        assertEquals(emptyList<Pair<String, Long>>(), repository.requests)
        assertNull(viewModel.uiState.value.history)
    }

    private class RecordingPatientMedicalHistoryRepository(
        private val outcome: PatientMedicalHistoryOutcome,
    ) : PatientMedicalHistoryRepository {
        val requests = mutableListOf<Pair<String, Long>>()

        override suspend fun getHistory(token: String, patientId: Long): PatientMedicalHistoryOutcome {
            requests += token to patientId
            return outcome
        }
    }
}
