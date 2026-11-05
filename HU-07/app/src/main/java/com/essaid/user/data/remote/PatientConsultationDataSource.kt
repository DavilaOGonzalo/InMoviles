package com.essaid.user.data.remote

import com.essaid.user.domain.model.ConsultationDoctor
import com.essaid.user.domain.model.ConsultationEntry
import com.essaid.user.domain.model.PatientConsultations
import com.essaid.user.domain.model.PatientConsultationsOutcome
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

interface PatientConsultationDataSource {
    suspend fun getConsultations(
        token: String,
        patientId: Long,
        desde: String? = null,
        hasta: String? = null,
        page: Int? = null,
    ): PatientConsultationsOutcome
}

class DjangoPatientConsultationHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : PatientConsultationDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun getConsultations(
        token: String,
        patientId: Long,
        desde: String?,
        hasta: String?,
        page: Int?,
    ): PatientConsultationsOutcome {
        if (token.isBlank()) return PatientConsultationsOutcome.Unauthorized
        if (patientId <= 0) return PatientConsultationsOutcome.NotFound

        val queryParams = mutableListOf<String>()
        if (!desde.isNullOrBlank()) queryParams.add("desde=$desde")
        if (!hasta.isNullOrBlank()) queryParams.add("hasta=$hasta")
        if (page != null && page > 0) queryParams.add("page=$page")

        val queryString = if (queryParams.isNotEmpty()) "?" + queryParams.joinToString("&") else ""
        val url = URL("$baseUrl/api/medico/pacientes/$patientId/atenciones/$queryString")

        val connection = connectionFactory(url).apply {
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
                    PatientConsultationsOutcome.Success(parseConsultations(body))
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientConsultationsOutcome.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> PatientConsultationsOutcome.Forbidden
                HttpURLConnection.HTTP_NOT_FOUND -> PatientConsultationsOutcome.NotFound
                else -> PatientConsultationsOutcome.ServerError(statusCode)
            }
        } catch (_: IOException) {
            PatientConsultationsOutcome.NetworkError
        } catch (_: Exception) {
            PatientConsultationsOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseConsultations(body: String): PatientConsultations {
        val root = JSONObject(body)
        val count = root.optInt("count", 0)
        val next = root.nullableString("next")
        val previous = root.nullableString("previous")
        val resultsArray = root.optJSONArray("results")
            ?: if (root.has("id")) JSONArray().put(root) else JSONArray()

        val entries = (0 until resultsArray.length()).map { index ->
            val record = resultsArray.getJSONObject(index)
            val doctorObj = record.optJSONObject("medico")
            val doctor = doctorObj?.let {
                ConsultationDoctor(
                    id = it.optLong("id"),
                    nombre = it.optString("nombre"),
                    apellido = it.optString("apellido"),
                )
            }
            ConsultationEntry(
                id = record.getLong("id"),
                paciente = record.optLong("paciente"),
                medico = doctor,
                fechaAtencion = record.getString("fecha_atencion"),
                motivoConsulta = record.getString("motivo_consulta"),
                presionArterial = record.nullableString("presion_arterial"),
                frecuenciaCardiaca = record.optInteger("frecuencia_cardiaca"),
                temperatura = record.optDoubleOrNull("temperatura"),
                saturacionO2 = record.optInteger("saturacion_o2"),
                pesoKg = record.optDoubleOrNull("peso_kg"),
                tallaCm = record.optDoubleOrNull("talla_cm"),
                diagnostico = record.getString("diagnostico"),
                codigoCie10 = record.optString("codigo_cie10", ""),
                tratamiento = record.getString("tratamiento"),
                notas = record.nullableString("notas"),
                creadoEn = record.nullableString("creado_en"),
            )
        }
        return PatientConsultations(
            count = if (count > 0) count else entries.size,
            next = next,
            previous = previous,
            entries = entries,
        )
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    private fun JSONObject.optInteger(key: String): Int? =
        if (!has(key) || isNull(key)) null else optInt(key)

    private fun JSONObject.optDoubleOrNull(key: String): Double? =
        if (!has(key) || isNull(key)) null else optDouble(key)
}
