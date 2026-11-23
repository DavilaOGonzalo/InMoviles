package com.essaid.user.presentation.hospital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.HospitalRepository
import com.essaid.user.domain.model.Hospital
import com.essaid.user.domain.model.HospitalListOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HospitalUiState {
    data object Idle : HospitalUiState
    data object Loading : HospitalUiState
    data class Success(val hospitales: List<Hospital>) : HospitalUiState
    data object Empty : HospitalUiState
    data object Unauthorized : HospitalUiState
    data class Error(val message: String) : HospitalUiState
}

class HospitalViewModel(
    private val repository: HospitalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HospitalUiState>(HospitalUiState.Idle)
    val uiState: StateFlow<HospitalUiState> = _uiState.asStateFlow()

    fun cargarHospitales(token: String) {
        viewModelScope.launch {
            _uiState.value = HospitalUiState.Loading
            when (val outcome = repository.obtenerHospitales(token)) {
                is HospitalListOutcome.Success -> {
                    _uiState.value = HospitalUiState.Success(outcome.hospitales)
                }
                is HospitalListOutcome.Empty -> {
                    _uiState.value = HospitalUiState.Empty
                }
                is HospitalListOutcome.Unauthorized -> {
                    _uiState.value = HospitalUiState.Unauthorized
                }
                is HospitalListOutcome.NetworkError -> {
                    _uiState.value = HospitalUiState.Error("Error de red: ${outcome.message}")
                }
                is HospitalListOutcome.ServerError -> {
                    _uiState.value = HospitalUiState.Error("Error ${outcome.code}: ${outcome.message}")
                }
            }
        }
    }
}
