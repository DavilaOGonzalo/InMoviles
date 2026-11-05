package com.essaid.user.data.remote

import com.essaid.user.domain.model.MedicalHistoryCategory
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class DjangoPatientMedicalHistoryHttpClientTest {
    @Test
    fun `requests selected patient endpoint and parses all groups`() = runTest {
        val responseBody = """
            {
              "alergia": [{
                "id": 15,
                "tipo": "alergia",
                "descripcion": "Alergia ficticia de prueba",
                "fecha_aproximada": null,
                "activo": true,
                "registrado_por": {"id": 7, "nombre": "Médica", "apellido": "Prueba"},
                "creado_en": "2026-01-02T10:00:00-05:00"
              }],
              "patologia": [],
              "cirugia": [],
              "familiar": [],
              "medicamento": []
            }
        """.trimIndent()
        lateinit var connection: RecordingHttpURLConnection
        val client = DjangoPatientMedicalHistoryHttpClient(
            baseUrl = "https://example.test/",
            connectionFactory = { url ->
                connection = RecordingHttpURLConnection(url, HttpURLConnection.HTTP_OK, responseBody)
                connection
            },
        )

        val outcome = client.getHistory("access-token", 42)

        assertEquals("https://example.test/api/medico/pacientes/42/antecedentes/", connection.url.toString())
        assertEquals("GET", connection.requestMethod)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
        val success = outcome as PatientMedicalHistoryOutcome.Success
        val allergy = success.history.entriesByCategory.getValue(MedicalHistoryCategory.Allergy).single()
        assertEquals("Alergia ficticia de prueba", allergy.description)
        assertEquals("Médica Prueba", allergy.recordedBy)
        MedicalHistoryCategory.entries.drop(1).forEach { category ->
            assertTrue(success.history.entriesByCategory.getValue(category).isEmpty())
        }
    }

    @Test
    fun `maps unauthorized and forbidden responses separately`() = runTest {
        fun clientFor(status: Int) = DjangoPatientMedicalHistoryHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = { url -> RecordingHttpURLConnection(url, status, "") },
        )

        assertEquals(
            PatientMedicalHistoryOutcome.Unauthorized,
            clientFor(HttpURLConnection.HTTP_UNAUTHORIZED).getHistory("expired", 42),
        )
        assertEquals(
            PatientMedicalHistoryOutcome.Forbidden,
            clientFor(HttpURLConnection.HTTP_FORBIDDEN).getHistory("patient", 42),
        )
    }

    @Test
    fun `rejects blank credentials before opening a connection`() = runTest {
        var opened = false
        val client = DjangoPatientMedicalHistoryHttpClient(
            baseUrl = "https://example.test",
            connectionFactory = {
                opened = true
                RecordingHttpURLConnection(it, HttpURLConnection.HTTP_OK, "{}")
            },
        )

        assertEquals(PatientMedicalHistoryOutcome.Unauthorized, client.getHistory("", 42))
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
