package com.essaid.user.domain.validation

import com.essaid.user.domain.model.RegistrationField
import com.essaid.user.domain.model.RegistrationForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationValidatorTest {
    @Test
    fun `accepts a complete DNI registration`() {
        val errors = RegistrationValidator.validate(validForm())

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `rejects email outside the agreed domains and missing consents`() {
        val errors = RegistrationValidator.validate(
            validForm().copy(
                email = "persona@example.com",
                terminosAceptados = false,
                tratamientoDatosAutorizado = false,
            ),
        )

        assertEquals(
            "Usa un correo que termine en @gmail.com o @tecsup.edu.pe.",
            errors[RegistrationField.Email.wireName],
        )
        assertTrue(RegistrationField.TerminosAceptados.wireName in errors)
        assertTrue(RegistrationField.TratamientoDatosAutorizado.wireName in errors)
    }

    @Test
    fun `requires DNI verifier and matching strong password`() {
        val errors = RegistrationValidator.validate(
            validForm().copy(
                password1 = "12345678",
                password2 = "different",
                caracterVerificador = "",
            ),
        )

        assertEquals(
            "La contraseña no puede estar formada solo por números.",
            errors[RegistrationField.Password1.wireName],
        )
        assertEquals(
            "Las contraseñas no coinciden.",
            errors[RegistrationField.Password2.wireName],
        )
        assertEquals(
            "Ingresa el carácter verificador del DNI.",
            errors[RegistrationField.CaracterVerificador.wireName],
        )
    }

    @Test
    fun `does not allow a verifier for CE`() {
        val errors = RegistrationValidator.validate(
            validForm().copy(
                tipoDocumento = "CE",
                caracterVerificador = "X",
            ),
        )

        assertEquals(
            "El CE no lleva carácter verificador.",
            errors[RegistrationField.CaracterVerificador.wireName],
        )
    }

    private fun validForm() = RegistrationForm(
        email = "persona@tecsup.edu.pe",
        password1 = "ClaveSegura2026!",
        password2 = "ClaveSegura2026!",
        tipoDocumento = "DNI",
        numeroDocumento = "12345678",
        caracterVerificador = "7",
        fechaNacimiento = "1995-04-18",
        celular = "999888777",
        terminosAceptados = true,
        tratamientoDatosAutorizado = true,
    )
}
