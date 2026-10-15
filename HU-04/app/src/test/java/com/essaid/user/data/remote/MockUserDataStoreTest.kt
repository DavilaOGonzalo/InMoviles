package com.essaid.user.data.remote

import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserLoginRequest
import com.essaid.user.domain.model.UserRegistrationRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockUserDataStoreTest {

    @Before
    fun setUp() {
        MockUserDataStore.clear()
    }

    @Test
    fun `registered user can login and retrieve custom profile`() {
        val registerRequest = UserRegistrationRequest(
            email = "pedro.suarez@tecsup.edu.pe",
            password1 = "ClaveSegura2026!",
            password2 = "ClaveSegura2026!",
            tipoDocumento = "DNI",
            numeroDocumento = "76543210",
            caracterVerificador = "1",
            fechaNacimiento = "2001-05-20",
            celular = "987654321",
            terminosAceptados = true,
            tratamientoDatosAutorizado = true,
        )

        val regOutcome = MockUserDataStore.register(registerRequest)
        assertTrue(regOutcome is RegistrationOutcome.Success)

        val loginOutcome = MockUserDataStore.login(
            UserLoginRequest(
                email = "pedro.suarez@tecsup.edu.pe",
                password = "ClaveSegura2026!",
            )
        )
        assertTrue(loginOutcome is LoginOutcome.Success)
        val session = (loginOutcome as LoginOutcome.Success).session

        val profileOutcome = MockUserDataStore.getProfile(session.token)
        assertTrue(profileOutcome is PatientProfileOutcome.Success)

        val profile = (profileOutcome as PatientProfileOutcome.Success).profile
        assertEquals("pedro.suarez@tecsup.edu.pe", profile.personal.email)
        assertEquals("DNI", profile.personal.tipoDocumento)
        assertEquals("76543210", profile.personal.numeroDocumento)
        assertEquals("987654321", profile.personal.celular)
        assertEquals("2001-05-20", profile.personal.fechaNacimiento)
    }

    @Test
    fun `rejects login with wrong password for registered user`() {
        val registerRequest = UserRegistrationRequest(
            email = "maria@tecsup.edu.pe",
            password1 = "ClaveSegura2026!",
            password2 = "ClaveSegura2026!",
            tipoDocumento = "DNI",
            numeroDocumento = "88776655",
            caracterVerificador = "0",
            fechaNacimiento = "1999-10-10",
            celular = "911222333",
            terminosAceptados = true,
            tratamientoDatosAutorizado = true,
        )

        MockUserDataStore.register(registerRequest)

        val loginOutcome = MockUserDataStore.login(
            UserLoginRequest(
                email = "maria@tecsup.edu.pe",
                password = "WrongPassword!",
            )
        )
        assertTrue(loginOutcome is LoginOutcome.InvalidCredentials)
    }
}
