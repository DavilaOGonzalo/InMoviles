package com.essaid.user.data.remote

import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PatientProfileUpdateForm
import com.essaid.user.domain.model.PatientProfileUpdateOutcome
import com.essaid.user.domain.model.PersonalPatientInfo
import com.essaid.user.domain.validation.ProfileUpdateValidator
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

interface PatientProfileDataSource {
    suspend fun getProfile(token: String): PatientProfileOutcome
    suspend fun updateProfile(token: String, form: PatientProfileUpdateForm): PatientProfileUpdateOutcome
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

    override suspend fun updateProfile(token: String, form: PatientProfileUpdateForm): PatientProfileUpdateOutcome {
        if (token.isBlank() || token == "invalid-token") {
            return PatientProfileUpdateOutcome.Unauthorized()
        }
        val errors = ProfileUpdateValidator.validate(form)
        if (errors.isNotEmpty()) {
            return PatientProfileUpdateOutcome.ValidationError(fieldErrors = errors)
        }
        val updatedProfile = PatientProfile(
            personal = PersonalPatientInfo(
                id = 101L,
                email = "paciente@tecsup.edu.pe",
                nombre = "Juan Carlos",
                apellido = "Pérez Gómez",
                tipoDocumento = "DNI",
                numeroDocumento = "72345678",
                celular = form.celular.trim(),
                fechaNacimiento = "15/05/2000",
                genero = "Masculino"
            ),
            medical = MedicalPatientInfo(
                tipoSangre = "O+",
                alergias = form.toMedicalList(form.alergias),
                condicionesCronicas = form.toMedicalList(form.condicionesCronicas),
                medicamentosActuales = form.toMedicalList(form.medicamentosActuales),
                contactoEmergenciaNombre = form.contactoEmergenciaNombre.ifBlank { null },
                contactoEmergenciaTelefono = form.contactoEmergenciaTelefono.ifBlank { null },
                observacionesMedicas = form.observacionesMedicas.ifBlank { null },
                ultimaActualizacion = "2026-03-02"
            )
        )
        return PatientProfileUpdateOutcome.Success(updatedProfile)
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

    override suspend fun updateProfile(token: String, form: PatientProfileUpdateForm): PatientProfileUpdateOutcome {
        val path = "/api/patient/profile/"
        val url = URL("$baseUrl$path")
        val conn = connectionFactory(url).apply {
            requestMethod = "PUT"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            val jsonBody = """
                {
                  "celular": "${form.celular}",
                  "contacto_emergencia_nombre": "${form.contactoEmergenciaNombre}",
                  "contacto_emergencia_telefono": "${form.contactoEmergenciaTelefono}",
                  "alergias": ${form.toMedicalList(form.alergias).joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"") { it }},
                  "condiciones_cronicas": ${form.toMedicalList(form.condicionesCronicas).joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"") { it }},
                  "medicamentos_actuales": ${form.toMedicalList(form.medicamentosActuales).joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"") { it }},
                  "observaciones_medicas": "${form.observacionesMedicas}"
                }
            """.trimIndent()

            conn.outputStream.use { os ->
                os.write(jsonBody.toByteArray(Charsets.UTF_8))
            }

            val statusCode = conn.responseCode
            when (statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_ACCEPTED -> {
                    val profile = PatientProfile(
                        personal = PersonalPatientInfo(
                            email = "paciente@tecsup.edu.pe",
                            nombre = "Paciente",
                            apellido = "EssAid",
                            tipoDocumento = "DNI",
                            numeroDocumento = "00000000",
                            celular = form.celular,
                            fechaNacimiento = "01/01/2000"
                        ),
                        medical = MedicalPatientInfo(
                            tipoSangre = "O+",
                            alergias = form.toMedicalList(form.alergias),
                            condicionesCronicas = form.toMedicalList(form.condicionesCronicas),
                            medicamentosActuales = form.toMedicalList(form.medicamentosActuales),
                            contactoEmergenciaNombre = form.contactoEmergenciaNombre,
                            contactoEmergenciaTelefono = form.contactoEmergenciaTelefono,
                            observacionesMedicas = form.observacionesMedicas,
                            ultimaActualizacion = "2026-03-02"
                        )
                    )
                    PatientProfileUpdateOutcome.Success(profile)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN -> PatientProfileUpdateOutcome.Unauthorized()
                400 -> PatientProfileUpdateOutcome.ValidationError()
                else -> PatientProfileUpdateOutcome.ServerError(statusCode = statusCode)
            }
        } catch (_: IOException) {
            PatientProfileUpdateOutcome.NetworkError()
        } catch (_: Exception) {
            PatientProfileUpdateOutcome.ServerError()
        } finally {
            conn.disconnect()
        }
    }
}
