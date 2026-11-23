package com.essaid.user.domain.validation

import com.essaid.user.domain.model.RegistrationField
import com.essaid.user.domain.model.RegistrationForm
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object RegistrationValidator {
    private val allowedEmail = Regex(
        """^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@(gmail\.com|tecsup\.edu\.pe)$""",
        RegexOption.IGNORE_CASE,
    )
    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun validate(form: RegistrationForm): Map<String, String> {
        val errors = linkedMapOf<String, String>()
        val email = form.email.trim()
        val documentNumber = form.numeroDocumento.trim()
        val verifier = form.caracterVerificador.trim()
        val phone = form.celular.trim()
        val birthDate = form.fechaNacimiento.trim()

        when {
            email.isEmpty() -> errors[RegistrationField.Email.wireName] = "Ingresa tu correo electrónico."
            !allowedEmail.matches(email) -> errors[RegistrationField.Email.wireName] =
                "Usa un correo que termine en @gmail.com o @tecsup.edu.pe."
        }

        when {
            form.password1.isEmpty() -> errors[RegistrationField.Password1.wireName] =
                "Ingresa una contraseña."
            form.password1.length < 8 -> errors[RegistrationField.Password1.wireName] =
                "La contraseña debe tener al menos 8 caracteres."
            form.password1.all { it.isDigit() } -> errors[RegistrationField.Password1.wireName] =
                "La contraseña no puede estar formada solo por números."
        }

        when {
            form.password2.isEmpty() -> errors[RegistrationField.Password2.wireName] =
                "Confirma tu contraseña."
            form.password1 != form.password2 -> errors[RegistrationField.Password2.wireName] =
                "Las contraseñas no coinciden."
        }

        if (form.tipoDocumento !in setOf("DNI", "CE")) {
            errors[RegistrationField.TipoDocumento.wireName] = "Selecciona un tipo de documento."
        }

        when {
            documentNumber.isEmpty() -> errors[RegistrationField.NumeroDocumento.wireName] =
                "Ingresa tu número de documento."
            documentNumber.length > 20 -> errors[RegistrationField.NumeroDocumento.wireName] =
                "El documento no puede superar 20 caracteres."
        }

        when (form.tipoDocumento) {
            "DNI" -> when {
                verifier.isEmpty() -> errors[RegistrationField.CaracterVerificador.wireName] =
                    "Ingresa el carácter verificador del DNI."
                verifier.length > 1 -> errors[RegistrationField.CaracterVerificador.wireName] =
                    "El carácter verificador debe tener un solo carácter."
            }
            "CE" -> if (verifier.isNotEmpty()) {
                errors[RegistrationField.CaracterVerificador.wireName] =
                    "El CE no lleva carácter verificador."
            }
        }

        when {
            birthDate.isEmpty() -> errors[RegistrationField.FechaNacimiento.wireName] =
                "Ingresa tu fecha de nacimiento."
            else -> try {
                if (LocalDate.parse(birthDate, isoDateFormatter).isAfter(LocalDate.now())) {
                    errors[RegistrationField.FechaNacimiento.wireName] =
                        "La fecha de nacimiento no puede ser futura."
                }
            } catch (_: DateTimeParseException) {
                errors[RegistrationField.FechaNacimiento.wireName] =
                    "Usa el formato AAAA-MM-DD."
            }
        }

        when {
            phone.isEmpty() -> errors[RegistrationField.Celular.wireName] = "Ingresa tu número de celular."
            phone.length > 20 -> errors[RegistrationField.Celular.wireName] =
                "El celular no puede superar 20 caracteres."
        }

        if (!form.terminosAceptados) {
            errors[RegistrationField.TerminosAceptados.wireName] =
                "Debes aceptar los términos y condiciones."
        }
        if (!form.tratamientoDatosAutorizado) {
            errors[RegistrationField.TratamientoDatosAutorizado.wireName] =
                "Debes autorizar el tratamiento de tus datos."
        }

        return errors
    }
}
