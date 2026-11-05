package com.essaid.user.presentation.home

enum class HomeRole {
    Doctor,
    Patient,
    Unsupported,
    ;

    companion object {
        fun from(role: String?): HomeRole =
            when (role?.trim()?.lowercase()) {
                "medico", "doctor" -> Doctor
                "paciente", "patient" -> Patient
                else -> Unsupported
            }
    }
}
