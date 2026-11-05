package com.essaid.user.data.remote

import com.essaid.user.domain.model.PatientSearchOutcome
import com.essaid.user.domain.model.PatientSearchResult
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface PatientSearchDataSource {
    suspend fun searchPatients(token: String, query: String): PatientSearchOutcome
}

class MockPatientSearchDataSource : PatientSearchDataSource {
    private val fictitiousPatients = listOf(
        PatientSearchResult(
            id = 3,
            nombre = "Juan Carlos",
            apellido = "Pérez Gómez",
            dni = "88000000",
            fechaNacimiento = "2000-05-15",
            sexo = "masculino",
            tipoSangre = "O+",
            alergias = listOf("Penicilina (dato ficticio)"),
            condicionesCronicas = listOf("Ninguna registrada"),
            medicamentosActuales = listOf("Ninguno"),
            contactoEmergencia = "Marisol Pérez (Madre)",
            telefonoEmergencia = "912345678",
            observacionesMedicas = "Dato de alergia pendiente de confirmación directa con el paciente.",
            informacionMedicaActualizada = "2026-03-01",
        ),
        PatientSearchResult(
            id = 4,
            nombre = "Rosa Elena",
            apellido = "Quispe Cárdenas",
            dni = "88000001",
            fechaNacimiento = "1992-04-15",
            sexo = "femenino",
            tipoSangre = "O+",
            alergias = listOf("Látex (dato ficticio)"),
            condicionesCronicas = listOf("Ninguna registrada"),
            medicamentosActuales = listOf("Ninguno"),
            contactoEmergencia = "Elena Cárdenas (Madre)",
            telefonoEmergencia = "976543210",
            observacionesMedicas = "Confirmar alergias y antecedentes directamente con el paciente antes de usarlos.",
            informacionMedicaActualizada = "2026-03-01",
        ),
        PatientSearchResult(
            id = 5,
            nombre = "Luis Miguel",
            apellido = "Huamán Salazar",
            dni = "88000002",
            fechaNacimiento = "1987-09-22",
            sexo = "masculino",
            tipoSangre = "A+",
            alergias = listOf("Ninguna registrada (dato ficticio)"),
            condicionesCronicas = listOf("Ninguna registrada"),
            medicamentosActuales = listOf("Ninguno"),
            contactoEmergencia = "Carlos Salazar (Padre)",
            telefonoEmergencia = "965432108",
            observacionesMedicas = "Completar la información en una entrevista real.",
            informacionMedicaActualizada = "2026-03-01",
        ),
        PatientSearchResult(
            id = 6,
            nombre = "Andrea Paola",
            apellido = "Rojas Medina",
            dni = "88000003",
            fechaNacimiento = "1998-12-03",
            sexo = "femenino",
            tipoSangre = "B+",
            alergias = listOf("Polvo doméstico (dato ficticio)"),
            condicionesCronicas = listOf("Ninguna registrada"),
            medicamentosActuales = listOf("Ninguno"),
            contactoEmergencia = "Patricia Medina (Madre)",
            telefonoEmergencia = "954321087",
            observacionesMedicas = "Nota ficticia de demostración: alergia de ejemplo pendiente de confirmación con el paciente.",
            informacionMedicaActualizada = "2026-03-01",
        ),
        PatientSearchResult(
            id = 7,
            nombre = "José Antonio",
            apellido = "Mamani Flores",
            dni = "88000004",
            fechaNacimiento = "1979-06-28",
            sexo = "masculino",
            tipoSangre = "O-",
            alergias = listOf("Ninguna registrada (dato ficticio)"),
            condicionesCronicas = listOf("Ninguna registrada"),
            medicamentosActuales = listOf("Ninguno"),
            contactoEmergencia = "Miguel Flores (Hermano)",
            telefonoEmergencia = "943210876",
            observacionesMedicas = "Sin observaciones clínicas verificadas; confirmar datos en consulta.",
            informacionMedicaActualizada = "2026-03-01",
        ),
        PatientSearchResult(
            id = 8,
            nombre = "Fiorella Milagros",
            apellido = "Chávez Paredes",
            dni = "88000005",
            fechaNacimiento = "2001-02-11",
            sexo = "femenino",
            tipoSangre = "AB+",
            alergias = listOf("Fragancias (dato ficticio)"),
            condicionesCronicas = listOf("Ninguna registrada"),
            medicamentosActuales = listOf("Ninguno"),
            contactoEmergencia = "Rosa Paredes (Madre)",
            telefonoEmergencia = "983456783",
            observacionesMedicas = "Alergias pendientes de confirmar directamente con el paciente.",
            informacionMedicaActualizada = "2026-03-01",
        ),
    )

    override suspend fun searchPatients(token: String, query: String): PatientSearchOutcome {
        return when {
            token.isBlank() || token == "invalid-token" -> PatientSearchOutcome.Unauthorized
            token == "forbidden-token" -> PatientSearchOutcome.Forbidden
            else -> {
                val normalizedQuery = query.trim()
                PatientSearchOutcome.Success(
                    fictitiousPatients.filter { patient ->
                        patient.dni == normalizedQuery ||
                            "${patient.nombre} ${patient.apellido}".contains(normalizedQuery, ignoreCase = true)
                    },
                )
            }
        }
    }
}

class DjangoPatientSearchHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : PatientSearchDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun searchPatients(token: String, query: String): PatientSearchOutcome {
        if (token.isBlank()) return PatientSearchOutcome.Unauthorized
        val encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name())
        val url = URL("$baseUrl/api/medico/pacientes/?q=$encodedQuery")
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
                    val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    PatientSearchOutcome.Success(parsePatients(body))
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientSearchOutcome.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> PatientSearchOutcome.Forbidden
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

    private fun parsePatients(body: String): List<PatientSearchResult> {
        val root = JSONObject(body)
        val results = root.optJSONArray("results")
            ?: if (root.has("id")) JSONArray().put(root) else JSONArray()
        return (0 until results.length()).map { index ->
            val patient = results.getJSONObject(index)
            PatientSearchResult(
                id = patient.getLong("id"),
                nombre = patient.getString("nombre"),
                apellido = patient.getString("apellido"),
                dni = patient.nullableString("dni"),
                fechaNacimiento = patient.nullableString("fecha_nacimiento"),
                sexo = patient.nullableString("sexo"),
                tipoSangre = patient.nullableString("tipo_sangre"),
                alergias = patient.stringList("alergias"),
                condicionesCronicas = patient.stringList("condiciones_cronicas"),
                medicamentosActuales = patient.stringList("medicamentos_actuales"),
                contactoEmergencia = patient.nullableString("contacto_emergencia"),
                telefonoEmergencia = patient.nullableString("telefono_emergencia"),
                observacionesMedicas = patient.nullableString("observaciones_medicas"),
                informacionMedicaActualizada = patient.nullableString("informacion_medica_actualizada"),
            )
        }
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key) || !has(key)) null else getString(key)

    private fun JSONObject.stringList(key: String): List<String> {
        val values = optJSONArray(key) ?: return emptyList()
        return (0 until values.length()).map(values::getString)
    }
}
