package com.essaid.user.domain.model

data class ServicioMedico(val id: String, val hospitalId: String, val nombreServicio: String, val descripcion: String, val costoReferencial: Double, val disponible: Boolean)
