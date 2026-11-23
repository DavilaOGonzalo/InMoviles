package com.essaid.user.data.remote

import com.essaid.user.domain.model.MedicalHistoryCategory
import com.essaid.user.domain.model.MedicalHistoryEntry
import com.essaid.user.domain.model.PatientMedicalHistory
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

interface PatientMedicalHistoryDataSource {
    suspend fun getHistory(token: String, patientId: Long): PatientMedicalHistoryOutcome
}

class DjangoPatientMedicalHistoryHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : PatientMedicalHistoryDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun getHistory(token: String, patientId: Long): PatientMedicalHistoryOutcome {
        if (token.isBlank()) return PatientMedicalHistoryOutcome.Unauthorized
        if (patientId <= 0) return PatientMedicalHistoryOutcome.NotFound

        val connection = connectionFactory(
            URL("$baseUrl/api/medico/pacientes/$patientId/antecedentes/"),
        ).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            when (val statusCode = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream
                        .bufferedReader(StandardCharsets.UTF_8)
                        .use { it.readText() }
                    PatientMedicalHistoryOutcome.Success(parseHistory(body))
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientMedicalHistoryOutcome.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> PatientMedicalHistoryOutcome.Forbidden
                HttpURLConnection.HTTP_NOT_FOUND -> PatientMedicalHistoryOutcome.NotFound
                else -> PatientMedicalHistoryOutcome.ServerError(statusCode)
            }
        } catch (_: IOException) {
            PatientMedicalHistoryOutcome.NetworkError
        } catch (_: Exception) {
            PatientMedicalHistoryOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseHistory(body: String): PatientMedicalHistory {
        val root = JSONObject(body)
        val groups = MedicalHistoryCategory.entries.associateWith { category ->
            val records = root.getJSONArray(category.wireValue)
            (0 until records.length()).map { index ->
                val record = records.getJSONObject(index)
                val doctor = record.optJSONObject("registrado_por")
                MedicalHistoryEntry(
                    id = record.getLong("id"),
                    description = record.getString("descripcion"),
                    approximateDate = record.nullableString("fecha_aproximada"),
                    active = record.getBoolean("activo"),
                    recordedBy = doctor?.let {
                        listOf(it.optString("nombre"), it.optString("apellido"))
                            .filter(String::isNotBlank)
                            .joinToString(" ")
                            .ifBlank { null }
                    },
                    createdAt = record.nullableString("creado_en"),
                )
            }
        }
        return PatientMedicalHistory(groups)
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)
}
