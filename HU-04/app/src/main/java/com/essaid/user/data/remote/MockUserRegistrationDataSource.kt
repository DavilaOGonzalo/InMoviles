package com.essaid.user.data.remote

import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserRegistrationRequest

class MockUserRegistrationDataSource : UserRegistrationDataSource {
    override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome {
        return MockUserDataStore.register(request)
    }
}
