package com.essaid.user.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.UserRegistrationRepository
import com.essaid.user.domain.model.RegistrationField
import com.essaid.user.domain.model.RegistrationForm
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.validation.RegistrationValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegistrationStatus {
    Idle,
    Loading,
    Success,
    ValidationError,
    Duplicate,
    Error,
}

data class RegisterUiState(
    val form: RegistrationForm = RegistrationForm(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val status: RegistrationStatus = RegistrationStatus.Idle,
    val message: String? = null,
) {
    val isLoading: Boolean
        get() = status == RegistrationStatus.Loading

    fun errorFor(field: RegistrationField): String? = fieldErrors[field.wireName]
}

class RegisterViewModel(
    private val repository: UserRegistrationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    var lastRegisteredEmail: String = ""
        private set

    fun onEmailChanged(value: String) = updateForm(RegistrationField.Email) {
        it.copy(email = value)
    }

    fun onPasswordChanged(value: String) = updateForm(RegistrationField.Password1) {
        it.copy(password1 = value)
    }

    fun onPasswordConfirmationChanged(value: String) = updateForm(RegistrationField.Password2) {
        it.copy(password2 = value)
    }

    fun onDocumentTypeChanged(value: String) = updateForm(RegistrationField.TipoDocumento) {
        it.copy(
            tipoDocumento = value,
            caracterVerificador = if (value == "CE") "" else it.caracterVerificador,
        )
    }

    fun onDocumentNumberChanged(value: String) = updateForm(RegistrationField.NumeroDocumento) {
        it.copy(numeroDocumento = value)
    }

    fun onVerifierChanged(value: String) = updateForm(RegistrationField.CaracterVerificador) {
        it.copy(caracterVerificador = value)
    }

    fun onBirthDateChanged(value: String) = updateForm(RegistrationField.FechaNacimiento) {
        it.copy(fechaNacimiento = value)
    }

    fun onPhoneChanged(value: String) = updateForm(RegistrationField.Celular) {
        it.copy(celular = value)
    }

    fun onTermsChanged(value: Boolean) = updateBooleanField(
        field = RegistrationField.TerminosAceptados,
        update = { it.copy(terminosAceptados = value) },
    )

    fun onDataProcessingChanged(value: Boolean) = updateBooleanField(
        field = RegistrationField.TratamientoDatosAutorizado,
        update = { it.copy(tratamientoDatosAutorizado = value) },
    )

    fun submit() {
        val current = _uiState.value
        if (current.isLoading) return

        val validationErrors = RegistrationValidator.validate(current.form)
        if (validationErrors.isNotEmpty()) {
            _uiState.value = current.copy(
                fieldErrors = validationErrors,
                status = RegistrationStatus.ValidationError,
                message = "Revisa los campos marcados.",
            )
            return
        }

        val request = current.form.toRequest()
        val submittedEmail = request.email
        _uiState.value = current.copy(
            fieldErrors = emptyMap(),
            status = RegistrationStatus.Loading,
            message = null,
        )

        viewModelScope.launch {
            when (val outcome = repository.register(request)) {
                is RegistrationOutcome.Success -> {
                    lastRegisteredEmail = submittedEmail
                    _uiState.update {
                        RegisterUiState(
                            status = RegistrationStatus.Success,
                            message = outcome.message,
                        )
                    }
                }

                is RegistrationOutcome.ValidationError -> _uiState.update {
                    it.copy(
                        fieldErrors = flattenErrors(outcome.errors),
                        status = RegistrationStatus.ValidationError,
                        message = outcome.detail,
                    )
                }

                is RegistrationOutcome.Duplicate -> _uiState.update {
                    it.copy(
                        fieldErrors = flattenErrors(outcome.errors),
                        status = RegistrationStatus.Duplicate,
                        message = outcome.detail,
                    )
                }

                is RegistrationOutcome.NetworkError -> _uiState.update {
                    it.copy(status = RegistrationStatus.Error, message = outcome.message)
                }

                is RegistrationOutcome.ServerError -> _uiState.update {
                    it.copy(status = RegistrationStatus.Error, message = outcome.message)
                }
            }
        }
    }

    private fun updateForm(
        field: RegistrationField,
        update: (RegistrationForm) -> RegistrationForm,
    ) {
        _uiState.update { state ->
            val errors = state.fieldErrors.toMutableMap().apply {
                remove(field.wireName)
                if (field == RegistrationField.TipoDocumento) {
                    remove(RegistrationField.CaracterVerificador.wireName)
                }
            }
            state.copy(
                form = update(state.form),
                fieldErrors = errors,
                status = RegistrationStatus.Idle,
                message = null,
            )
        }
    }

    private fun updateBooleanField(
        field: RegistrationField,
        update: (RegistrationForm) -> RegistrationForm,
    ) {
        updateForm(field, update)
    }

    private fun flattenErrors(errors: Map<String, List<String>>): Map<String, String> =
        errors.mapValues { (_, messages) -> messages.joinToString(" ") }
            .filterValues { it.isNotBlank() }

    class Factory(
        private val repository: UserRegistrationRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
