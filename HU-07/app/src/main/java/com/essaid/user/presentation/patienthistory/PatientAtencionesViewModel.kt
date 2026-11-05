package com.essaid.user.presentation.patienthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.PatientAtencionesRepository
import com.essaid.user.domain.model.PatientAtencion
import com.essaid.user.domain.model.PatientAtencionesOutcome
import com.essaid.user.domain.model.PatientAtencionesResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PatientAtencionesStatus {
    Idle,
    Loading,
    Success,
    Empty,
    ValidationError,
    Unauthorized,
    Forbidden,
    NotFound,
    NetworkError,
    ServerError,
}

data class PatientAtencionesUiState(
    val patientId: Long? = null,
    val status: PatientAtencionesStatus = PatientAtencionesStatus.Idle,
    val response: PatientAtencionesResponse? = null,
    val atenciones: List<PatientAtencion> = emptyList(),
    val desde: String? = null,
    val hasta: String? = null,
    val currentPage: Int = 1,
    val selectedAtencion: PatientAtencion? = null,
    val message: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
)

class PatientAtencionesViewModel(
    private val repository: PatientAtencionesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PatientAtencionesUiState())
    val uiState: StateFlow<PatientAtencionesUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun loadAtenciones(
        token: String,
        patientId: Long,
        desde: String? = _uiState.value.desde,
        hasta: String? = _uiState.value.hasta,
        page: Int = 1,
    ) {
        if (token.isBlank()) {
            loadJob?.cancel()
            _uiState.value = PatientAtencionesUiState(
                patientId = patientId,
                desde = desde,
                hasta = hasta,
                currentPage = page,
                status = PatientAtencionesStatus.Unauthorized,
                message = "No hay una sesión válida para consultar el historial de atenciones.",
            )
            return
        }
        if (patientId <= 0) {
            loadJob?.cancel()
            _uiState.value = PatientAtencionesUiState(
                patientId = patientId,
                desde = desde,
                hasta = hasta,
                currentPage = page,
                status = PatientAtencionesStatus.NotFound,
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
                currentPage = page,
                status = PatientAtencionesStatus.Loading,
                message = null,
            )
        }

        loadJob = viewModelScope.launch {
            val outcome = repository.getAtenciones(token, patientId, desde, hasta, page)
            if (_uiState.value.patientId != patientId) return@launch
            _uiState.update { state ->
                when (outcome) {
                    is PatientAtencionesOutcome.Success -> {
                        val resp = outcome.response
                        state.copy(
                            status = if (resp.isEmpty) PatientAtencionesStatus.Empty else PatientAtencionesStatus.Success,
                            response = resp,
                            atenciones = resp.results,
                            message = null,
                        )
                    }
                    is PatientAtencionesOutcome.ValidationError -> state.copy(
                        status = PatientAtencionesStatus.ValidationError,
                        fieldErrors = outcome.fieldErrors,
                        message = outcome.fieldErrors.values.firstOrNull() ?: "Error de validación en las fechas de consulta.",
                    )
                    PatientAtencionesOutcome.Unauthorized -> state.copy(
                        status = PatientAtencionesStatus.Unauthorized,
                        message = "La sesión expiró o no está autorizada. Inicia sesión nuevamente.",
                    )
                    PatientAtencionesOutcome.Forbidden -> state.copy(
                        status = PatientAtencionesStatus.Forbidden,
                        message = "Tu cuenta no tiene permiso para consultar atenciones médicas.",
                    )
                    PatientAtencionesOutcome.NotFound -> state.copy(
                        status = PatientAtencionesStatus.NotFound,
                        message = "No se encontró el historial de atenciones del paciente.",
                    )
                    PatientAtencionesOutcome.NetworkError -> state.copy(
                        status = PatientAtencionesStatus.NetworkError,
                        message = "No se pudo conectar con EssAid. Revisa tu conexión.",
                    )
                    is PatientAtencionesOutcome.ServerError -> state.copy(
                        status = PatientAtencionesStatus.ServerError,
                        message = "No se pudieron consultar las atenciones. Inténtalo más tarde.",
                    )
                }
            }
        }
    }

    fun updateFilters(token: String, patientId: Long, desde: String?, hasta: String?) {
        loadAtenciones(token, patientId, desde = desde?.takeIf(String::isNotBlank), hasta = hasta?.takeIf(String::isNotBlank), page = 1)
    }

    fun loadNextPage(token: String, patientId: Long) {
        val current = _uiState.value
        if (current.response?.next != null) {
            loadAtenciones(token, patientId, desde = current.desde, hasta = current.hasta, page = current.currentPage + 1)
        }
    }

    fun loadPreviousPage(token: String, patientId: Long) {
        val current = _uiState.value
        if (current.response?.previous != null && current.currentPage > 1) {
            loadAtenciones(token, patientId, desde = current.desde, hasta = current.hasta, page = current.currentPage - 1)
        }
    }

    fun selectAtencion(atencion: PatientAtencion?) {
        _uiState.update { it.copy(selectedAtencion = atencion) }
    }

    companion object {
        fun Factory(repository: PatientAtencionesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (!modelClass.isAssignableFrom(PatientAtencionesViewModel::class.java)) {
                        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                    }
                    return PatientAtencionesViewModel(repository) as T
                }
            }
    }
}
