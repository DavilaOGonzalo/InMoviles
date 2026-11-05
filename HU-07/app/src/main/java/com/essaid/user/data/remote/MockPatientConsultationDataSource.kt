package com.essaid.user.data.remote

import com.essaid.user.domain.model.ConsultationDoctor
import com.essaid.user.domain.model.ConsultationEntry
import com.essaid.user.domain.model.PatientConsultations
import com.essaid.user.domain.model.PatientConsultationsOutcome

class MockPatientConsultationDataSource : PatientConsultationDataSource {
    override suspend fun getConsultations(
        token: String,
        patientId: Long,
        desde: String?,
        hasta: String?,
        page: Int?,
    ): PatientConsultationsOutcome {
        if (token.isBlank() || token == "invalid-token") return PatientConsultationsOutcome.Unauthorized
        if (token == "forbidden-token") return PatientConsultationsOutcome.Forbidden
        if (patientId <= 0) return PatientConsultationsOutcome.NotFound

        val sampleEntries = listOf(
            ConsultationEntry(
                id = 101,
                paciente = patientId,
                medico = ConsultationDoctor(7, "Médica", "Prueba"),
                fechaAtencion = "2026-03-01T10:30:00-05:00",
                motivoConsulta = "Control general y revisión de presión arterial",
                presionArterial = "120/80",
                frecuenciaCardiaca = 72,
                temperatura = 36.5,
                saturacionO2 = 98,
                pesoKg = 70.5,
                tallaCm = 172.0,
                diagnostico = "Hipertensión esencial controlada",
                codigoCie10 = "I10",
                tratamiento = "Continuar con dieta baja en sodio y medicación habitual.",
                notas = "Paciente refiere sentirse bien.",
                creadoEn = "2026-03-01T11:00:00-05:00",
            ),
            ConsultationEntry(
                id = 102,
                paciente = patientId,
                medico = ConsultationDoctor(7, "Médica", "Prueba"),
                fechaAtencion = "2026-02-15T09:00:00-05:00",
                motivoConsulta = "Dolor de garganta y fiebre leve",
                presionArterial = "118/76",
                frecuenciaCardiaca = 78,
                temperatura = 37.8,
                saturacionO2 = 97,
                pesoKg = 70.0,
                tallaCm = 172.0,
                diagnostico = "Faringitis aguda",
                codigoCie10 = "J02.9",
                tratamiento = "Paracetamol 500mg cada 8 horas por 3 días.",
                notas = "Reposo relativo.",
                creadoEn = "2026-02-15T09:30:00-05:00",
            ),
        )

        val filtered = sampleEntries.filter { entry ->
            val dateStr = entry.fechaAtencion.substringBefore("T")
            val matchesDesde = desde == null || dateStr >= desde
            val matchesHasta = hasta == null || dateStr <= hasta
            matchesDesde && matchesHasta
        }

        return PatientConsultationsOutcome.Success(
            PatientConsultations(
                count = filtered.size,
                next = null,
                previous = null,
                entries = filtered,
            )
        )
    }
}
