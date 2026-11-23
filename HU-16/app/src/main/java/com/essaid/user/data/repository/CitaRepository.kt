package com.essaid.user.data.repository

import com.essaid.user.data.remote.CitaDataSource
import com.essaid.user.domain.model.BloqueHorario
import com.essaid.user.domain.model.CitaMedica
import com.essaid.user.domain.model.MisCitasOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CitaRepository(private val dataSource: CitaDataSource) {

    suspend fun obtenerBloquesHorarios(token: String, hospitalId: String, fecha: String): List<BloqueHorario> = withContext(Dispatchers.IO) {
        try {
            dataSource.listarBloquesHorarios(token, hospitalId, fecha)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun reservarCita(token: String, pacienteId: String, hospitalId: String, fecha: String, hora: String): Result<CitaMedica> = withContext(Dispatchers.IO) {
        try {
            Result.success(dataSource.reservarCita(token, pacienteId, hospitalId, fecha, hora))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerCitasPaciente(token: String, pacienteId: String): MisCitasOutcome = withContext(Dispatchers.IO) {
        try {
            val lista = dataSource.listarCitasPaciente(token, pacienteId)
            if (lista.isEmpty()) MisCitasOutcome.Empty else MisCitasOutcome.Success(lista)
        } catch (_: Exception) {
            MisCitasOutcome.Empty
        }
    }

    suspend fun cancelarCita(token: String, citaId: String, motivo: String): Boolean = withContext(Dispatchers.IO) {
        try {
            dataSource.cancelarCita(token, citaId, motivo)
        } catch (_: Exception) {
            false
        }
    }
}
