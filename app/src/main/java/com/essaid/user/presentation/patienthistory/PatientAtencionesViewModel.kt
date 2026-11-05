package com.essaid.user.presentation.patienthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.PatientAtencionesRepository
import com.essaid.user.domain.model.CreateAtencionRequest
import com.essaid.user.domain.model.PatientAtencion
import com.essaid.user.domain.model.PatientAtencionesOutcome
import com.essaid.user.domain.model.PatientAtencionesResponse
import com.essaid.user.domain.validation.AtencionValidator
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
    // Registration Form State
    val isFormVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val createSuccessMessage: String? = null,
    val motivoInput: String = "",
    val diagnosticoInput: String = "",
    val tratamientoInput: String = "",
    val codigoCie10Input: String = "",
    val presionArterialInput: String = "",
    val frecuenciaCardiacaInput: String = "",
    val temperaturaInput: String = "",
    val saturacionO2Input: String = "",
    val pesoKgInput: String = "",
    val tallaCmInput: String = "",
    val notasInput: String = "",
    val formFieldErrors: Map<String, String> = emptyMap(),
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
                    is PatientAtencionesOutcome.CreateSuccess -> state
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

    fun toggleFormVisibility(visible: Boolean) {
        _uiState.update { it.copy(isFormVisible = visible) }
    }

    fun onMotivoChanged(value: String) {
        _uiState.update { it.copy(motivoInput = value, formFieldErrors = it.formFieldErrors - "motivo_consulta") }
    }

    fun onDiagnosticoChanged(value: String) {
        _uiState.update { it.copy(diagnosticoInput = value, formFieldErrors = it.formFieldErrors - "diagnostico") }
    }

    fun onTratamientoChanged(value: String) {
        _uiState.update { it.copy(tratamientoInput = value, formFieldErrors = it.formFieldErrors - "tratamiento") }
    }

    fun onCodigoCie10Changed(value: String) {
        _uiState.update { it.copy(codigoCie10Input = value, formFieldErrors = it.formFieldErrors - "codigo_cie10") }
    }

    fun onPresionArterialChanged(value: String) {
        _uiState.update { it.copy(presionArterialInput = value, formFieldErrors = it.formFieldErrors - "presion_arterial") }
    }

    fun onFrecuenciaCardiacaChanged(value: String) {
        _uiState.update { it.copy(frecuenciaCardiacaInput = value, formFieldErrors = it.formFieldErrors - "frecuencia_cardiaca") }
    }

    fun onTemperaturaChanged(value: String) {
        _uiState.update { it.copy(temperaturaInput = value, formFieldErrors = it.formFieldErrors - "temperatura") }
    }

    fun onSaturacionO2Changed(value: String) {
        _uiState.update { it.copy(saturacionO2Input = value, formFieldErrors = it.formFieldErrors - "saturacion_o2") }
    }

    fun onPesoKgChanged(value: String) {
        _uiState.update { it.copy(pesoKgInput = value, formFieldErrors = it.formFieldErrors - "peso_kg") }
    }

    fun onTallaCmChanged(value: String) {
        _uiState.update { it.copy(tallaCmInput = value, formFieldErrors = it.formFieldErrors - "talla_cm") }
    }

    fun onNotasChanged(value: String) {
        _uiState.update { it.copy(notasInput = value, formFieldErrors = it.formFieldErrors - "notas") }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(createSuccessMessage = null) }
    }

    fun resetCreateForm() {
        _uiState.update {
            it.copy(
                motivoInput = "",
                diagnosticoInput = "",
                tratamientoInput = "",
                codigoCie10Input = "",
                presionArterialInput = "",
                frecuenciaCardiacaInput = "",
                temperaturaInput = "",
                saturacionO2Input = "",
                pesoKgInput = "",
                tallaCmInput = "",
                notasInput = "",
                formFieldErrors = emptyMap(),
            )
        }
    }

    fun submitNewAtencion(token: String, patientId: Long) {
        val current = _uiState.value
        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    status = PatientAtencionesStatus.Unauthorized,
                    message = "La sesión expiró o no está autorizada.",
                )
            }
            return
        }
        if (patientId <= 0) {
            _uiState.update {
                it.copy(
                    status = PatientAtencionesStatus.NotFound,
                    message = "No se encontró el paciente seleccionado.",
                )
            }
            return
        }

        val request = CreateAtencionRequest(
            motivoConsulta = current.motivoInput,
            diagnostico = current.diagnosticoInput,
            tratamiento = current.tratamientoInput,
            codigoCie10 = current.codigoCie10Input.takeIf(String::isNotBlank),
            presionArterial = current.presionArterialInput.takeIf(String::isNotBlank),
            frecuenciaCardiaca = current.frecuenciaCardiacaInput.toIntOrNull(),
            temperatura = current.temperaturaInput.takeIf(String::isNotBlank),
            saturacionO2 = current.saturacionO2Input.toIntOrNull(),
            pesoKg = current.pesoKgInput.takeIf(String::isNotBlank),
            tallaCm = current.tallaCmInput.takeIf(String::isNotBlank),
            notas = current.notasInput.takeIf(String::isNotBlank),
        )

        val errors = AtencionValidator.validate(request)
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    formFieldErrors = errors,
                    message = errors.values.firstOrNull() ?: "Por favor verifica los campos obligatorios.",
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSubmitting = true,
                formFieldErrors = emptyMap(),
                message = null,
            )
        }

        viewModelScope.launch {
            val outcome = repository.createAtencion(token, patientId, request)
            _uiState.update { state ->
                when (outcome) {
                    is PatientAtencionesOutcome.CreateSuccess -> {
                        state.copy(
                            isSubmitting = false,
                            isFormVisible = false,
                            createSuccessMessage = "Atención médica registrada exitosamente.",
                            motivoInput = "",
                            diagnosticoInput = "",
                            tratamientoInput = "",
                            codigoCie10Input = "",
                            presionArterialInput = "",
                            frecuenciaCardiacaInput = "",
                            temperaturaInput = "",
                            saturacionO2Input = "",
                            pesoKgInput = "",
                            tallaCmInput = "",
                            notasInput = "",
                            formFieldErrors = emptyMap(),
                            message = null,
                        )
                    }
                    is PatientAtencionesOutcome.ValidationError -> {
                        state.copy(
                            isSubmitting = false,
                            formFieldErrors = outcome.fieldErrors,
                            message = outcome.fieldErrors.values.firstOrNull() ?: "Error de validación en el formulario.",
                        )
                    }
                    PatientAtencionesOutcome.Unauthorized -> {
                        state.copy(
                            isSubmitting = false,
                            status = PatientAtencionesStatus.Unauthorized,
                            message = "La sesión expiró o no está autorizada. Inicia sesión nuevamente.",
                        )
                    }
                    PatientAtencionesOutcome.Forbidden -> {
                        state.copy(
                            isSubmitting = false,
                            status = PatientAtencionesStatus.Forbidden,
                            message = "Tu cuenta no tiene permiso para registrar atenciones médicas.",
                        )
                    }
                    PatientAtencionesOutcome.NotFound -> {
                        state.copy(
                            isSubmitting = false,
                            status = PatientAtencionesStatus.NotFound,
                            message = "No se encontró el paciente para registrar la atención.",
                        )
                    }
                    PatientAtencionesOutcome.NetworkError -> {
                        state.copy(
                            isSubmitting = false,
                            message = "No se pudo conectar con EssAid. Revisa tu conexión a internet.",
                        )
                    }
                    is PatientAtencionesOutcome.ServerError -> {
                        state.copy(
                            isSubmitting = false,
                            message = "Ocurrió un error en el servidor al guardar la atención. Inténtalo más tarde.",
                        )
                    }
                    is PatientAtencionesOutcome.Success -> state.copy(isSubmitting = false)
                }
            }

            // If creation was successful, refresh list automatically
            if (_uiState.value.createSuccessMessage != null) {
                loadAtenciones(token, patientId)
            }
        }
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
