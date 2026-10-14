package com.essaid.user.data.remote

import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserRegistrationRequest

interface UserRegistrationDataSource {
    suspend fun register(request: UserRegistrationRequest): RegistrationOutcome
}
