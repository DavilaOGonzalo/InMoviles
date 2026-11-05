package com.essaid.user.data.remote

import com.essaid.user.domain.model.AuthenticatedUser
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest

class MockUserLoginDataSource : UserLoginDataSource {
    override suspend fun login(request: UserLoginRequest): LoginOutcome {
        val email = request.email.lowercase()
        return when {
            request.password == "Inactivo123!" -> LoginOutcome.InactiveAccount()
            request.password.length < 8 || request.password == "WrongPassword1!" -> LoginOutcome.InvalidCredentials()
            else -> LoginOutcome.Success(
                session = AuthSession(
                    token = "mock-jwt-token-essaid-${System.currentTimeMillis()}",
                    user = AuthenticatedUser(email = request.email, name = "Paciente EssAid", role = "PATIENT")
                )
            )
        }
    }
}
