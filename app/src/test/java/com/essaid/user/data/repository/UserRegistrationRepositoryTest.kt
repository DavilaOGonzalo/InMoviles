package com.essaid.user.data.repository

import com.essaid.user.data.remote.UserRegistrationDataSource
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserRegistrationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserRegistrationRepositoryTest {
    @Test
    fun `maps IO exception to network error`() = runTest {
        val repository = DefaultUserRegistrationRepository(
            dataSource = object : UserRegistrationDataSource {
                override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome {
                    throw IOException("offline")
                }
            },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val result = repository.register(request())

        assertEquals(RegistrationOutcome.NetworkError(), result)
    }

    @Test
    fun `keeps remote duplicate outcome`() = runTest {
        val duplicate = RegistrationOutcome.Duplicate(
            errors = mapOf("numero_documento" to listOf("Duplicado")),
        )
        val repository = DefaultUserRegistrationRepository(
            dataSource = object : UserRegistrationDataSource {
                override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome = duplicate
            },
            ioDispatcher = Dispatchers.Unconfined,
        )

        val result = repository.register(request())

        assertEquals(duplicate, result)
    }

    private fun request() = UserRegistrationRequest(
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
