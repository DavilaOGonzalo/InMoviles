package com.essaid.user.presentation.login

import com.essaid.user.data.repository.UserLoginRepository
import com.essaid.user.domain.model.AuthenticatedUser
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.LoginField
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows local validation errors without calling repository`() {
        val repository = RecordingRepository(LoginOutcome.Success(AuthSession("t", AuthenticatedUser(email = "e"))))
        val viewModel = LoginViewModel(repository)

        viewModel.submit()

        assertEquals(LoginStatus.ValidationError, viewModel.uiState.value.status)
        assertFalse(repository.called)
        assertEquals(
            "Ingresa tu correo electrónico.",
            viewModel.uiState.value.errorFor(LoginField.Email),
        )
    }

    @Test
    fun `emits loading and success while clearing password`() = runTest {
        val repository = RecordingRepository(
            LoginOutcome.Success(AuthSession("token-abc", AuthenticatedUser(email = "test@tecsup.edu.pe")))
        )
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChanged("test@tecsup.edu.pe")
        viewModel.onPasswordChanged("ClaveSegura2026!")

        viewModel.submit()

        assertEquals(LoginStatus.Loading, viewModel.uiState.value.status)
        advanceUntilIdle()

        assertEquals(LoginStatus.Success, viewModel.uiState.value.status)
        assertEquals("", viewModel.uiState.value.form.password)
        assertEquals("test@tecsup.edu.pe", repository.received?.email)
        assertEquals("ClaveSegura2026!", repository.received?.password)
    }

    @Test
    fun `handles invalid credentials outcome`() = runTest {
        val repository = RecordingRepository(LoginOutcome.InvalidCredentials())
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChanged("test@tecsup.edu.pe")
        viewModel.onPasswordChanged("WrongPassword")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(LoginStatus.InvalidCredentials, viewModel.uiState.value.status)
    }

    @Test
    fun `handles inactive account outcome`() = runTest {
        val repository = RecordingRepository(LoginOutcome.InactiveAccount())
        val viewModel = LoginViewModel(repository)
        viewModel.onEmailChanged("test@tecsup.edu.pe")
        viewModel.onPasswordChanged("Inactivo123!")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(LoginStatus.InactiveAccount, viewModel.uiState.value.status)
    }

    private class RecordingRepository(
        private val outcome: LoginOutcome,
    ) : UserLoginRepository {
        private val _session = MutableStateFlow<AuthSession?>(null)
        override val currentSession: StateFlow<AuthSession?> = _session.asStateFlow()

        var called: Boolean = false
        var received: UserLoginRequest? = null

        override suspend fun login(request: UserLoginRequest): LoginOutcome {
            called = true
            received = request
            if (outcome is LoginOutcome.Success) {
                _session.value = outcome.session
            }
            return outcome
        }

        override fun logout() {
            _session.value = null
        }
    }
}
