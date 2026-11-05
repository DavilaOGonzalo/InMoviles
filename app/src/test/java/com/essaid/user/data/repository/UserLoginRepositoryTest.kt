package com.essaid.user.data.repository

import com.essaid.user.data.remote.UserLoginDataSource
import com.essaid.user.domain.model.AuthenticatedUser
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserLoginRepositoryTest {
    @Test
    fun `stores session on successful login`() = runTest {
        val session = AuthSession(
            token = "token-123",
            user = AuthenticatedUser(email = "test@tecsup.edu.pe")
        )
        val dataSource = FakeDataSource(LoginOutcome.Success(session))
        val repository = DefaultUserLoginRepository(dataSource)

        assertNull(repository.currentSession.value)

        val result = repository.login(UserLoginRequest("test@tecsup.edu.pe", "password123"))

        assert(result is LoginOutcome.Success)
        assertNotNull(repository.currentSession.value)
        assertEquals("token-123", repository.currentSession.value?.token)

        repository.logout()
        assertNull(repository.currentSession.value)
    }

    @Test
    fun `catches IOException and returns NetworkError`() = runTest {
        val dataSource = FakeDataSource(null, throwIo = true)
        val repository = DefaultUserLoginRepository(dataSource)

        val result = repository.login(UserLoginRequest("test@tecsup.edu.pe", "password123"))

        assert(result is LoginOutcome.NetworkError)
    }

    private class FakeDataSource(
        private val outcome: LoginOutcome?,
        private val throwIo: Boolean = false,
    ) : UserLoginDataSource {
        override suspend fun login(request: UserLoginRequest): LoginOutcome {
            if (throwIo) throw IOException("Network down")
            return outcome ?: LoginOutcome.ServerError()
        }
    }
}
