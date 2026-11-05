package com.essaid.user.data.remote

import com.essaid.user.domain.model.RegistrationField
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserRegistrationRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Adapter for the already existing Django form endpoint.
 *
 * This is deliberately not a guessed JSON endpoint. The inspected backend
 * exposes GET/POST /registro/, uses a CSRF cookie/token and redirects to
 * /registro/completado/ after success. Once the team publishes the JSON API,
 * this data source can be replaced without changing the repository or UI.
 */
class DjangoRegistrationHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : UserRegistrationDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome {
        val cookies = CookieJar()
        val formPage = openConnection(DjangoRegistrationContract.registrationPath).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "text/html")
        }

        val formHtml = try {
            val body = readBody(formPage)
            cookies.capture(formPage)
            body
        } finally {
            formPage.disconnect()
        }

        val csrfToken = extractCsrfToken(formHtml) ?: return RegistrationOutcome.ServerError(
            message = "El formulario de registro no entregó un token CSRF.",
        )

        val post = openConnection(DjangoRegistrationContract.registrationPath).apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            doOutput = true
            setRequestProperty("Accept", "text/html")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setRequestProperty("Cookie", cookies.headerValue())
            setRequestProperty("Referer", "$baseUrl${DjangoRegistrationContract.registrationPath}")
        }

        val fields = linkedMapOf(
            "csrfmiddlewaretoken" to csrfToken,
            "email" to request.email,
            "password1" to request.password1,
            "password2" to request.password2,
            "tipo_documento" to request.tipoDocumento,
            "numero_documento" to request.numeroDocumento,
            "caracter_verificador" to request.caracterVerificador,
            "fecha_nacimiento" to request.fechaNacimiento,
            "celular" to request.celular,
        )
        if (request.terminosAceptados) {
            fields["terminos_aceptados"] = "on"
        }
        if (request.tratamientoDatosAutorizado) {
            fields["tratamiento_datos_autorizado"] = "on"
        }

        return try {
            val encodedBody = fields.entries.joinToString("&") { (name, value) ->
                "${urlEncode(name)}=${urlEncode(value)}"
            }
            post.outputStream.use { output ->
                output.write(encodedBody.toByteArray(StandardCharsets.UTF_8))
            }

            val statusCode = post.responseCode
            val location = post.getHeaderField("Location").orEmpty()
            val responseHtml = readBody(post)
            when {
                statusCode in 300..399 && location.contains(
                    DjangoRegistrationContract.completionPath,
                ) -> RegistrationOutcome.Success()

                statusCode == HttpURLConnection.HTTP_CONFLICT -> duplicateFromHtml(responseHtml)
                statusCode in 200..299 -> outcomeFromHtml(responseHtml)
                statusCode in 400..499 -> outcomeFromHtml(responseHtml, statusCode)
                else -> RegistrationOutcome.ServerError(statusCode = statusCode)
            }
        } finally {
            post.disconnect()
        }
    }

    private fun openConnection(path: String): HttpURLConnection =
        connectionFactory(URL("$baseUrl$path")).apply {
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            useCaches = false
        }

    private fun readBody(connection: HttpURLConnection): String {
        val statusCode = connection.responseCode
        val stream = if (statusCode >= 400) connection.errorStream else connection.inputStream
        return stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
    }

    private fun extractCsrfToken(html: String): String? {
        val direct = Regex(
            """<input[^>]+name=[\"']csrfmiddlewaretoken[\"'][^>]+value=[\"']([^\"']+)[\"']""",
            RegexOption.IGNORE_CASE,
        ).find(html)?.groupValues?.getOrNull(1)
        if (direct != null) return direct

        return Regex(
            """<input[^>]+value=[\"']([^\"']+)[\"'][^>]+name=[\"']csrfmiddlewaretoken[\"']""",
            RegexOption.IGNORE_CASE,
        ).find(html)?.groupValues?.getOrNull(1)
    }

    private fun outcomeFromHtml(html: String, statusCode: Int? = null): RegistrationOutcome {
        val text = htmlToText(html)
        val lower = text.lowercase()
        return when {
            lower.contains("ya existe") -> duplicateFromHtml(text)
            statusCode == HttpURLConnection.HTTP_FORBIDDEN && lower.contains("csrf") ->
                RegistrationOutcome.ServerError(
                    message = "La sesión de registro expiró. Inténtalo nuevamente.",
                    statusCode = statusCode,
                )
            else -> RegistrationOutcome.ValidationError(
                errors = mapOf(RegistrationField.General.wireName to listOf(
                    "Revisa los datos del formulario y vuelve a intentarlo.",
                )),
            )
        }
    }

    private fun duplicateFromHtml(html: String): RegistrationOutcome.Duplicate {
        val text = htmlToText(html)
        val lower = text.lowercase()
        val errors = linkedMapOf<String, List<String>>()
        if (lower.contains("correo") || lower.contains("cuenta")) {
            errors[RegistrationField.Email.wireName] = listOf(
                "Ya existe una cuenta con ese correo.",
            )
        }
        if (lower.contains("documento") || lower.contains("paciente")) {
            errors[RegistrationField.NumeroDocumento.wireName] = listOf(
                "Ya existe un paciente con ese tipo y número de documento.",
            )
        }
        if (errors.isEmpty()) {
            errors[RegistrationField.General.wireName] = listOf(
                "Ya existe una cuenta o documento con esos datos.",
            )
        }
        return RegistrationOutcome.Duplicate(errors)
    }

    private fun htmlToText(html: String): String = html
        .replace(Regex("<[^>]*>"), " ")
        .replace("&nbsp;", " ", ignoreCase = true)
        .replace("&aacute;", "á", ignoreCase = true)
        .replace("&eacute;", "é", ignoreCase = true)
        .replace("&iacute;", "í", ignoreCase = true)
        .replace("&oacute;", "ó", ignoreCase = true)
        .replace("&uacute;", "ú", ignoreCase = true)
        .replace("&ntilde;", "ñ", ignoreCase = true)
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}

private object DjangoRegistrationContract {
    const val registrationPath = "/registro/"
    const val completionPath = "/registro/completado/"
}

private class CookieJar {
    private val values = linkedMapOf<String, String>()

    fun capture(connection: HttpURLConnection) {
        connection.headerFields.entries
            .filter { it.key.equals("Set-Cookie", ignoreCase = true) }
            .flatMap { it.value.orEmpty() }
            .forEach { header ->
                val pair = header.substringBefore(';')
                val separator = pair.indexOf('=')
                if (separator > 0) {
                    values[pair.substring(0, separator)] = pair.substring(separator + 1)
                }
            }
    }

    fun headerValue(): String = values.entries.joinToString("; ") { (name, value) ->
        "$name=$value"
    }
}
