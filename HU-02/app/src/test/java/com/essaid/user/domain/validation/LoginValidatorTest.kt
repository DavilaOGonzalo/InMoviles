package com.essaid.user.domain.validation

import com.essaid.user.domain.model.LoginField
import com.essaid.user.domain.model.LoginForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidatorTest {
    @Test
    fun `accepts valid login credentials`() {
        val errors = LoginValidator.validate(validForm())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `rejects invalid email and missing password`() {
        val errors = LoginValidator.validate(
            LoginForm(email = "invalid@example.com", password = "")
        )

        assertEquals(
            "Usa un correo que termine en @gmail.com o @tecsup.edu.pe.",
            errors[LoginField.Email.wireName],
        )
        assertEquals(
            "Ingresa tu contraseña.",
            errors[LoginField.Password.wireName],
        )
    }

    private fun validForm() = LoginForm(
        email = "paciente@tecsup.edu.pe",
        password = "ClaveSegura2026!",
    )
}
