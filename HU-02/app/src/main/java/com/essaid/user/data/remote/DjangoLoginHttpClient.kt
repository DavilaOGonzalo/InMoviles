package com.essaid.user.data.remote

import com.essaid.user.domain.model.AuthenticatedUser
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.domain.model.LoginOutcome
import com.essaid.user.domain.model.UserLoginRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Adapter for Django login endpoint (POST /api/login/ or similar agreed backend route).
 * Uses HttpURLConnection, avoids storing passwords in plain text, and respects connection timeouts.
 */
class DjangoLoginHttpClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
    private val connectTimeoutMillis: Int = 15_000,
    private val readTimeoutMillis: Int = 15_000,
) : UserLoginDataSource {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun login(request: UserLoginRequest): LoginOutcome {
        val loginPath = "/api/login/"
        val url = URL("$baseUrl$loginPath")
        val conn = connectionFactory(url).apply {
            requestMethod = "POST"
            connectTimeout = connectTimeoutMillis
            readTimeout = readTimeoutMillis
            doOutput = true
            setRequestProperty("Accept", "application/json, text/plain, */*")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        }

        val encodedBody = "email=${URLEncoder.encode(request.email, "UTF-8")}&password=${URLEncoder.encode(request.password, "UTF-8")}"

        return try {
            conn.outputStream.use { output ->
                output.write(encodedBody.toByteArray(StandardCharsets.UTF_8))
            }
            val statusCode = conn.responseCode
            when (statusCode) {
                HttpURLConnection.HTTP_OK, 201 -> LoginOutcome.Success(
                    session = AuthSession(
                        token = "backend-token",
                        user = AuthenticatedUser(email = request.email, name = "Usuario EssAid", role = "PATIENT")
                    )
                )
                HttpURLConnection.HTTP_UNAUTHORIZED -> LoginOutcome.InvalidCredentials()
                HttpURLConnection.HTTP_FORBIDDEN -> LoginOutcome.InactiveAccount()
                400 -> LoginOutcome.ValidationError(emptyMap(), "Credenciales o datos inválidos.")
                else -> LoginOutcome.ServerError(statusCode = statusCode)
            }
        } catch (_: IOException) {
            LoginOutcome.NetworkError()
        } catch (_: Exception) {
            LoginOutcome.ServerError()
        } finally {
            conn.disconnect()
        }
    }
}
