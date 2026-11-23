package com.essaid.user.domain.validation

import com.essaid.user.domain.model.CreateAtencionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AtencionValidatorTest {

    @Test
    fun `returns empty errors for valid complete request`() {
        val request = CreateAtencionRequest(
            motivoConsulta = "Dolor abdominal agudo",
            diagnostico = "Gastritis aguda",
            tratamiento = "Omeprazol 20mg cada 24 horas por 14 días",
            codigoCie10 = "K29.1",
            presionArterial = "120/80",
            frecuenciaCardiaca = 75,
            temperatura = "36.8",
            saturacionO2 = 98,
            pesoKg = "70.5",
            tallaCm = "172.0",
            notas = "Paciente refiere alivio relativo tras ingesta de antiácidos.",
        )

        val errors = AtencionValidator.validate(request)

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `returns errors when required fields are blank`() {
        val request = CreateAtencionRequest(
            motivoConsulta = "   ",
            diagnostico = "",
            tratamiento = "  ",
        )

        val errors = AtencionValidator.validate(request)

        assertEquals("El motivo de consulta es obligatorio.", errors["motivo_consulta"])
        assertEquals("El diagnóstico es obligatorio.", errors["diagnostico"])
        assertEquals("El tratamiento prescrito es obligatorio.", errors["tratamiento"])
    }

    @Test
    fun `validates invalid vital sign formats and ranges`() {
        val request = CreateAtencionRequest(
            motivoConsulta = "Control de rutina",
            diagnostico = "Chequeo médico",
            tratamiento = "Sin medicación",
            presionArterial = "12080", // Missing slash
            frecuenciaCardiaca = 300, // Exceeds max 250
            temperatura = "50.0", // Exceeds max 45
            saturacionO2 = 110, // Exceeds max 100
            pesoKg = "0.0", // Below min 0.1
            tallaCm = "300.0", // Exceeds max 250
        )

        val errors = AtencionValidator.validate(request)

        assertEquals("Formato de presión arterial inválido. Ejemplo: 120/80", errors["presion_arterial"])
        assertEquals("La frecuencia cardíaca debe estar entre 20 y 250 lpm.", errors["frecuencia_cardiaca"])
        assertEquals("La temperatura debe estar entre 25 y 45 °C.", errors["temperatura"])
        assertEquals("La saturación de O2 debe estar entre 0 y 100 %.", errors["saturacion_o2"])
        assertEquals("El peso debe estar entre 0.1 y 500 kg.", errors["peso_kg"])
        assertEquals("La talla debe estar entre 20 y 250 cm.", errors["talla_cm"])
    }
}
