package com.essaid.user.data.remote

import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.AuthenticatedUser
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SupabaseLoginHttpClient(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val apiKey: String = SupabaseConfig.ANON_KEY,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) : UserLoginDataSource {

    override suspend fun login(request: UserLoginRequest): LoginOutcome {
        val url = URL("$baseUrl/auth/v1/token?grant_type=password")
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
            val payload = JSONObject()
                .put("email", request.email.trim())
                .put("password", request.password)
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(StandardCharsets.UTF_8))
            }

            when (val status = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    val response = JSONObject(body)
                    val accessToken = response.optString("access_token", "")
                    val userObj = response.optJSONObject("user") ?: JSONObject()
                    val userMetadata = userObj.optJSONObject("user_metadata") ?: JSONObject()

                    val name = listOf(
                        userMetadata.optString("nombre"),
                        userMetadata.optString("apellido")
                    ).filter(String::isNotBlank).joinToString(" ").ifBlank { request.email }

                    val role = userMetadata.optString("role").ifBlank {
                        if (request.email.contains("medico")) "medico" else "paciente"
                    }

                    val userId = 1L

                    LoginOutcome.Success(
                        session = AuthSession(
                            token = accessToken,
                            user = AuthenticatedUser(
                                id = userId,
                                email = request.email,
                                name = name,
                                role = role,
                            ),
                        ),
                    )
                }
                HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_UNAUTHORIZED ->
                    LoginOutcome.InvalidCredentials("Correo o contraseña incorrectos en Supabase.")
                else -> LoginOutcome.ServerError(statusCode = status)
            }
        } catch (_: IOException) {
            LoginOutcome.NetworkError()
        } catch (_: Exception) {
            LoginOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }
}
