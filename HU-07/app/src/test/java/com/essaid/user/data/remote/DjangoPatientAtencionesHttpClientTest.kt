package com.essaid.user.data.remote

import com.essaid.user.domain.model.PatientAtencionesOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.ByteArrayInputStream
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
    ) : HttpURLConnection(url) {
        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy(): Boolean = false
        override fun getResponseCode(): Int = status
        override fun getInputStream() = ByteArrayInputStream(body.toByteArray(StandardCharsets.UTF_8))
    }
}
