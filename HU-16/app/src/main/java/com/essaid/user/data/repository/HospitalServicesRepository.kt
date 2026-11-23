package com.essaid.user.data.repository

import com.essaid.user.data.remote.HospitalServicesDataSource
import com.essaid.user.domain.model.ServicioMedico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ServicioListOutcome {
    data class Success(val servicios: List<ServicioMedico>) : ServicioListOutcome
    data object Empty : ServicioListOutcome
    data class Error(val message: String) : ServicioListOutcome
}

class HospitalServicesRepository(private val dataSource: HospitalServicesDataSource) {
    suspend fun obtenerServicios(token: String, hospitalId: String): ServicioListOutcome = withContext(Dispatchers.IO) {
        try {
            val lista = dataSource.listarServicios(token, hospitalId)
            if (lista.isEmpty()) ServicioListOutcome.Empty else ServicioListOutcome.Success(lista)
        } catch (e: Exception) {
            ServicioListOutcome.Error(e.message ?: "Error desconocido al obtener servicios")
        }
    }

    suspend fun actualizarDisponibilidad(token: String, servicioId: String, disponible: Boolean): Result<ServicioMedico> = withContext(Dispatchers.IO) {
        try {
            Result.success(dataSource.actualizarDisponibilidadServicio(token, servicioId, disponible))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
