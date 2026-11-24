package com.essaid.user.domain.model

data class BloqueHorario(val hora: String, val disponible: Boolean)

data class EspecialidadMedica(
    val id: String,
    val nombre: String,
    val descripcion: String,
)

data class DoctorInfo(
    val id: String,
    val nombre: String,
    val especialidad: String,
    val hospitalId: String,
    val hospitalNombre: String,
    val colegiatura: String,
    val biografia: String,
    val imageUrl: String? = null,
)

data class DiaHorarios(
    val fecha: String,
    val diaLabel: String,
    val bloques: List<BloqueHorario>,
)
