package com.essaid.user.domain.validation

object HospitalSearchValidator {
    fun validarTerminoUbicacion(termino: String): Boolean {
        return termino.isNotBlank() && termino.length >= 2
    }
}
