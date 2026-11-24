package com.essaid.user.data.remote

import com.essaid.user.domain.model.PatientSearchOutcome
import com.essaid.user.domain.model.PatientSearchResult
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SupabasePatientSearchDataSource(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val apiKey: String = SupabaseConfig.ANON_KEY,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) : PatientSearchDataSource {

    override suspend fun searchPatients(token: String, query: String): PatientSearchOutcome {
        val trimmed = query.trim()
        val urlString = if (trimmed.isNotEmpty()) {
            if (trimmed.all { it.isDigit() }) {
                "$baseUrl/rest/v1/profiles?role=eq.paciente&numero_documento=eq.$trimmed"
            } else {
                val encoded = URLEncoder.encode("%$trimmed%", "UTF-8")
                "$baseUrl/rest/v1/profiles?role=eq.paciente&or=(nombre.ilike.$encoded,apellido.ilike.$encoded)"
            }
        } else {
            "$baseUrl/rest/v1/profiles?role=eq.paciente&limit=100"
        }

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
                    val results = (0 until jsonArray.length()).map { i ->
                        val obj = jsonArray.getJSONObject(i)
                        PatientSearchResult(
                            id = obj.optLong("id_num", (1000 + i).toLong()),
                            nombre = obj.optString("nombre", ""),
                            apellido = obj.optString("apellido", ""),
                            dni = obj.optString("numero_documento", ""),
                            fechaNacimiento = obj.optString("fecha_nacimiento", "1990-01-01"),
                            sexo = obj.optString("genero", "No especificado"),
                            tipoSangre = "O+",
                            alergias = emptyList(),
                            condicionesCronicas = emptyList(),
                            medicamentosActuales = emptyList(),
                            contactoEmergencia = "",
                            telefonoEmergencia = "",
                            observacionesMedicas = "",
                            informacionMedicaActualizada = "2026-03-01",
                        )
                    }
                    PatientSearchOutcome.Success(results)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientSearchOutcome.Unauthorized
                else -> PatientSearchOutcome.ServerError(statusCode)
            }
        } catch (_: IOException) {
            PatientSearchOutcome.NetworkError
        } catch (_: Exception) {
            PatientSearchOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }
}
