package com.essaid.user.domain.model

sealed interface MisCitasOutcome {
    data class Success(val citas: List<CitaMedica>) : MisCitasOutcome
    data object Empty : MisCitasOutcome
}
