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

class DjangoJwtLoginHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : UserLoginDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun login(request: UserLoginRequest): LoginOutcome {
        val connection = connectionFactory(URL("$baseUrl/api/auth/token/")).apply {
            requestMethod = "POST"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }

        return try {
            val payload = JSONObject()
                .put("email", request.email)
                .put("password", request.password)
                .toString()
            connection.outputStream.use { output ->
                output.write(payload.toByteArray(StandardCharsets.UTF_8))
            }
            when (val status = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    val response = JSONObject(body)
                    val user = response.getJSONObject("user")
                    val name = listOf(user.optString("nombre"), user.optString("apellido"))
                        .filter(String::isNotBlank)
                        .joinToString(" ")
                        .ifBlank { null }
                    LoginOutcome.Success(
                        session = AuthSession(
                            token = response.getString("access"),
                            user = AuthenticatedUser(
                                id = user.optLong("id").takeIf { user.has("id") },
                                email = request.email,
                                name = name,
                                role = user.optString("rol").ifBlank { null },
                            ),
                        ),
                    )
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> LoginOutcome.InvalidCredentials()
                HttpURLConnection.HTTP_FORBIDDEN -> LoginOutcome.InactiveAccount()
                HttpURLConnection.HTTP_BAD_REQUEST ->
                    LoginOutcome.ValidationError(emptyMap(), "Credenciales o datos inválidos.")
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
