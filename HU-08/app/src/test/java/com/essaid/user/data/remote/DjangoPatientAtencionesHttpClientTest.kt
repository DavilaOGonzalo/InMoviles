package com.essaid.user.data.remote

import com.essaid.user.domain.model.CreateAtencionRequest
import com.essaid.user.domain.model.PatientAtencionesOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class DjangoPatientAtencionesHttpClientTest {

    @Test
    fun `requests patient atenciones endpoint and parses pagination results`() = runTest {
        val responseBody = """
            {
              "count": 1,
              "next": null,
              "previous": null,
              "results": [{
                "id": 101,
                "paciente": 42,
                "medico": {"id": 7, "nombre": "Médica", "apellido": "Prueba"},
                "fecha_atencion": "2026-03-01T10:30:00Z",
                "motivo_consulta": "Control rutinario",
                "presion_arterial": "120/80",
                "frecuencia_cardiaca": 75,
                "temperatura": "36.6",
                "saturacion_o2": 99,
                "peso_kg": "70.00",
                "talla_cm": "175.00",
                "diagnostico": "Sano",
                "codigo_cie10": "Z00.0",
                "tratamiento": "Ninguno",
                "notas": "Sin novedad",
                "creado_en": "2026-03-01T11:00:00Z"
              }]
            }
        """.trimIndent()
        lateinit var connection: RecordingHttpURLConnection
        val client = DjangoPatientAtencionesHttpClient(
            baseUrl = "https://example.test/",
            connectionFactory = { url ->
                connection = RecordingHttpURLConnection(url, HttpURLConnection.HTTP_OK, responseBody)
                connection
            },
        )

        val outcome = client.getAtenciones("access-token", 42, desde = "2026-03-01", hasta = "2026-03-01", page = 1)

        assertEquals("https://example.test/api/medico/pacientes/42/atenciones/?desde=2026-03-01&hasta=2026-03-01", connection.url.toString())
        assertEquals("GET", connection.requestMethod)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
        val success = outcome as PatientAtencionesOutcome.Success
        assertEquals(1, success.response.count)
        val atencion = success.response.results.single()
        assertEquals(101L, atencion.id)
        assertEquals("Control rutinario", atencion.motivoConsulta)
        assertEquals("Z00.0", atencion.codigoCie10)
        assertEquals("Médica Prueba", atencion.medico?.fullName)
    }

    @Test
    fun `posts new atencion to endpoint and parses 201 created response`() = runTest {
        val createdBody = """
            {
              "id": 105,
              "paciente": 42,
              "medico": {"id": 7, "nombre": "Dra. María", "apellido": "González"},
              "fecha_atencion": "2026-03-31T12:00:00Z",
              "motivo_consulta": "Dolor torácico",
              "presion_arterial": "130/85",
              "frecuencia_cardiaca": 88,
              "temperatura": "37.0",
              "saturacion_o2": 97,
              "peso_kg": "75.0",
              "talla_cm": "175.0",
              "diagnostico": "Angina de pecho",
              "codigo_cie10": "I20.9",
              "tratamiento": "Nitroglicerina sublingual SOS",
              "notas": "Derivado a cardiología",
              "creado_en": "2026-03-31T12:00:00Z"
            }
        """.trimIndent()
        lateinit var connection: RecordingHttpURLConnection
        val client = DjangoPatientAtencionesHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = { url ->
                connection = RecordingHttpURLConnection(url, HttpURLConnection.HTTP_CREATED, createdBody)
                connection
            },
        )

        val request = CreateAtencionRequest(
            motivoConsulta = "Dolor torácico",
            diagnostico = "Angina de pecho",
            tratamiento = "Nitroglicerina sublingual SOS",
            codigoCie10 = "I20.9",
            presionArterial = "130/85",
            frecuenciaCardiaca = 88,
            temperatura = "37.0",
            saturacionO2 = 97,
            pesoKg = "75.0",
            tallaCm = "175.0",
            notas = "Derivado a cardiología",
        )

        val outcome = client.createAtencion("access-token", 42, request)

        assertEquals("https://example.test/api/medico/pacientes/42/atenciones/", connection.url.toString())
        assertEquals("POST", connection.requestMethod)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
        assertEquals("application/json; charset=UTF-8", connection.getRequestProperty("Content-Type"))

        val postedJson = connection.writtenBody
        assertTrue(postedJson.contains("Dolor torácico"))
        assertTrue(postedJson.contains("Angina de pecho"))
        assertTrue(postedJson.contains("Nitroglicerina sublingual SOS"))

        val success = outcome as PatientAtencionesOutcome.CreateSuccess
        assertEquals(105L, success.atencion.id)
        assertEquals(42L, success.atencion.patientId)
        assertEquals("Dolor torácico", success.atencion.motivoConsulta)
        assertEquals("Angina de pecho", success.atencion.diagnostico)
    }

    @Test
    fun `parses 400 bad request error response for createAtencion`() = runTest {
        val errorBody = """{"motivo_consulta": ["Este campo es obligatorio."]}"""
        val client = DjangoPatientAtencionesHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = { url ->
                RecordingHttpURLConnection(url, HttpURLConnection.HTTP_BAD_REQUEST, "", errorBody = errorBody)
            },
        )

        val outcome = client.createAtencion(
            "access-token",
            42,
            CreateAtencionRequest("", "", ""),
        )

        val validationError = outcome as PatientAtencionesOutcome.ValidationError
        assertEquals("Este campo es obligatorio.", validationError.fieldErrors["motivo_consulta"])
    }

    @Test
    fun `maps unauthorized and forbidden responses separately`() = runTest {
        fun clientFor(status: Int) = DjangoPatientAtencionesHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = { url -> RecordingHttpURLConnection(url, status, "") },
        )

        assertEquals(
            PatientAtencionesOutcome.Unauthorized,
            clientFor(HttpURLConnection.HTTP_UNAUTHORIZED).getAtenciones("expired", 42),
        )
        assertEquals(
            PatientAtencionesOutcome.Forbidden,
            clientFor(HttpURLConnection.HTTP_FORBIDDEN).getAtenciones("patient", 42),
        )
    }

    @Test
    fun `rejects blank credentials before opening connection`() = runTest {
        var opened = false
        val client = DjangoPatientAtencionesHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = {
                opened = true
                RecordingHttpURLConnection(it, HttpURLConnection.HTTP_OK, "{}")
            },
        )

        assertEquals(PatientAtencionesOutcome.Unauthorized, client.getAtenciones("", 42))
        assertFalse(opened)
    }

    private class RecordingHttpURLConnection(
        url: URL,
        private val status: Int,
        private val body: String,
        private val errorBody: String = "{}",
    ) : HttpURLConnection(url) {
        private val outputStreamBuffer = ByteArrayOutputStream()

        val writtenBody: String
            get() = outputStreamBuffer.toString(StandardCharsets.UTF_8.name())

        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy(): Boolean = false
        override fun getResponseCode(): Int = status
        override fun getInputStream() = ByteArrayInputStream(body.toByteArray(StandardCharsets.UTF_8))
        override fun getErrorStream() = ByteArrayInputStream(errorBody.toByteArray(StandardCharsets.UTF_8))
        override fun getOutputStream(): OutputStream = outputStreamBuffer
    }
}
