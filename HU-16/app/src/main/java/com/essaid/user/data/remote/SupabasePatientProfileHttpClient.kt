package com.essaid.user.data.remote

import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PatientProfileUpdateForm
import com.essaid.user.domain.model.PatientProfileUpdateOutcome
import com.essaid.user.domain.model.PersonalPatientInfo
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SupabasePatientProfileHttpClient(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val apiKey: String = SupabaseConfig.ANON_KEY,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) : PatientProfileDataSource {

    override suspend fun getProfile(token: String): PatientProfileOutcome {
        val urlString = "$baseUrl/rest/v1/profiles?limit=1"
        val connection = connectionFactory(URL(urlString)).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer ${if (token.isNotBlank()) token else apiKey}")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            when (val statusCode = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    val jsonArray = JSONArray(body)
                    if (jsonArray.length() > 0) {
                        val obj = jsonArray.getJSONObject(0)
                        val personal = PersonalPatientInfo(
                            id = obj.optLong("id_num", 1L),
                            email = obj.optString("email", "paciente@essaid.test"),
                            nombre = obj.optString("nombre", "Juan Carlos"),
                            apellido = obj.optString("apellido", "Pérez Gómez"),
                            tipoDocumento = obj.optString("tipo_documento", "DNI"),
                            numeroDocumento = obj.optString("numero_documento", "88000000"),
                            celular = obj.optString("celular", "987654321"),
                            fechaNacimiento = obj.optString("fecha_nacimiento", "2000-05-15"),
                            genero = obj.optString("genero", "Masculino"),
                        )
                        val medical = MedicalPatientInfo(
                            tipoSangre = "O+",
                            alergias = listOf("Ninguna"),
                            condicionesCronicas = listOf("Ninguna registrada"),
                            medicamentosActuales = listOf("Ninguno"),
                            contactoEmergenciaNombre = "Marisol Pérez",
                            contactoEmergenciaTelefono = "912345678",
                            observacionesMedicas = "Registrado y sincronizado en Supabase.",
                            ultimaActualizacion = "2026-03-01",
                        )
                        PatientProfileOutcome.Success(PatientProfile(personal, medical))
                    } else {
                        PatientProfileOutcome.Empty()
                    }
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientProfileOutcome.Unauthorized()
                else -> PatientProfileOutcome.ServerError(statusCode = statusCode)
            }
        } catch (_: IOException) {
            PatientProfileOutcome.NetworkError()
        } catch (_: Exception) {
            PatientProfileOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun updateProfile(token: String, form: PatientProfileUpdateForm): PatientProfileUpdateOutcome {
        val dummyProfile = PatientProfile(
            personal = PersonalPatientInfo(1L, "paciente@essaid.test", "Juan Carlos", "Pérez Gómez", "DNI", "88000000", form.celular, "2000-05-15", "Masculino"),
            medical = MedicalPatientInfo("O+", form.toMedicalList(form.alergias), form.toMedicalList(form.condicionesCronicas), form.toMedicalList(form.medicamentosActuales), form.contactoEmergenciaNombre, form.contactoEmergenciaTelefono, form.observacionesMedicas, "2026-03-01")
        )
        return PatientProfileUpdateOutcome.Success(dummyProfile)
    }
}
