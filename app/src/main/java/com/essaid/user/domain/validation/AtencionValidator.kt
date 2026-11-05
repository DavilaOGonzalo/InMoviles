package com.essaid.user.domain.validation

import com.essaid.user.domain.model.CreateAtencionRequest

object AtencionValidator {
    private val bloodPressureRegex = Regex("""^\d{2,3}/\d{2,3}$""")

    fun validate(request: CreateAtencionRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (request.motivoConsulta.isBlank()) {
            errors["motivo_consulta"] = "El motivo de consulta es obligatorio."
        }

        if (request.diagnostico.isBlank()) {
            errors["diagnostico"] = "El diagnóstico es obligatorio."
        }

        if (request.tratamiento.isBlank()) {
            errors["tratamiento"] = "El tratamiento prescrito es obligatorio."
        }

        request.presionArterial?.takeIf(String::isNotBlank)?.let { pa ->
            if (!bloodPressureRegex.matches(pa.trim())) {
                errors["presion_arterial"] = "Formato de presión arterial inválido. Ejemplo: 120/80"
            }
        }

        request.frecuenciaCardiaca?.let { fc ->
            if (fc !in (20..250)) {
                errors["frecuencia_cardiaca"] = "La frecuencia cardíaca debe estar entre 20 y 250 lpm."
            }
        }

        request.temperatura?.takeIf(String::isNotBlank)?.let { tempStr ->
            val temp = tempStr.replace(',', '.').toDoubleOrNull()
            if (temp == null || temp !in 25.0..45.0) {
                errors["temperatura"] = "La temperatura debe estar entre 25 y 45 °C."
            }
        }

        request.saturacionO2?.let { sat ->
            if (sat !in 0..100) {
                errors["saturacion_o2"] = "La saturación de O2 debe estar entre 0 y 100 %."
            }
        }

        request.pesoKg?.takeIf(String::isNotBlank)?.let { pesoStr ->
            val peso = pesoStr.replace(',', '.').toDoubleOrNull()
            if (peso == null || peso !in 0.1..500.0) {
                errors["peso_kg"] = "El peso debe estar entre 0.1 y 500 kg."
            }
        }

        request.tallaCm?.takeIf(String::isNotBlank)?.let { tallaStr ->
            val talla = tallaStr.replace(',', '.').toDoubleOrNull()
            if (talla == null || talla !in 20.0..250.0) {
                errors["talla_cm"] = "La talla debe estar entre 20 y 250 cm."
            }
        }

        return errors
    }
}
