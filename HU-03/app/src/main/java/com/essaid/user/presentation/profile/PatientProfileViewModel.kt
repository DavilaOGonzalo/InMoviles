package com.essaid.user.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.PatientProfileRepository
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProfileStatus {
    Idle,
    Loading,
    Success,
    Empty,
    Error,
    Unauthorized,
}

data class PatientProfileUiState(
    val status: ProfileStatus = ProfileStatus.Idle,
    val profile: PatientProfile? = null,
    val errorMessage: String? = null,
    val isDemoMode: Boolean = false,
)

class PatientProfileViewModel(
    private val repository: PatientProfileRepository,
    val isDemoMode: Boolean = false,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PatientProfileUiState(isDemoMode = isDemoMode))
    val uiState: StateFlow<PatientProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(token: String) {
        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    status = ProfileStatus.Unauthorized,
                    errorMessage = "No hay una sesión válida para consultar el perfil.",
                )
            }
            return
        }

        _uiState.update { it.copy(status = ProfileStatus.Loading, errorMessage = null) }

        viewModelScope.launch {
            when (val outcome = repository.getProfile(token)) {
                is PatientProfileOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            status = ProfileStatus.Success,
                            profile = outcome.profile,
                            errorMessage = null,
                        )
                    }
                }
                is PatientProfileOutcome.Empty -> {
                    _uiState.update {
                        it.copy(
                            status = ProfileStatus.Empty,
                            profile = null,
                            errorMessage = outcome.message,
                        )
                    }
                }
                is PatientProfileOutcome.Unauthorized -> {
                    _uiState.update {
                        it.copy(
                            status = ProfileStatus.Unauthorized,
                            profile = null,
                            errorMessage = outcome.message,
                        )
                    }
                }
                is PatientProfileOutcome.ValidationError -> {
                    _uiState.update {
                        it.copy(
                            status = ProfileStatus.Error,
                            errorMessage = outcome.message,
                        )
                    }
                }
                is PatientProfileOutcome.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            status = ProfileStatus.Error,
                            errorMessage = outcome.message,
                        )
                    }
                }
                is PatientProfileOutcome.ServerError -> {
                    _uiState.update {
                        it.copy(
                            status = ProfileStatus.Error,
                            errorMessage = outcome.message,
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun Factory(repository: PatientProfileRepository, isDemoMode: Boolean = false): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PatientProfileViewModel(repository, isDemoMode) as T
                }
            }
    }
}
