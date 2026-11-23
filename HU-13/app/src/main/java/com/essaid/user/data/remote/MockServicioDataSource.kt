package com.essaid.user.data.remote

import com.essaid.user.domain.model.ServicioMedico

class MockServicioDataSource {
    private val servicios = mutableListOf(
        ServicioMedico("SRV-01", "H-001", "Medicina General", "Atención primaria", 30.0, true)
    )
    fun actualizarDisponibilidad(id: String, disponible: Boolean): ServicioMedico {
        val idx = servicios.indexOfFirst { it.id == id }
        val actualizado = servicios[idx].copy(disponible = disponible)
        servicios[idx] = actualizado
        return actualizado
    }
}
