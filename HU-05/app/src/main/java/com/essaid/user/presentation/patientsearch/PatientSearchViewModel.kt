package com.essaid.user.presentation.patientsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.PatientSearchRepository
import com.essaid.user.domain.model.PatientSearchResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

enum class PatientSearchStatus {
    Idle,
    ValidationError,
    Loading,
    Success,
    Empty,
    Unauthorized,
    Forbidden,
    NetworkError,
    ServerError,
}

data class PatientSearchUiState(
    val query: String = "",
    val status: PatientSearchStatus = PatientSearchStatus.Idle,
    val patients: List<PatientSearchResult> = emptyList(),
    val selectedPatient: PatientSearchResult? = null,
    val message: String? = null,
)

class PatientSearchViewModel(
    private val repository: PatientSearchRepository,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PatientSearchUiState())
    val uiState: StateFlow<PatientSearchUiState> = _uiState.asStateFlow()
    private val searchRequests = MutableStateFlow(SearchRequest())

    init {
        observeQueryChanges()
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            status = PatientSearchStatus.Idle,
            patients = emptyList(),
            selectedPatient = null,
            message = null,
        )
        searchRequests.value = searchRequests.value.copy(query = query)
    }

    fun setSessionToken(token: String) {
        searchRequests.value = searchRequests.value.copy(token = token)
    }

    fun retry(token: String) {
        viewModelScope.launch { search(token, _uiState.value.query) }
    }

    fun selectPatient(patient: PatientSearchResult) {
        _uiState.value = _uiState.value.copy(selectedPatient = patient)
    }

    fun returnToResults() {
        _uiState.value = _uiState.value.copy(selectedPatient = null)
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryChanges() {
        viewModelScope.launch {
            searchRequests
                .debounce(debounceMillis)
                .collectLatest { request -> search(request.token, request.query) }
        }
    }

    private suspend fun search(token: String, rawQuery: String) {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(status = PatientSearchStatus.Idle)
            return
        }

        val validationMessage = when {
            query.all(Char::isDigit) && query.length != DNI_LENGTH ->
                "Para buscar por DNI, ingresa los $DNI_LENGTH dígitos."
            query.any { !it.isDigit() } && query.length < MIN_NAME_QUERY_LENGTH ->
                "Ingresa al menos $MIN_NAME_QUERY_LENGTH caracteres del nombre o apellido."
            else -> null
        }
        if (validationMessage != null) {
            _uiState.value = _uiState.value.copy(
                status = PatientSearchStatus.ValidationError,
                patients = emptyList(),
                message = validationMessage,
            )
            return
        }

        if (token.isBlank()) {
            _uiState.value = _uiState.value.copy(
                status = PatientSearchStatus.Unauthorized,
                patients = emptyList(),
                message = "No hay una sesión válida para buscar pacientes.",
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            status = PatientSearchStatus.Loading,
            patients = emptyList(),
            selectedPatient = null,
            message = null,
        )
        when (val outcome = repository.searchPatients(token, query)) {
            is com.essaid.user.domain.model.PatientSearchOutcome.Success -> {
                _uiState.value = _uiState.value.copy(
                    status = if (outcome.patients.isEmpty()) PatientSearchStatus.Empty else PatientSearchStatus.Success,
                    patients = outcome.patients,
                    message = null,
                )
            }
            com.essaid.user.domain.model.PatientSearchOutcome.Unauthorized -> {
                _uiState.value = _uiState.value.copy(
                    status = PatientSearchStatus.Unauthorized,
                    message = "La sesión expiró o no está autorizada. Inicia sesión nuevamente.",
                )
            }
            com.essaid.user.domain.model.PatientSearchOutcome.Forbidden -> {
                _uiState.value = _uiState.value.copy(
                    status = PatientSearchStatus.Forbidden,
                    message = "Tu cuenta no tiene permiso para buscar pacientes.",
                )
            }
            com.essaid.user.domain.model.PatientSearchOutcome.NetworkError -> {
                _uiState.value = _uiState.value.copy(
                    status = PatientSearchStatus.NetworkError,
                    message = "No se pudo conectar con EssAid. Revisa tu conexión.",
                )
            }
            is com.essaid.user.domain.model.PatientSearchOutcome.ServerError -> {
                _uiState.value = _uiState.value.copy(
                    status = PatientSearchStatus.ServerError,
                    message = "No se pudo completar la búsqueda. Inténtalo más tarde.",
                )
            }
        }
    }

    companion object {
        const val DEFAULT_DEBOUNCE_MILLIS = 350L
        private const val DNI_LENGTH = 8
        private const val MIN_NAME_QUERY_LENGTH = 2

        fun Factory(
            repository: PatientSearchRepository,
            debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (!modelClass.isAssignableFrom(PatientSearchViewModel::class.java)) {
                    throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
                return PatientSearchViewModel(repository, debounceMillis) as T
            }
        }

        private data class SearchRequest(
            val query: String = "",
            val token: String = "",
        )
    }
}
