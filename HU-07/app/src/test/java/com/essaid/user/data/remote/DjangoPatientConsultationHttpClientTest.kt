package com.essaid.user.data.remote

import com.essaid.user.domain.model.PatientConsultationsOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class DjangoPatientConsultationHttpClientTest {
    @Test
    fun `requests patient atenciones endpoint and parses consultation entries`() = runTest {
        val responseBody = """
            {
              "count": 1,
              "next": null,
              "previous": null,
              "results": [{
                "id": 101,
                "paciente": 42,
                "medico": {"id": 7, "nombre": "Médica", "apellido": "Prueba"},
                "fecha_atencion": "2026-03-01T10:30:00-05:00",
                "motivo_consulta": "Control",
                "presion_arterial": "120/80",
                "frecuencia_cardiaca": 72,
                "temperatura": 36.5,
                "saturacion_o2": 98,
                "peso_kg": 70.5,
                "talla_cm": 172.0,
                "diagnostico": "Hipertensión",
                "codigo_cie10": "I10",
                "tratamiento": "Dieta",
                "notas": "Bien",
                "creado_en": "2026-03-01T11:00:00-05:00"
              }]
            }
        """.trimIndent()
        lateinit var connection: RecordingHttpURLConnection
        val client = DjangoPatientConsultationHttpClient(
            baseUrl = "https://example.test/",
            connectionFactory = { url ->
                connection = RecordingHttpURLConnection(url, HttpURLConnection.HTTP_OK, responseBody)
                connection
            },
        )

        val outcome = client.getConsultations("access-token", 42, desde = "2026-03-01", hasta = "2026-03-01", page = 1)

        assertEquals("https://example.test/api/medico/pacientes/42/atenciones/?desde=2026-03-01&hasta=2026-03-01&page=1", connection.url.toString())
        assertEquals("GET", connection.requestMethod)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
        val success = outcome as PatientConsultationsOutcome.Success
        val entry = success.consultations.entries.single()
        assertEquals(101L, entry.id)
        assertEquals("Control", entry.motivoConsulta)
        assertEquals("Hipertensión", entry.diagnostico)
        assertEquals("I10", entry.codigoCie10)
        assertEquals("Médica Prueba", entry.medico?.fullName)
    }

    @Test
    fun `maps unauthorized and forbidden responses for atenciones`() = runTest {
        fun clientFor(status: Int) = DjangoPatientConsultationHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = { url -> RecordingHttpURLConnection(url, status, "") },
        )

        assertEquals(
            PatientConsultationsOutcome.Unauthorized,
            clientFor(HttpURLConnection.HTTP_UNAUTHORIZED).getConsultations("expired", 42),
        )
        assertEquals(
            PatientConsultationsOutcome.Forbidden,
            clientFor(HttpURLConnection.HTTP_FORBIDDEN).getConsultations("patient", 42),
        )
    }

    @Test
    fun `rejects blank credentials before opening connection for atenciones`() = runTest {
        var opened = false
        val client = DjangoPatientConsultationHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = {
                opened = true
                RecordingHttpURLConnection(it, HttpURLConnection.HTTP_OK, "{}")
            },
        )

        assertEquals(PatientConsultationsOutcome.Unauthorized, client.getConsultations("", 42))
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
