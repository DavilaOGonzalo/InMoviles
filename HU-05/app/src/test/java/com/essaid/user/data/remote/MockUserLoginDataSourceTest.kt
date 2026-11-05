package com.essaid.user.data.remote

import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockUserLoginDataSourceTest {
    private val dataSource = MockUserLoginDataSource()

    @Test
    fun `test doctor account receives doctor role in mock mode`() = runTest {
        val result = dataSource.login(
            UserLoginRequest("medico.prueba@tecsup.edu.pe", "EssAidTest!2026"),
        )

        assertTrue(result is LoginOutcome.Success)
        assertEquals("medico", (result as LoginOutcome.Success).session.user.role)
    }

    @Test
    fun `test patient account receives patient role in mock mode`() = runTest {
        val result = dataSource.login(
            UserLoginRequest("paciente.prueba@tecsup.edu.pe", "EssAidTest!2026"),
        )

        assertTrue(result is LoginOutcome.Success)
        assertEquals("paciente", (result as LoginOutcome.Success).session.user.role)
    }

    @Test
    fun `rejects incorrect password for demo account`() = runTest {
        val result = dataSource.login(
            UserLoginRequest("medico.demo@tecsup.edu.pe", "wrong-password"),
        )

        assertTrue(result is LoginOutcome.InvalidCredentials)
    }

    @Test
    fun `rejects unknown email even with demo password`() = runTest {
        val result = dataSource.login(
            UserLoginRequest("unknown@tecsup.edu.pe", "EssAidTest!2026"),
        )

        assertTrue(result is LoginOutcome.InvalidCredentials)
    }
}
