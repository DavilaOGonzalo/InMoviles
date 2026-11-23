package com.essaid.user.presentation.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.CitaRepository
import com.essaid.user.domain.model.BloqueHorario
import com.essaid.user.domain.model.MisCitasOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CitasUiState(
    val bloques: List<BloqueHorario> = emptyList(),
    val misCitas: MisCitasOutcome = MisCitasOutcome.Empty,
    val isLoading: Boolean = false,
    val message: String? = null,
    val successMessage: String? = null
)

class CitasViewModel(
    private val repository: CitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitasUiState())
    val uiState: StateFlow<CitasUiState> = _uiState.asStateFlow()

    fun cargarBloques(token: String, hospitalId: String, fecha: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val lista = repository.obtenerBloquesHorarios(token, hospitalId, fecha)
            _uiState.value = _uiState.value.copy(bloques = lista, isLoading = false)
        }
    }

    fun reservarCita(token: String, pacienteId: String, hospitalId: String, fecha: String, hora: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val result = repository.reservarCita(token, pacienteId, hospitalId, fecha, hora)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "¡Cita reservada con éxito para el $fecha a las $hora!"
                )
                cargarMisCitas(token, pacienteId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = result.exceptionOrNull()?.message ?: "Error al reservar la cita"
                )
            }
        }
    }

    fun cargarMisCitas(token: String, pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val outcome = repository.obtenerCitasPaciente(token, pacienteId)
            _uiState.value = _uiState.value.copy(misCitas = outcome, isLoading = false)
        }
    }

    fun cancelarCita(token: String, citaId: String, motivo: String, pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val success = repository.cancelarCita(token, citaId, motivo)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    successMessage = "Cita cancelada correctamente."
                )
                cargarMisCitas(token, pacienteId)
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "No se pudo cancelar la cita."
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, successMessage = null)
    }
}
