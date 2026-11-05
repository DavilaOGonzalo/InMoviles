package com.essaid.user.presentation.registration

import androidx.lifecycle.ViewModel
import com.essaid.user.data.repository.UserRegistrationRepository
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserRegistrationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
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
        val repository = RecordingRepository(RegistrationOutcome.Success())
        val viewModel = RegisterViewModel(repository)

        viewModel.submit()

        assertEquals(RegistrationStatus.ValidationError, viewModel.uiState.value.status)
        assertFalse(repository.called)
        assertEquals(
            "Ingresa tu correo electrónico.",
            viewModel.uiState.value.errorFor(com.essaid.user.domain.model.RegistrationField.Email),
        )
    }

    @Test
    fun `emits loading and then success while clearing passwords`() = runTest {
        val repository = RecordingRepository(
            RegistrationOutcome.Success(message = "Cuenta creada."),
        )
        val viewModel = RegisterViewModel(repository)
        fillValidForm(viewModel)

        viewModel.submit()

        assertEquals(RegistrationStatus.Loading, viewModel.uiState.value.status)
        advanceUntilIdle()

        assertEquals(RegistrationStatus.Success, viewModel.uiState.value.status)
        assertEquals("Cuenta creada.", viewModel.uiState.value.message)
        assertEquals("", viewModel.uiState.value.form.password1)
        assertEquals("", viewModel.uiState.value.form.password2)
        assertEquals("persona@tecsup.edu.pe", repository.received?.email)
        assertEquals("ClaveSegura2026!", repository.received?.password1)
    }

    @Test
    fun `shows duplicate field errors returned by repository`() = runTest {
        val repository = RecordingRepository(
            RegistrationOutcome.Duplicate(
                errors = mapOf("email" to listOf("Ya existe una cuenta con ese correo.")),
            ),
        )
        val viewModel = RegisterViewModel(repository)
        fillValidForm(viewModel)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(RegistrationStatus.Duplicate, viewModel.uiState.value.status)
        assertEquals(
            "Ya existe una cuenta con ese correo.",
            viewModel.uiState.value.errorFor(com.essaid.user.domain.model.RegistrationField.Email),
        )
    }

    @Test
    fun `shows network failure without exposing a password`() = runTest {
        val repository = RecordingRepository(
            RegistrationOutcome.NetworkError("Sin conexión."),
        )
        val viewModel = RegisterViewModel(repository)
        fillValidForm(viewModel)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(RegistrationStatus.Error, viewModel.uiState.value.status)
        assertEquals("Sin conexión.", viewModel.uiState.value.message)
        assertNull(viewModel.uiState.value.fieldErrors["password1"])
    }

    private fun fillValidForm(viewModel: RegisterViewModel) {
        viewModel.onEmailChanged("persona@tecsup.edu.pe")
        viewModel.onPasswordChanged("ClaveSegura2026!")
        viewModel.onPasswordConfirmationChanged("ClaveSegura2026!")
        viewModel.onDocumentTypeChanged("DNI")
        viewModel.onDocumentNumberChanged("12345678")
        viewModel.onVerifierChanged("7")
        viewModel.onBirthDateChanged("1995-04-18")
        viewModel.onPhoneChanged("999888777")
        viewModel.onTermsChanged(true)
        viewModel.onDataProcessingChanged(true)
    }

    private class RecordingRepository(
        private val outcome: RegistrationOutcome,
    ) : UserRegistrationRepository {
        var called: Boolean = false
        var received: UserRegistrationRequest? = null

        override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome {
            called = true
            received = request
            return outcome
        }
    }
}
