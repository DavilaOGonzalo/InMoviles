package com.essaid.user.domain.validation

import com.essaid.user.domain.model.LoginField
import com.essaid.user.domain.model.LoginForm

object LoginValidator {
    private val allowedEmail = Regex(
        """^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@(gmail\.com|tecsup\.edu\.pe)$""",
        RegexOption.IGNORE_CASE,
    )

    fun validate(form: LoginForm): Map<String, String> {
        val errors = linkedMapOf<String, String>()
        val email = form.email.trim()

        when {
            email.isEmpty() -> errors[LoginField.Email.wireName] = "Ingresa tu correo electrónico."
            !allowedEmail.matches(email) -> errors[LoginField.Email.wireName] =
                "Usa un correo que termine en @gmail.com o @tecsup.edu.pe."
        }

        when {
            form.password.isEmpty() -> errors[LoginField.Password.wireName] = "Ingresa tu contraseña."
        }

        return errors
    }
}
