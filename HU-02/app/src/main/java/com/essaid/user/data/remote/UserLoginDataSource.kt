package com.essaid.user.data.remote

import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest

interface UserLoginDataSource {
    suspend fun login(request: UserLoginRequest): LoginOutcome
}
