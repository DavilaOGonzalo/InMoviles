package com.essaid.user.data.remote

import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest

class MockUserLoginDataSource : UserLoginDataSource {
    override suspend fun login(request: UserLoginRequest): LoginOutcome {
        return MockUserDataStore.login(request)
    }
}
