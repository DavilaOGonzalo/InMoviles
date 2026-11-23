package com.essaid.user.domain.model

data class Hospital(val id: String, val nombre: String, val direccion: String, val region: String, val telefono: String, val activo: Boolean)

sealed interface HospitalListOutcome {
    data class Success(val hospitales: List<Hospital>) : HospitalListOutcome
    data object Empty : HospitalListOutcome
    data object Unauthorized : HospitalListOutcome
    data class NetworkError(val message: String) : HospitalListOutcome
    data class ServerError(val code: Int, val message: String) : HospitalListOutcome
}
