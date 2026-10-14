package com.essaid.user.di

import com.essaid.user.BuildConfig
import com.essaid.user.data.remote.DjangoRegistrationHttpClient
import com.essaid.user.data.remote.MockUserRegistrationDataSource
import com.essaid.user.data.remote.UserRegistrationDataSource
import com.essaid.user.data.repository.DefaultUserRegistrationRepository
import com.essaid.user.data.repository.UserRegistrationRepository

object AppContainer {
    val registrationRepository: UserRegistrationRepository by lazy {
        DefaultUserRegistrationRepository(registrationDataSource())
    }

    private fun registrationDataSource(): UserRegistrationDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockUserRegistrationDataSource()
        } else {
            DjangoRegistrationHttpClient(BuildConfig.ESSAID_API_BASE_URL)
        }
}
