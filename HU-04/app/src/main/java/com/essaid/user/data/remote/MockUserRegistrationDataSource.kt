package com.essaid.user.data.remote

import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.RegisteredUser
import com.essaid.user.domain.model.UserRegistrationRequest

/**
 * Temporary in-memory source used until the JSON contract for Android is
 * published. It keeps only duplicate-check keys and never stores passwords.
 */
class MockUserRegistrationDataSource : UserRegistrationDataSource {
    private val registeredEmails = mutableSetOf<String>()
    private val registeredDocuments = mutableSetOf<String>()

    override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome {
        return synchronized(this) {
            val email = request.email.lowercase()
            val document = "${request.tipoDocumento}:${request.numeroDocumento.lowercase()}"
            val errors = linkedMapOf<String, List<String>>()

            if (email in registeredEmails) {
                errors["email"] = listOf("Ya existe una cuenta con ese correo.")
            }
            if (document in registeredDocuments) {
                errors["numero_documento"] = listOf(
                    "Ya existe un paciente con ese tipo y número de documento.",
                )
            }
            if (errors.isNotEmpty()) {
                return@synchronized RegistrationOutcome.Duplicate(errors)
            }

            registeredEmails += email
            registeredDocuments += document
            RegistrationOutcome.Success(
                message = "Registro simulado correctamente. El backend real aún está pendiente.",
                user = RegisteredUser(email = request.email),
            )
        }
    }
}
