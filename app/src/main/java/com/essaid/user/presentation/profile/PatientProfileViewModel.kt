package com.essaid.user.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.PatientProfileRepository
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PatientProfileUpdateForm
import com.essaid.user.domain.model.PatientProfileUpdateOutcome
import com.essaid.user.domain.validation.ProfileUpdateValidator
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
    val isEditing: Boolean = false,
    val updateForm: PatientProfileUpdateForm = PatientProfileUpdateForm(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val isUpdating: Boolean = false,
    val successMessage: String? = null,
) {
    fun errorFor(field: String): String? = fieldErrors[field]
}

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

    fun startEditing() {
        _uiState.value.profile?.let { p ->
            _uiState.update {
                it.copy(
                    isEditing = true,
                    successMessage = null,
                    errorMessage = null,
                    fieldErrors = emptyMap(),
                    updateForm = PatientProfileUpdateForm(
                        celular = p.personal.celular,
                        contactoEmergenciaNombre = p.medical.contactoEmergenciaNombre ?: "",
                        contactoEmergenciaTelefono = p.medical.contactoEmergenciaTelefono ?: "",
                        alergias = p.medical.alergias.joinToString(", "),
                        condicionesCronicas = p.medical.condicionesCronicas.joinToString(", "),
                        medicamentosActuales = p.medical.medicamentosActuales.joinToString(", "),
                        observacionesMedicas = p.medical.observacionesMedicas ?: "",
                    )
                )
            }
        }
    }

    fun cancelEditing() {
        _uiState.update {
            it.copy(
                isEditing = false,
                fieldErrors = emptyMap(),
                successMessage = null,
            )
        }
    }

    fun onCelularChanged(value: String) = updateForm { it.copy(celular = value) }
    fun onContactoEmergenciaNombreChanged(value: String) = updateForm { it.copy(contactoEmergenciaNombre = value) }
    fun onContactoEmergenciaTelefonoChanged(value: String) = updateForm { it.copy(contactoEmergenciaTelefono = value) }
    fun onAlergiasChanged(value: String) = updateForm { it.copy(alergias = value) }
    fun onCondicionesCronicasChanged(value: String) = updateForm { it.copy(condicionesCronicas = value) }
    fun onMedicamentosActualesChanged(value: String) = updateForm { it.copy(medicamentosActuales = value) }
    fun onObservacionesMedicasChanged(value: String) = updateForm { it.copy(observacionesMedicas = value) }

    private fun updateForm(update: (PatientProfileUpdateForm) -> PatientProfileUpdateForm) {
        _uiState.update { state ->
            state.copy(
                updateForm = update(state.updateForm),
                fieldErrors = state.fieldErrors - "general",
            )
        }
    }

    fun updateProfile(token: String) {
        val form = _uiState.value.updateForm
        val validationErrors = ProfileUpdateValidator.validate(form)
        if (validationErrors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    fieldErrors = validationErrors,
                    errorMessage = "Corrige los errores en los campos antes de guardar.",
                )
            }
            return
        }

        if (token.isBlank()) {
            _uiState.update {
                it.copy(
                    status = ProfileStatus.Unauthorized,
                    errorMessage = "No hay una sesión válida para actualizar el perfil.",
                )
            }
            return
        }

        _uiState.update { it.copy(isUpdating = true, errorMessage = null, fieldErrors = emptyMap()) }

        viewModelScope.launch {
            when (val outcome = repository.updateProfile(token, form)) {
                is PatientProfileUpdateOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            isEditing = false,
                            profile = outcome.profile,
                            successMessage = outcome.message,
                            errorMessage = null,
                        )
                    }
                }
                is PatientProfileUpdateOutcome.ValidationError -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            fieldErrors = outcome.fieldErrors,
                            errorMessage = outcome.message,
                        )
                    }
                }
                is PatientProfileUpdateOutcome.Unauthorized -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            isEditing = false,
                            status = ProfileStatus.Unauthorized,
                            errorMessage = outcome.message,
                        )
                    }
                }
                is PatientProfileUpdateOutcome.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = outcome.message,
                        )
                    }
                }
                is PatientProfileUpdateOutcome.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
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

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
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
