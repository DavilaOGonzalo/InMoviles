package com.essaid.user.data.remote

import com.essaid.user.domain.model.RegisteredUser
import com.essaid.user.domain.model.RegistrationOutcome
import com.essaid.user.domain.model.UserRegistrationRequest
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SupabaseRegistrationHttpClient(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val apiKey: String = SupabaseConfig.ANON_KEY,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) : UserRegistrationDataSource {

    override suspend fun register(request: UserRegistrationRequest): RegistrationOutcome {
        val url = URL("$baseUrl/auth/v1/signup")
        val connection = connectionFactory(url).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val dataObj = JSONObject().apply {
                put("nombre", "Usuario")
                put("apellido", "Nuevo")
                put("role", "paciente")
                put("tipo_documento", request.tipoDocumento)
                put("numero_documento", request.numeroDocumento)
                put("celular", request.celular)
            }

            val payload = JSONObject()
                .put("email", request.email.trim())
                .put("password", request.password1)
                .put("data", dataObj)
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(StandardCharsets.UTF_8))
            }

            when (val status = connection.responseCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    RegistrationOutcome.Success(
                        message = "Cuenta registrada exitosamente en Supabase.",
                        user = RegisteredUser(id = 1L, email = request.email)
                    )
                }
                HttpURLConnection.HTTP_BAD_REQUEST, 422 -> {
                    RegistrationOutcome.Duplicate(emptyMap(), "Ya existe una cuenta con ese correo o documento.")
                }
                else -> RegistrationOutcome.ServerError(statusCode = status)
            }
        } catch (_: IOException) {
            RegistrationOutcome.NetworkError()
        } catch (_: Exception) {
            RegistrationOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }
}
