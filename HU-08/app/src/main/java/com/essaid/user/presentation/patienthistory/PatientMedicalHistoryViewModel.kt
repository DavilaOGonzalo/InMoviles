package com.essaid.user.presentation.patienthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.PatientMedicalHistoryRepository
import com.essaid.user.domain.model.PatientMedicalHistory
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PatientMedicalHistoryStatus {
    Idle,
    Loading,
    Success,
    Empty,
    Unauthorized,
    Forbidden,
    NotFound,
    NetworkError,
    ServerError,
}

data class PatientMedicalHistoryUiState(
    val patientId: Long? = null,
    val status: PatientMedicalHistoryStatus = PatientMedicalHistoryStatus.Idle,
    val history: PatientMedicalHistory? = null,
    val message: String? = null,
)

class PatientMedicalHistoryViewModel(
    private val repository: PatientMedicalHistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PatientMedicalHistoryUiState())
    val uiState: StateFlow<PatientMedicalHistoryUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadHistory(token: String, patientId: Long) {
        if (token.isBlank()) {
            loadJob?.cancel()
            _uiState.value = PatientMedicalHistoryUiState(
                patientId = patientId,
                status = PatientMedicalHistoryStatus.Unauthorized,
                message = "No hay una sesión válida para consultar los antecedentes.",
            )
            return
        }
        if (patientId <= 0) {
            loadJob?.cancel()
            _uiState.value = PatientMedicalHistoryUiState(
                patientId = patientId,
                status = PatientMedicalHistoryStatus.NotFound,
                message = "No se encontró el paciente seleccionado.",
            )
            return
        }

        loadJob?.cancel()
        _uiState.value = PatientMedicalHistoryUiState(
            patientId = patientId,
            status = PatientMedicalHistoryStatus.Loading,
        )
        loadJob = viewModelScope.launch {
            val outcome = repository.getHistory(token, patientId)
            if (_uiState.value.patientId != patientId) return@launch
            _uiState.update { state ->
                when (outcome) {
                    is PatientMedicalHistoryOutcome.Success -> state.copy(
                        status = if (outcome.history.isEmpty) {
                            PatientMedicalHistoryStatus.Empty
                        } else {
                            PatientMedicalHistoryStatus.Success
                        },
                        history = outcome.history,
                        message = null,
                    )
                    PatientMedicalHistoryOutcome.Unauthorized -> state.copy(
                        status = PatientMedicalHistoryStatus.Unauthorized,
                        message = "La sesión expiró o no está autorizada. Inicia sesión nuevamente.",
                    )
                    PatientMedicalHistoryOutcome.Forbidden -> state.copy(
                        status = PatientMedicalHistoryStatus.Forbidden,
                        message = "Tu cuenta no tiene permiso para consultar antecedentes.",
                    )
                    PatientMedicalHistoryOutcome.NotFound -> state.copy(
                        status = PatientMedicalHistoryStatus.NotFound,
                        message = "No se encontró el perfil del paciente seleccionado.",
                    )
                    PatientMedicalHistoryOutcome.NetworkError -> state.copy(
                        status = PatientMedicalHistoryStatus.NetworkError,
                        message = "No se pudo conectar con EssAid. Revisa tu conexión.",
                    )
                    is PatientMedicalHistoryOutcome.ServerError -> state.copy(
                        status = PatientMedicalHistoryStatus.ServerError,
                        message = "No se pudieron consultar los antecedentes. Inténtalo más tarde.",
                    )
                }
            }
        }
    }

    companion object {
        fun Factory(repository: PatientMedicalHistoryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (!modelClass.isAssignableFrom(PatientMedicalHistoryViewModel::class.java)) {
                        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                    }
                    return PatientMedicalHistoryViewModel(repository) as T
                }
            }
    }
}
