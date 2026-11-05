package com.essaid.user.data.remote

import com.essaid.user.domain.model.AuthenticatedUser
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest

class MockUserLoginDataSource : UserLoginDataSource {
    override suspend fun login(request: UserLoginRequest): LoginOutcome {
        val email = request.email.trim().lowercase()
        val role = when (email) {
            "medico.prueba@tecsup.edu.pe", "medico.demo@tecsup.edu.pe" -> "medico"
            "paciente.prueba@tecsup.edu.pe", "paciente.demo@tecsup.edu.pe" -> "paciente"
            else -> return LoginOutcome.InvalidCredentials()
        }
        if (request.password != DEMO_PASSWORD) {
            return LoginOutcome.InvalidCredentials()
        }

        return LoginOutcome.Success(
            session = AuthSession(
                token = "mock-jwt-token-essaid-${System.currentTimeMillis()}",
                user = AuthenticatedUser(
                    email = email,
                    name = if (role == "medico") "Valeria Rojas Medina" else "Juan Carlos Pérez Gómez",
                    role = role,
                ),
            )
        )
    }

    private companion object {
        const val DEMO_PASSWORD = "EssAidTest!2026"
    }
}
