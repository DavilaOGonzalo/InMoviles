package com.essaid.user.data.repository

import com.essaid.user.data.remote.UserLoginDataSource
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException

interface UserLoginRepository {
    val currentSession: StateFlow<AuthSession?>
    suspend fun login(request: UserLoginRequest): LoginOutcome
    fun logout()
}

class DefaultUserLoginRepository(
    private val dataSource: UserLoginDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserLoginRepository {
    private val _currentSession = MutableStateFlow<AuthSession?>(null)
    override val currentSession: StateFlow<AuthSession?> = _currentSession.asStateFlow()

    override suspend fun login(request: UserLoginRequest): LoginOutcome =
        withContext(ioDispatcher) {
            try {
                val outcome = dataSource.login(request)
                if (outcome is LoginOutcome.Success) {
                    _currentSession.value = outcome.session
                }
                outcome
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                LoginOutcome.NetworkError()
            } catch (_: Exception) {
                LoginOutcome.ServerError()
            }
        }

    override fun logout() {
        _currentSession.value = null
    }
}
