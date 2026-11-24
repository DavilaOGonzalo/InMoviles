package com.essaid.user.data.remote

import com.essaid.user.domain.model.CreateAtencionRequest
import com.essaid.user.domain.model.PatientAtencion
import com.essaid.user.domain.model.PatientAtencionesOutcome
import com.essaid.user.domain.model.PatientAtencionesResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SupabasePatientAtencionesHttpClient(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val apiKey: String = SupabaseConfig.ANON_KEY,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) : PatientAtencionesDataSource {

    override suspend fun getAtenciones(
        token: String,
        patientId: Long,
        desde: String?,
        hasta: String?,
        page: Int,
    ): PatientAtencionesOutcome {
        var urlString = "$baseUrl/rest/v1/historial_medico_unificado?patient_id=eq.$patientId"
        if (!desde.isNullOrBlank()) {
            urlString += "&fecha_atencion=gte.$desde"
        }
        if (!hasta.isNullOrBlank()) {
            urlString += "&fecha_atencion=lte.$hasta"
        }
        urlString += "&order=fecha_atencion.desc"

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
                        PatientAtencion(
                            id = obj.optLong("id"),
                            patientId = patientId,
                            medico = null,
                            fechaAtencion = obj.optString("fecha_atencion", ""),
                            motivoConsulta = obj.optString("motivo_consulta", ""),
                            presionArterial = obj.nullableString("presion_arterial"),
                            frecuenciaCardiaca = obj.nullableInt("frecuencia_cardiaca"),
                            temperatura = obj.nullableString("temperatura"),
                            saturacionO2 = obj.nullableInt("saturacion_o2"),
                            pesoKg = obj.nullableString("peso_kg"),
                            tallaCm = obj.nullableString("talla_cm"),
                            diagnostico = obj.optString("diagnostico", ""),
                            codigoCie10 = obj.nullableString("codigo_cie10"),
                            tratamiento = obj.optString("tratamiento", ""),
                            notas = obj.nullableString("notas"),
                            creadoEn = obj.nullableString("created_at"),
                        )
                    }
                    val response = PatientAtencionesResponse(
                        count = results.size,
                        next = null,
                        previous = null,
                        results = results,
                    )
                    PatientAtencionesOutcome.Success(response)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientAtencionesOutcome.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> PatientAtencionesOutcome.Forbidden
                HttpURLConnection.HTTP_NOT_FOUND -> PatientAtencionesOutcome.NotFound
                else -> PatientAtencionesOutcome.ServerError(statusCode)
            }
        } catch (_: IOException) {
            PatientAtencionesOutcome.NetworkError
        } catch (_: Exception) {
            PatientAtencionesOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun createAtencion(
        token: String,
        patientId: Long,
        request: CreateAtencionRequest,
    ): PatientAtencionesOutcome {
        val url = URL("$baseUrl/rest/v1/hospital_norte_atenciones")
        val jsonBody = JSONObject().apply {
            put("patient_id", patientId)
            put("motivo_consulta", request.motivoConsulta)
            put("diagnostico", request.diagnostico)
            put("tratamiento", request.tratamiento)
            request.presionArterial?.takeIf(String::isNotBlank)?.let { put("presion_arterial", it) }
            request.frecuenciaCardiaca?.let { put("frecuencia_cardiaca", it) }
            request.temperatura?.takeIf(String::isNotBlank)?.let { put("temperatura", it) }
            request.saturacionO2?.let { put("saturacion_o2", it) }
            request.pesoKg?.takeIf(String::isNotBlank)?.let { put("peso_kg", it) }
            request.tallaCm?.takeIf(String::isNotBlank)?.let { put("talla_cm", it) }
            request.codigoCie10?.takeIf(String::isNotBlank)?.let { put("codigo_cie10", it) }
            request.notas?.takeIf(String::isNotBlank)?.let { put("notas", it) }
        }.toString()

        val connection = connectionFactory(url).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer ${if (token.isNotBlank()) token else apiKey}")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Prefer", "return=representation")
        }

        return try {
            connection.outputStream.use { os ->
                os.write(jsonBody.toByteArray(StandardCharsets.UTF_8))
            }
            when (val statusCode = connection.responseCode) {
                HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    val jsonArray = JSONArray(body)
                    val obj = if (jsonArray.length() > 0) jsonArray.getJSONObject(0) else JSONObject(body)
                    val atencion = PatientAtencion(
                        id = obj.optLong("id"),
                        patientId = patientId,
                        medico = null,
                        fechaAtencion = obj.optString("fecha_atencion", ""),
                        motivoConsulta = obj.getString("motivo_consulta"),
                        presionArterial = obj.nullableString("presion_arterial"),
                        frecuenciaCardiaca = obj.nullableInt("frecuencia_cardiaca"),
                        temperatura = obj.nullableString("temperatura"),
                        saturacionO2 = obj.nullableInt("saturacion_o2"),
                        pesoKg = obj.nullableString("peso_kg"),
                        tallaCm = obj.nullableString("talla_cm"),
                        diagnostico = obj.getString("diagnostico"),
                        codigoCie10 = obj.nullableString("codigo_cie10"),
                        tratamiento = obj.getString("tratamiento"),
                        notas = obj.nullableString("notas"),
                        creadoEn = obj.nullableString("created_at"),
                    )
                    PatientAtencionesOutcome.CreateSuccess(atencion)
                }
                else -> PatientAtencionesOutcome.ServerError(statusCode)
            }
        } catch (_: IOException) {
            PatientAtencionesOutcome.NetworkError
        } catch (_: Exception) {
            PatientAtencionesOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (!has(key) || isNull(key)) null else optInt(key)
}
