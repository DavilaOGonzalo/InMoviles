package com.essaid.user.di

import com.essaid.user.BuildConfig
import com.essaid.user.data.remote.DjangoLoginHttpClient
import com.essaid.user.data.remote.DjangoRegistrationHttpClient
import com.essaid.user.data.remote.MockUserLoginDataSource
import com.essaid.user.data.remote.MockUserRegistrationDataSource
import com.essaid.user.data.remote.UserLoginDataSource
import com.essaid.user.data.remote.UserRegistrationDataSource
import com.essaid.user.data.repository.DefaultUserLoginRepository
import com.essaid.user.data.repository.DefaultUserRegistrationRepository
import com.essaid.user.data.repository.UserLoginRepository
import com.essaid.user.data.repository.UserRegistrationRepository

object AppContainer {
    val registrationRepository: UserRegistrationRepository by lazy {
        DefaultUserRegistrationRepository(registrationDataSource())
    }

    val loginRepository: UserLoginRepository by lazy {
        DefaultUserLoginRepository(loginDataSource())
    }

    private fun registrationDataSource(): UserRegistrationDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockUserRegistrationDataSource()
        } else {
            DjangoRegistrationHttpClient(BuildConfig.ESSAID_API_BASE_URL)
        }

    private fun loginDataSource(): UserLoginDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockUserLoginDataSource()
        } else {
            DjangoLoginHttpClient(BuildConfig.ESSAID_API_BASE_URL)
        }
}
