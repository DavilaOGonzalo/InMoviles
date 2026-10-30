package com.essaid.user.data.repository

import com.essaid.user.data.remote.UserRegistrationDataSource
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserRegistrationRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

interface UserRegistrationRepository {
    suspend fun register(request: UserRegistrationRequest): RegistrationOutcome
}

class DefaultUserRegistrationRepository(
    private val dataSource: UserRegistrationDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UserRegistrationRepository {
    override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome =
        withContext(ioDispatcher) {
            try {
                dataSource.register(request)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                RegistrationOutcome.NetworkError()
            } catch (_: Exception) {
                RegistrationOutcome.ServerError()
            }
        }
}
