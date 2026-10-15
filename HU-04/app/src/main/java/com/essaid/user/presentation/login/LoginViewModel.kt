package com.essaid.user.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.essaid.user.data.repository.UserLoginRepository
import com.essaid.user.domain.model.LoginField
import com.essaid.user.domain.model.LoginForm
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.validation.LoginValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginStatus {
    Idle,
    Loading,
    Success,
    ValidationError,
    InvalidCredentials,
    InactiveAccount,
    Error,
}

data class LoginUiState(
    val form: LoginForm = LoginForm(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val status: LoginStatus = LoginStatus.Idle,
    val message: String? = null,
    val noticeMessage: String? = null,
) {
    val isLoading: Boolean
        get() = status == LoginStatus.Loading

    fun errorFor(field: LoginField): String? = fieldErrors[field.wireName]
}

class LoginViewModel(
    private val repository: UserLoginRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun prefillEmailAndNotice(email: String, notice: String) {
        _uiState.update {
            it.copy(
                form = LoginForm(email = email, password = ""),
                fieldErrors = emptyMap(),
                status = LoginStatus.Idle,
                message = null,
                noticeMessage = notice,
            )
        }
    }

    fun onEmailChanged(value: String) = updateForm(LoginField.Email) {
        it.copy(email = value)
    }

    fun onPasswordChanged(value: String) = updateForm(LoginField.Password) {
        it.copy(password = value)
    }

    fun submit() {
        val current = _uiState.value
        if (current.isLoading) return

        val validationErrors = LoginValidator.validate(current.form)
        if (validationErrors.isNotEmpty()) {
            _uiState.value = current.copy(
                fieldErrors = validationErrors,
                status = LoginStatus.ValidationError,
                message = "Revisa los campos ingresados.",
            )
            return
        }

        val request = current.form.toRequest()
        // Clear password from form state after creating request to prevent plain text persistence
        _uiState.value = current.copy(
            form = current.form.copy(password = ""),
            fieldErrors = emptyMap(),
            status = LoginStatus.Loading,
            message = null,
        )

        viewModelScope.launch {
            when (val outcome = repository.login(request)) {
                is LoginOutcome.Success -> _uiState.update {
                    LoginUiState(
                        status = LoginStatus.Success,
                        message = outcome.message,
                    )
                }

                is LoginOutcome.InvalidCredentials -> _uiState.update {
                    it.copy(
                        status = LoginStatus.InvalidCredentials,
                        message = outcome.message,
                    )
                }

                is LoginOutcome.InactiveAccount -> _uiState.update {
                    it.copy(
                        status = LoginStatus.InactiveAccount,
                        message = outcome.message,
                    )
                }

                is LoginOutcome.ValidationError -> _uiState.update {
                    it.copy(
                        fieldErrors = flattenErrors(outcome.errors),
                        status = LoginStatus.ValidationError,
                        message = outcome.detail,
                    )
                }

                is LoginOutcome.NetworkError -> _uiState.update {
                    it.copy(status = LoginStatus.Error, message = outcome.message)
                }

                is LoginOutcome.ServerError -> _uiState.update {
                    it.copy(status = LoginStatus.Error, message = outcome.message)
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = LoginUiState()
    }

    private fun updateForm(
        field: LoginField,
        update: (LoginForm) -> LoginForm,
    ) {
        _uiState.update { state ->
            val errors = state.fieldErrors.toMutableMap().apply {
                remove(field.wireName)
            }
            state.copy(
                form = update(state.form),
                fieldErrors = errors,
                status = LoginStatus.Idle,
                message = null,
                noticeMessage = null,
            )
        }
    }

    private fun flattenErrors(errors: Map<String, List<String>>): Map<String, String> =
        errors.mapValues { (_, messages) -> messages.joinToString(" ") }
            .filterValues { it.isNotBlank() }

    class Factory(
        private val repository: UserLoginRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
