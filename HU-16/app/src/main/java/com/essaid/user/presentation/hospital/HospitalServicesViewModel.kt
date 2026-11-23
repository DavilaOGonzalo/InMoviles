package com.essaid.user.presentation.hospital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.HospitalServicesRepository
import com.essaid.user.data.repository.ServicioListOutcome
import com.essaid.user.domain.model.ServicioMedico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HospitalServicesUiState {
    data object Idle : HospitalServicesUiState
    data object Loading : HospitalServicesUiState
    data class Success(val servicios: List<ServicioMedico>) : HospitalServicesUiState
    data object Empty : HospitalServicesUiState
    data class Error(val message: String) : HospitalServicesUiState
}

class HospitalServicesViewModel(
    private val repository: HospitalServicesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HospitalServicesUiState>(HospitalServicesUiState.Idle)
    val uiState: StateFlow<HospitalServicesUiState> = _uiState.asStateFlow()

    fun cargarServicios(token: String, hospitalId: String) {
        viewModelScope.launch {
            _uiState.value = HospitalServicesUiState.Loading
            when (val outcome = repository.obtenerServicios(token, hospitalId)) {
                is ServicioListOutcome.Success -> {
                    _uiState.value = HospitalServicesUiState.Success(outcome.servicios)
                }
                is ServicioListOutcome.Empty -> {
                    _uiState.value = HospitalServicesUiState.Empty
                }
                is ServicioListOutcome.Error -> {
                    _uiState.value = HospitalServicesUiState.Error(outcome.message)
                }
            }
        }
    }

    fun toggleDisponibilidad(token: String, servicioId: String, hospitalId: String, nuevoEstado: Boolean) {
        viewModelScope.launch {
            repository.actualizarDisponibilidad(token, servicioId, nuevoEstado)
            cargarServicios(token, hospitalId)
        }
    }
}
