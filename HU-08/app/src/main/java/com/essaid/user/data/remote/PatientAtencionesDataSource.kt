package com.essaid.user.data.remote

import com.essaid.user.domain.model.AttendingDoctor
import com.essaid.user.domain.model.CreateAtencionRequest
import com.essaid.user.domain.model.PatientAtencion
import com.essaid.user.domain.model.PatientAtencionesOutcome
import com.essaid.user.domain.model.PatientAtencionesResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface PatientAtencionesDataSource {
    suspend fun getAtenciones(
        token: String,
        patientId: Long,
        desde: String? = null,
        hasta: String? = null,
        page: Int = 1,
    ): PatientAtencionesOutcome

    suspend fun createAtencion(
        token: String,
        patientId: Long,
        request: CreateAtencionRequest,
    ): PatientAtencionesOutcome
}

class MockPatientAtencionesDataSource : PatientAtencionesDataSource {
    private val mockAtenciones = mutableListOf(
        PatientAtencion(
            id = 101L,
            patientId = 3L,
            medico = AttendingDoctor(id = 7L, nombre = "Dra. María", apellido = "González"),
            fechaAtencion = "2026-03-01T10:30:00Z",
            motivoConsulta = "Control general y seguimiento de presión arterial.",
            presionArterial = "120/80",
            frecuenciaCardiaca = 72,
            temperatura = "36.5",
            saturacionO2 = 98,
            pesoKg = "68.5",
            tallaCm = "170.0",
            diagnostico = "Hipertensión arterial esencial controlada.",
            codigoCie10 = "I10",
            tratamiento = "Continuar con dieta baja en sodio y control diario de presión.",
            notas = "Paciente refiere sentirse bien sin efectos secundarios.",
            creadoEn = "2026-03-01T11:00:00Z",
        ),
        PatientAtencion(
            id = 102L,
            patientId = 3L,
            medico = AttendingDoctor(id = 7L, nombre = "Dra. María", apellido = "González"),
            fechaAtencion = "2026-02-15T09:00:00Z",
            motivoConsulta = "Tos persistente y dolor de garganta.",
            presionArterial = "118/76",
            frecuenciaCardiaca = 80,
            temperatura = "37.2",
            saturacionO2 = 97,
            pesoKg = "68.0",
            tallaCm = "170.0",
            diagnostico = "Faringitis aguda.",
            codigoCie10 = "J02.9",
            tratamiento = "Paracetamol 500mg cada 8 horas por 3 días. Abundantes líquidos.",
            notas = "Amígdalas hiperémicas sin exudado purulento.",
            creadoEn = "2026-02-15T09:30:00Z",
        ),
    )

    override suspend fun getAtenciones(
        token: String,
        patientId: Long,
        desde: String?,
        hasta: String?,
        page: Int,
    ): PatientAtencionesOutcome {
        if (token.isBlank() || token == "invalid-token") {
            return PatientAtencionesOutcome.Unauthorized
        }
        if (token == "forbidden-token") {
            return PatientAtencionesOutcome.Forbidden
        }
        if (patientId <= 0) {
            return PatientAtencionesOutcome.NotFound
        }

        var filtered = mockAtenciones.filter { it.patientId == patientId }
        desde?.takeIf { it.isNotBlank() }?.let { d ->
            filtered = filtered.filter { it.fechaAtencion.take(10) >= d }
        }
        hasta?.takeIf { it.isNotBlank() }?.let { h ->
            filtered = filtered.filter { it.fechaAtencion.take(10) <= h }
        }

        val response = PatientAtencionesResponse(
            count = filtered.size,
            next = null,
            previous = null,
            results = filtered,
        )
        return PatientAtencionesOutcome.Success(response)
    }

    override suspend fun createAtencion(
        token: String,
        patientId: Long,
        request: CreateAtencionRequest,
    ): PatientAtencionesOutcome {
        if (token.isBlank() || token == "invalid-token") {
            return PatientAtencionesOutcome.Unauthorized
        }
        if (token == "forbidden-token") {
            return PatientAtencionesOutcome.Forbidden
        }
        if (patientId <= 0) {
            return PatientAtencionesOutcome.NotFound
        }

        val created = PatientAtencion(
            id = (mockAtenciones.maxOfOrNull { it.id } ?: 100L) + 1L,
            patientId = patientId,
            medico = AttendingDoctor(id = 7L, nombre = "Dra. María", apellido = "González"),
            fechaAtencion = request.fechaAtencion ?: "2026-03-31T12:00:00Z",
            motivoConsulta = request.motivoConsulta,
            presionArterial = request.presionArterial?.takeIf(String::isNotBlank),
            frecuenciaCardiaca = request.frecuenciaCardiaca,
            temperatura = request.temperatura?.takeIf(String::isNotBlank),
            saturacionO2 = request.saturacionO2,
            pesoKg = request.pesoKg?.takeIf(String::isNotBlank),
            tallaCm = request.tallaCm?.takeIf(String::isNotBlank),
            diagnostico = request.diagnostico,
            codigoCie10 = request.codigoCie10?.takeIf(String::isNotBlank),
            tratamiento = request.tratamiento,
            notas = request.notas?.takeIf(String::isNotBlank),
            creadoEn = "2026-03-31T12:00:00Z",
        )
        mockAtenciones.add(0, created)
        return PatientAtencionesOutcome.CreateSuccess(created)
    }
}

class DjangoPatientAtencionesHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : PatientAtencionesDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun getAtenciones(
        token: String,
        patientId: Long,
        desde: String?,
        hasta: String?,
        page: Int,
    ): PatientAtencionesOutcome {
        if (token.isBlank()) return PatientAtencionesOutcome.Unauthorized
        if (patientId <= 0) return PatientAtencionesOutcome.NotFound

        val queryParams = mutableListOf<String>()
        desde?.takeIf { it.isNotBlank() }?.let {
            queryParams.add("desde=${URLEncoder.encode(it, StandardCharsets.UTF_8.name())}")
        }
        hasta?.takeIf { it.isNotBlank() }?.let {
            queryParams.add("hasta=${URLEncoder.encode(it, StandardCharsets.UTF_8.name())}")
        }
        if (page > 1) {
            queryParams.add("page=$page")
        }

        val queryString = if (queryParams.isNotEmpty()) "?${queryParams.joinToString("&")}" else ""
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
                    PatientAtencionesOutcome.Success(parseResponse(body))
                }
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val errorBody = connection.errorStream
                        ?.bufferedReader(StandardCharsets.UTF_8)
                        ?.use { it.readText() } ?: "{}"
                    PatientAtencionesOutcome.ValidationError(parseFieldErrors(errorBody))
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
        if (token.isBlank()) return PatientAtencionesOutcome.Unauthorized
        if (patientId <= 0) return PatientAtencionesOutcome.NotFound

        val url = URL("$baseUrl/api/medico/pacientes/$patientId/atenciones/")
        val jsonBody = JSONObject().apply {
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
            request.fechaAtencion?.takeIf(String::isNotBlank)?.let { put("fecha_atencion", it) }
        }.toString()

        val connection = connectionFactory(url).apply {
            requestMethod = "POST"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            connection.outputStream.use { os ->
                os.write(jsonBody.toByteArray(StandardCharsets.UTF_8))
            }

            when (val statusCode = connection.responseCode) {
                HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream
                        .bufferedReader(StandardCharsets.UTF_8)
                        .use { it.readText() }
                    PatientAtencionesOutcome.CreateSuccess(parseSingleAtencion(body))
                }
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val errorBody = connection.errorStream
                        ?.bufferedReader(StandardCharsets.UTF_8)
                        ?.use { it.readText() } ?: "{}"
                    PatientAtencionesOutcome.ValidationError(parseFieldErrors(errorBody))
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

    private fun parseSingleAtencion(body: String): PatientAtencion {
        val obj = JSONObject(body)
        val doctorObj = obj.optJSONObject("medico")
        val doctor = doctorObj?.let {
            AttendingDoctor(
                id = it.optLong("id"),
                nombre = it.optString("nombre", ""),
                apellido = it.optString("apellido", ""),
            )
        }
        return PatientAtencion(
            id = obj.getLong("id"),
            patientId = obj.optLong("paciente", 0L),
            medico = doctor,
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
            creadoEn = obj.nullableString("creado_en"),
        )
    }

    private fun parseResponse(body: String): PatientAtencionesResponse {
        val root = JSONObject(body)
        val count = root.optInt("count", 0)
        val next = root.nullableString("next")
        val previous = root.nullableString("previous")
        val resultsArray = root.optJSONArray("results") ?: JSONArray()

        val results = (0 until resultsArray.length()).map { index ->
            val obj = resultsArray.getJSONObject(index)
            val doctorObj = obj.optJSONObject("medico")
            val doctor = doctorObj?.let {
                AttendingDoctor(
                    id = it.optLong("id"),
                    nombre = it.optString("nombre", ""),
                    apellido = it.optString("apellido", ""),
                )
            }
            PatientAtencion(
                id = obj.getLong("id"),
                patientId = obj.optLong("paciente", 0L),
                medico = doctor,
                fechaAtencion = obj.getString("fecha_atencion"),
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
                creadoEn = obj.nullableString("creado_en"),
            )
        }

        return PatientAtencionesResponse(
            count = count,
            next = next,
            previous = previous,
            results = results,
        )
    }

    private fun parseFieldErrors(body: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        try {
            val json = JSONObject(body)
            json.keys().forEach { key ->
                val value = json.get(key)
                when (value) {
                    is JSONArray -> {
                        val messages = (0 until value.length()).map { value.getString(it) }
                        errors[key] = messages.joinToString(", ")
                    }
                    is String -> errors[key] = value
                }
            }
        } catch (_: Exception) {
            errors["detail"] = body
        }
        return errors
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (!has(key) || isNull(key)) null else optInt(key)
}
