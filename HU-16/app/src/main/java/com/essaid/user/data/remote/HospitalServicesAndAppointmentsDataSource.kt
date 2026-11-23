package com.essaid.user.data.remote

import com.essaid.user.domain.model.BloqueHorario
import com.essaid.user.domain.model.CitaMedica
import com.essaid.user.domain.model.ServicioMedico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface HospitalServicesDataSource {
    suspend fun listarServicios(token: String, hospitalId: String): List<ServicioMedico>
    suspend fun actualizarDisponibilidadServicio(token: String, servicioId: String, disponible: Boolean): ServicioMedico
}

interface CitaDataSource {
    suspend fun listarBloquesHorarios(token: String, hospitalId: String, fecha: String): List<BloqueHorario>
    suspend fun reservarCita(token: String, pacienteId: String, hospitalId: String, fecha: String, hora: String): CitaMedica
    suspend fun listarCitasPaciente(token: String, pacienteId: String): List<CitaMedica>
    suspend fun cancelarCita(token: String, citaId: String, motivo: String): Boolean
}

class MockHospitalServicesAndCitasDataSource : HospitalServicesDataSource, CitaDataSource {
    private val servicios = mutableListOf(
        ServicioMedico("SRV-01", "H-001", "Medicina General", "Atención primaria y prevención", 30.0, true),
        ServicioMedico("SRV-02", "H-001", "Pediatría", "Atención integral infantil", 40.0, true),
        ServicioMedico("SRV-03", "H-002", "Cardiología", "Evaluación cardiovascular", 60.0, true),
        ServicioMedico("SRV-04", "H-002", "Odontología", "Salud bucal y dental", 35.0, false)
    )

    private val citas = mutableListOf(
        CitaMedica("CITA-101", "P-001", "H-001", "2025-06-10", "09:00", "PROGRAMADA"),
        CitaMedica("CITA-102", "P-001", "H-002", "2025-06-15", "11:30", "PROGRAMADA")
    )

    override suspend fun listarServicios(token: String, hospitalId: String): List<ServicioMedico> = withContext(Dispatchers.IO) {
        servicios.filter { it.hospitalId == hospitalId }
    }

    override suspend fun actualizarDisponibilidadServicio(token: String, servicioId: String, disponible: Boolean): ServicioMedico = withContext(Dispatchers.IO) {
        val idx = servicios.indexOfFirst { it.id == servicioId }
        if (idx >= 0) {
            val actualizado = servicios[idx].copy(disponible = disponible)
            servicios[idx] = actualizado
            actualizado
        } else {
            throw IllegalArgumentException("Servicio no encontrado")
        }
    }

    override suspend fun listarBloquesHorarios(token: String, hospitalId: String, fecha: String): List<BloqueHorario> = withContext(Dispatchers.IO) {
        listOf(
            BloqueHorario("08:00", true),
            BloqueHorario("09:00", true),
            BloqueHorario("10:00", false),
            BloqueHorario("11:00", true),
            BloqueHorario("14:00", true),
            BloqueHorario("15:00", false),
            BloqueHorario("16:00", true)
        )
    }

    override suspend fun reservarCita(token: String, pacienteId: String, hospitalId: String, fecha: String, hora: String): CitaMedica = withContext(Dispatchers.IO) {
        val nuevaCita = CitaMedica(
            id = "CITA-${System.currentTimeMillis()}",
            pacienteId = pacienteId,
            hospitalId = hospitalId,
            fecha = fecha,
            hora = hora,
            estado = "PROGRAMADA"
        )
        citas.add(nuevaCita)
        nuevaCita
    }

    override suspend fun listarCitasPaciente(token: String, pacienteId: String): List<CitaMedica> = withContext(Dispatchers.IO) {
        citas.filter { it.pacienteId == pacienteId }
    }

    override suspend fun cancelarCita(token: String, citaId: String, motivo: String): Boolean = withContext(Dispatchers.IO) {
        val idx = citas.indexOfFirst { it.id == citaId }
        if (idx >= 0) {
            citas[idx] = citas[idx].copy(estado = "CANCELADA")
            true
        } else {
            false
        }
    }
}
