package com.essaid.user.di

import com.essaid.user.BuildConfig
import com.essaid.user.data.remote.DjangoPatientProfileHttpClient
import com.essaid.user.data.remote.MockPatientProfileDataSource
import com.essaid.user.data.remote.MockUserLoginDataSource
import com.essaid.user.data.remote.MockUserRegistrationDataSource
import com.essaid.user.data.remote.PatientProfileDataSource
import com.essaid.user.data.remote.MockPatientMedicalHistoryDataSource
import com.essaid.user.data.remote.PatientMedicalHistoryDataSource
import com.essaid.user.data.remote.MockPatientAtencionesDataSource
import com.essaid.user.data.remote.PatientAtencionesDataSource
import com.essaid.user.data.remote.MockPatientSearchDataSource
import com.essaid.user.data.remote.PatientSearchDataSource
import com.essaid.user.data.remote.UserLoginDataSource
import com.essaid.user.data.remote.UserRegistrationDataSource
import com.essaid.user.data.repository.DefaultPatientProfileRepository
import com.essaid.user.data.repository.DefaultPatientMedicalHistoryRepository
import com.essaid.user.data.repository.DefaultPatientSearchRepository
import com.essaid.user.data.repository.DefaultPatientAtencionesRepository
import com.essaid.user.data.repository.DefaultUserLoginRepository
import com.essaid.user.data.repository.DefaultUserRegistrationRepository
import com.essaid.user.data.repository.PatientProfileRepository
import com.essaid.user.data.repository.PatientMedicalHistoryRepository
import com.essaid.user.data.repository.PatientAtencionesRepository
import com.essaid.user.data.repository.PatientSearchRepository
import com.essaid.user.data.repository.UserLoginRepository
import com.essaid.user.data.repository.UserRegistrationRepository
import com.essaid.user.data.repository.HospitalServicesRepository
import com.essaid.user.data.repository.CitaRepository
import com.essaid.user.data.repository.HospitalRepository
import com.essaid.user.data.remote.HospitalDataSource
import com.essaid.user.data.remote.MockHospitalDataSource
import com.essaid.user.data.remote.SupabaseHospitalDataSource
import com.essaid.user.data.remote.SupabasePatientSearchDataSource
import com.essaid.user.data.remote.MockHospitalServicesAndCitasDataSource
import com.essaid.user.data.remote.SupabasePatientAtencionesHttpClient
import com.essaid.user.data.remote.SupabaseLoginHttpClient
import com.essaid.user.data.remote.SupabaseRegistrationHttpClient
import com.essaid.user.data.remote.SupabasePatientProfileHttpClient
import com.essaid.user.data.remote.SupabasePatientMedicalHistoryHttpClient

object AppContainer {
    val registrationRepository: UserRegistrationRepository by lazy {
        DefaultUserRegistrationRepository(registrationDataSource())
    }

    val loginRepository: UserLoginRepository by lazy {
        DefaultUserLoginRepository(loginDataSource())
    }

    val patientProfileRepository: PatientProfileRepository by lazy {
        DefaultPatientProfileRepository(patientProfileDataSource())
    }

    val patientSearchRepository: PatientSearchRepository by lazy {
        DefaultPatientSearchRepository(patientSearchDataSource())
    }

    val patientMedicalHistoryRepository: PatientMedicalHistoryRepository by lazy {
        DefaultPatientMedicalHistoryRepository(patientMedicalHistoryDataSource())
    }

    val patientAtencionesRepository: PatientAtencionesRepository by lazy {
        DefaultPatientAtencionesRepository(patientAtencionesDataSource())
    }

    val hospitalServicesRepository: HospitalServicesRepository by lazy {
        HospitalServicesRepository(MockHospitalServicesAndCitasDataSource())
    }

    val citaRepository: CitaRepository by lazy {
        CitaRepository(MockHospitalServicesAndCitasDataSource())
    }

    val hospitalRepository: HospitalRepository by lazy {
        HospitalRepository(hospitalDataSource())
    }

    private fun registrationDataSource(): UserRegistrationDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockUserRegistrationDataSource()
        } else {
            SupabaseRegistrationHttpClient()
        }

    private fun loginDataSource(): UserLoginDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockUserLoginDataSource()
        } else {
            SupabaseLoginHttpClient()
        }

    private fun patientProfileDataSource(): PatientProfileDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockPatientProfileDataSource()
        } else {
            SupabasePatientProfileHttpClient()
        }

    private fun patientSearchDataSource(): PatientSearchDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockPatientSearchDataSource()
        } else {
            SupabasePatientSearchDataSource()
        }

    private fun patientMedicalHistoryDataSource(): PatientMedicalHistoryDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockPatientMedicalHistoryDataSource()
        } else {
            SupabasePatientMedicalHistoryHttpClient()
        }

    private fun patientAtencionesDataSource(): PatientAtencionesDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockPatientAtencionesDataSource()
        } else {
            SupabasePatientAtencionesHttpClient()
        }

    private fun hospitalDataSource(): HospitalDataSource =
        if (BuildConfig.USE_MOCK_API) {
            MockHospitalDataSource()
        } else {
            SupabaseHospitalDataSource()
        }
}
