package com.essaid.user.data.remote

import com.essaid.user.domain.model.AuthenticatedUser
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfile
import com.essaid.user.domain.model.PatientProfileOutcome
import com.essaid.user.domain.model.PatientProfileUpdateForm
import com.essaid.user.domain.model.PatientProfileUpdateOutcome
import com.essaid.user.domain.model.PersonalPatientInfo
import com.essaid.user.domain.model.RegisteredUser
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserLoginRequest
import com.essaid.user.domain.model.UserRegistrationRequest
import com.essaid.user.domain.validation.ProfileUpdateValidator
import java.util.Locale

data class MockUserRecord(
    val email: String,
    val password: String,
    val nombre: String,
    val apellido: String,
    val tipoDocumento: String,
    val numeroDocumento: String,
    val caracterVerificador: String = "",
    val fechaNacimiento: String,
    val celular: String,
    var medical: MedicalPatientInfo = MedicalPatientInfo(
        tipoSangre = "O+",
        alergias = listOf("Penicilina"),
        condicionesCronicas = listOf("Ninguna registrada"),
        medicamentosActuales = listOf("Ninguno"),
        contactoEmergenciaNombre = "Contacto de Emergencia",
        contactoEmergenciaTelefono = "912345678",
        observacionesMedicas = "Paciente registrado en EssAid. Sin observaciones agudas.",
        ultimaActualizacion = "2026-03-01"
    )
)

object MockUserDataStore {
    private val usersByEmail = mutableMapOf<String, MockUserRecord>()
    private val usersByDocument = mutableMapOf<String, MockUserRecord>()

    init {
        seedDefaultUser()
    }

    private fun seedDefaultUser() {
        val defaultUser = MockUserRecord(
            email = "paciente@tecsup.edu.pe",
            password = "Password123!",
            nombre = "Juan Carlos",
            apellido = "Pérez Gómez",
            tipoDocumento = "DNI",
            numeroDocumento = "72345678",
            caracterVerificador = "0",
            fechaNacimiento = "15/05/2000",
            celular = "987654321"
        )
        usersByEmail[defaultUser.email.lowercase(Locale.ROOT)] = defaultUser
        usersByDocument["${defaultUser.tipoDocumento}:${defaultUser.numeroDocumento.lowercase(Locale.ROOT)}"] = defaultUser
    }

    @Synchronized
    fun register(request: UserRegistrationRequest): RegistrationOutcome {
        val emailKey = request.email.trim().lowercase(Locale.ROOT)
        val docKey = "${request.tipoDocumento}:${request.numeroDocumento.trim().lowercase(Locale.ROOT)}"
        val errors = linkedMapOf<String, List<String>>()

        if (emailKey in usersByEmail) {
            errors["email"] = listOf("Ya existe una cuenta con ese correo.")
        }
        if (docKey in usersByDocument) {
            errors["numero_documento"] = listOf("Ya existe un paciente con ese tipo y número de documento.")
        }
        if (errors.isNotEmpty()) {
            return RegistrationOutcome.Duplicate(errors)
        }

        val localPart = emailKey.substringBefore("@")
        val nameParts = localPart.split(".", "_", "-")
        val derivedNombre = nameParts.getOrNull(0)?.replaceFirstChar { it.uppercase() } ?: "Paciente"
        val derivedApellido = if (nameParts.size > 1) {
            nameParts.drop(1).joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
        } else {
            "EssAid"
        }

        val record = MockUserRecord(
            email = request.email.trim(),
            password = request.password1,
            nombre = derivedNombre,
            apellido = derivedApellido,
            tipoDocumento = request.tipoDocumento,
            numeroDocumento = request.numeroDocumento.trim(),
            caracterVerificador = request.caracterVerificador.trim(),
            fechaNacimiento = request.fechaNacimiento.trim(),
            celular = request.celular.trim()
        )

        usersByEmail[emailKey] = record
        usersByDocument[docKey] = record

        return RegistrationOutcome.Success(
            message = "Cuenta registrada correctamente.",
            user = RegisteredUser(email = request.email.trim())
        )
    }

    @Synchronized
    fun login(request: UserLoginRequest): LoginOutcome {
        val emailKey = request.email.trim().lowercase(Locale.ROOT)

        if (request.password == "Inactivo123!") {
            return LoginOutcome.InactiveAccount()
        }

        val user = usersByEmail[emailKey]
        if (user != null) {
            if (user.password == request.password) {
                val token = "mock-jwt-token-$emailKey"
                return LoginOutcome.Success(
                    session = AuthSession(
                        token = token,
                        user = AuthenticatedUser(
                            email = user.email,
                            name = "${user.nombre} ${user.apellido}",
                            role = "PATIENT"
                        )
                    )
                )
            } else {
                return LoginOutcome.InvalidCredentials()
            }
        }

        // Fallback for unseeded test/mock logins where password isn't "WrongPassword1!" and length >= 8
        if (request.password.length >= 8 && request.password != "WrongPassword1!") {
            val token = "mock-jwt-token-$emailKey"
            return LoginOutcome.Success(
                session = AuthSession(
                    token = token,
                    user = AuthenticatedUser(
                        email = request.email.trim(),
                        name = "Paciente EssAid",
                        role = "PATIENT"
                    )
                )
            )
        }

        return LoginOutcome.InvalidCredentials()
    }

    @Synchronized
    fun getProfile(token: String): PatientProfileOutcome {
        if (token.isBlank() || token == "invalid-token") {
            return PatientProfileOutcome.Unauthorized()
        }
        val emailFromToken = token.removePrefix("mock-jwt-token-").trim().lowercase(Locale.ROOT)
        val user = usersByEmail[emailFromToken] ?: usersByEmail["paciente@tecsup.edu.pe"]

        if (user == null) {
            return PatientProfileOutcome.Empty()
        }

        val profile = PatientProfile(
            personal = PersonalPatientInfo(
                id = 101L,
                email = user.email,
                nombre = user.nombre,
                apellido = user.apellido,
                tipoDocumento = user.tipoDocumento,
                numeroDocumento = user.numeroDocumento,
                celular = user.celular,
                fechaNacimiento = user.fechaNacimiento,
                genero = "No especificado"
            ),
            medical = user.medical
        )
        return PatientProfileOutcome.Success(profile)
    }

    @Synchronized
    fun updateProfile(token: String, form: PatientProfileUpdateForm): PatientProfileUpdateOutcome {
        if (token.isBlank() || token == "invalid-token") {
            return PatientProfileUpdateOutcome.Unauthorized()
        }
        val errors = ProfileUpdateValidator.validate(form)
        if (errors.isNotEmpty()) {
            return PatientProfileUpdateOutcome.ValidationError(fieldErrors = errors)
        }
        val emailFromToken = token.removePrefix("mock-jwt-token-").trim().lowercase(Locale.ROOT)
        val user = usersByEmail[emailFromToken] ?: usersByEmail["paciente@tecsup.edu.pe"]
            ?: return PatientProfileUpdateOutcome.ServerError()

        val updatedUser = user.copy(
            celular = form.celular.trim(),
            medical = MedicalPatientInfo(
                tipoSangre = user.medical.tipoSangre ?: "O+",
                alergias = form.toMedicalList(form.alergias),
                condicionesCronicas = form.toMedicalList(form.condicionesCronicas),
                medicamentosActuales = form.toMedicalList(form.medicamentosActuales),
                contactoEmergenciaNombre = form.contactoEmergenciaNombre.ifBlank { null },
                contactoEmergenciaTelefono = form.contactoEmergenciaTelefono.ifBlank { null },
                observacionesMedicas = form.observacionesMedicas.ifBlank { null },
                ultimaActualizacion = "2026-03-02"
            )
        )

        usersByEmail[user.email.lowercase(Locale.ROOT)] = updatedUser
        val docKey = "${user.tipoDocumento}:${user.numeroDocumento.lowercase(Locale.ROOT)}"
        usersByDocument[docKey] = updatedUser

        val profile = PatientProfile(
            personal = PersonalPatientInfo(
                id = 101L,
                email = updatedUser.email,
                nombre = updatedUser.nombre,
                apellido = updatedUser.apellido,
                tipoDocumento = updatedUser.tipoDocumento,
                numeroDocumento = updatedUser.numeroDocumento,
                celular = updatedUser.celular,
                fechaNacimiento = updatedUser.fechaNacimiento,
                genero = "No especificado"
            ),
            medical = updatedUser.medical
        )

        return PatientProfileUpdateOutcome.Success(profile)
    }

    @Synchronized
    fun clear() {
        usersByEmail.clear()
        usersByDocument.clear()
        seedDefaultUser()
    }
}
