package com.essaid.user.presentation.patienthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.PatientConsultationRepository
import com.essaid.user.domain.model.PatientConsultations
import com.essaid.user.domain.model.PatientConsultationsOutcome
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PatientConsultationStatus {
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

data class PatientConsultationUiState(
    val patientId: Long? = null,
    val status: PatientConsultationStatus = PatientConsultationStatus.Idle,
    val consultations: PatientConsultations? = null,
    val desde: String? = null,
    val hasta: String? = null,
    val page: Int = 1,
    val message: String? = null,
)

class PatientConsultationViewModel(
    private val repository: PatientConsultationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PatientConsultationUiState())
    val uiState: StateFlow<PatientConsultationUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadConsultations(
        token: String,
        patientId: Long,
        desde: String? = _uiState.value.desde,
        hasta: String? = _uiState.value.hasta,
        page: Int = 1,
    ) {
        if (token.isBlank()) {
            loadJob?.cancel()
            _uiState.value = PatientConsultationUiState(
                patientId = patientId,
                desde = desde,
                hasta = hasta,
                page = page,
                status = PatientConsultationStatus.Unauthorized,
                message = "No hay una sesión válida para consultar las atenciones.",
            )
            return
        }
        if (patientId <= 0) {
            loadJob?.cancel()
            _uiState.value = PatientConsultationUiState(
                patientId = patientId,
                desde = desde,
                hasta = hasta,
                page = page,
                status = PatientConsultationStatus.NotFound,
                message = "No se encontró el paciente seleccionado.",
            )
            return
        }

        loadJob?.cancel()
        _uiState.update { state ->
            state.copy(
                patientId = patientId,
                desde = desde,
                hasta = hasta,
                page = page,
                status = PatientConsultationStatus.Loading,
                message = null,
            )
        }

        loadJob = viewModelScope.launch {
            val outcome = repository.getConsultations(token, patientId, desde, hasta, page)
            if (_uiState.value.patientId != patientId) return@launch
            _uiState.update { state ->
                when (outcome) {
                    is PatientConsultationsOutcome.Success -> state.copy(
                        status = if (outcome.consultations.isEmpty) {
                            PatientConsultationStatus.Empty
                        } else {
                            PatientConsultationStatus.Success
                        },
                        consultations = outcome.consultations,
                        message = null,
                    )
                    PatientConsultationsOutcome.Unauthorized -> state.copy(
                        status = PatientConsultationStatus.Unauthorized,
                        message = "La sesión expiró o no está autorizada. Inicia sesión nuevamente.",
                    )
                    PatientConsultationsOutcome.Forbidden -> state.copy(
                        status = PatientConsultationStatus.Forbidden,
                        message = "Tu cuenta no tiene permiso para consultar atenciones.",
                    )
                    PatientConsultationsOutcome.NotFound -> state.copy(
                        status = PatientConsultationStatus.NotFound,
                        message = "No se encontró el historial de atenciones del paciente.",
                    )
                    PatientConsultationsOutcome.NetworkError -> state.copy(
                        status = PatientConsultationStatus.NetworkError,
                        message = "No se pudo conectar con EssAid. Revisa tu conexión.",
                    )
                    is PatientConsultationsOutcome.ServerError -> state.copy(
                        status = PatientConsultationStatus.ServerError,
                        message = "No se pudieron consultar las atenciones. Inténtalo más tarde.",
                    )
                }
            }
        }
    }

    fun applyDateFilter(token: String, patientId: Long, desde: String?, hasta: String?) {
        loadConsultations(token, patientId, desde = desde, hasta = hasta, page = 1)
    }

    companion object {
        fun Factory(repository: PatientConsultationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (!modelClass.isAssignableFrom(PatientConsultationViewModel::class.java)) {
                        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                    }
                    return PatientConsultationViewModel(repository) as T
                }
            }
    }
}
