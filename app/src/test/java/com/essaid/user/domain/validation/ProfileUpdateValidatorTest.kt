package com.essaid.user.domain.validation

import com.essaid.user.domain.model.PatientProfileUpdateForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUpdateValidatorTest {
    @Test
    fun `accepts valid update form`() {
        val form = PatientProfileUpdateForm(
            celular = "987654321",
            contactoEmergenciaNombre = "María",
            contactoEmergenciaTelefono = "912345678",
            alergias = "Penicilina",
        )
        val errors = ProfileUpdateValidator.validate(form)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `rejects empty or invalid celular`() {
        val form1 = PatientProfileUpdateForm(celular = "")
        val errors1 = ProfileUpdateValidator.validate(form1)
        assertEquals("El número de celular es obligatorio.", errors1["celular"])

        val form2 = PatientProfileUpdateForm(celular = "12345")
        val errors2 = ProfileUpdateValidator.validate(form2)
        assertEquals("El celular debe tener 9 dígitos y empezar con 9.", errors2["celular"])
    }

    @Test
    fun `rejects invalid emergency phone`() {
        val form = PatientProfileUpdateForm(
            celular = "987654321",
            contactoEmergenciaTelefono = "12345",
        )
        val errors = ProfileUpdateValidator.validate(form)
        assertEquals("El teléfono de emergencia debe tener 9 dígitos y empezar con 9.", errors["contactoEmergenciaTelefono"])
    }
}
