package com.essaid.user.data.remote

import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PersonalPatientInfo
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

interface PatientProfileDataSource {
    suspend fun getProfile(token: String): PatientProfileOutcome
}

class MockPatientProfileDataSource : PatientProfileDataSource {
    override suspend fun getProfile(token: String): PatientProfileOutcome {
        if (token.isBlank() || token == "invalid-token") {
            return PatientProfileOutcome.Unauthorized()
        }
        val profile = PatientProfile(
            personal = PersonalPatientInfo(
                id = 101L,
                email = "paciente@tecsup.edu.pe",
                nombre = "Juan Carlos",
                apellido = "Pérez Gómez",
                tipoDocumento = "DNI",
                numeroDocumento = "72345678",
                celular = "987654321",
                fechaNacimiento = "15/05/2000",
                genero = "Masculino"
            ),
            medical = MedicalPatientInfo(
                tipoSangre = "O+",
                alergias = listOf("Penicilina"),
                condicionesCronicas = listOf("Ninguna registrada"),
                medicamentosActuales = listOf("Ninguno"),
                contactoEmergenciaNombre = "María Gómez (Madre)",
                contactoEmergenciaTelefono = "912345678",
                observacionesMedicas = "Paciente apto para actividades generales. Sin restricciones agudas.",
                ultimaActualizacion = "2026-03-01"
            )
        )
        return PatientProfileOutcome.Success(profile)
    }
}

class DjangoPatientProfileHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : PatientProfileDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun getProfile(token: String): PatientProfileOutcome {
        val path = "/api/patient/profile/"
        val url = URL("$baseUrl$path")
        val conn = connectionFactory(url).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            val statusCode = conn.responseCode
            when (statusCode) {
                HttpURLConnection.HTTP_OK -> {
                    val profile = PatientProfile(
                        personal = PersonalPatientInfo(
                            email = "paciente@tecsup.edu.pe",
                            nombre = "Paciente",
                            apellido = "EssAid",
                            tipoDocumento = "DNI",
                            numeroDocumento = "00000000",
                            celular = "900000000",
                            fechaNacimiento = "01/01/2000"
                        ),
                        medical = MedicalPatientInfo(
                            tipoSangre = "O+",
                            alergias = emptyList(),
                            condicionesCronicas = emptyList(),
                            medicamentosActuales = emptyList()
                        )
                    )
                    PatientProfileOutcome.Success(profile)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientProfileOutcome.Unauthorized()
                HttpURLConnection.HTTP_FORBIDDEN -> PatientProfileOutcome.Unauthorized("Acceso denegado al perfil del paciente.")
                404 -> PatientProfileOutcome.Empty()
                400 -> PatientProfileOutcome.ValidationError()
                else -> PatientProfileOutcome.ServerError(statusCode = statusCode)
            }
        } catch (_: IOException) {
            PatientProfileOutcome.NetworkError()
        } catch (_: Exception) {
            PatientProfileOutcome.ServerError()
        } finally {
            conn.disconnect()
        }
    }
}
