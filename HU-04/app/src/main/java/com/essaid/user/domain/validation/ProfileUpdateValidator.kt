package com.essaid.user.domain.validation

import com.essaid.user.domain.model.PatientProfileUpdateForm

object ProfileUpdateValidator {
    private val phoneRegex = Regex("^9\\d{8}$")

    fun validate(form: PatientProfileUpdateForm): Map<String, String> {
        val errors = linkedMapOf<String, String>()

        val celularClean = form.celular.trim()
        when {
            celularClean.isEmpty() -> errors["celular"] = "El número de celular es obligatorio."
            !phoneRegex.matches(celularClean) -> errors["celular"] = "El celular debe tener 9 dígitos y empezar con 9."
        }

        val telEmergencia = form.contactoEmergenciaTelefono.trim()
        if (telEmergencia.isNotEmpty() && !phoneRegex.matches(telEmergencia)) {
            errors["contactoEmergenciaTelefono"] = "El teléfono de emergencia debe tener 9 dígitos y empezar con 9."
        }

        return errors
    }
}
