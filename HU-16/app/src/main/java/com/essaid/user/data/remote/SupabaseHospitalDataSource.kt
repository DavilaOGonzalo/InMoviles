package com.essaid.user.data.remote

import com.essaid.user.domain.model.Hospital
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SupabaseHospitalDataSource(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val apiKey: String = SupabaseConfig.ANON_KEY,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) : HospitalDataSource {

    override suspend fun listarHospitales(token: String): List<Hospital> {
        val urlString = "$baseUrl/rest/v1/hospitales?select=*&activo=eq.true"
        val connection = connectionFactory(URL(urlString)).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer ${if (token.isNotBlank()) token else apiKey}")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                val jsonArray = JSONArray(body)
                (0 until jsonArray.length()).map { i ->
                    val obj = jsonArray.getJSONObject(i)
                    Hospital(
                        id = obj.optString("id", ""),
                        nombre = obj.optString("nombre", ""),
                        direccion = obj.optString("direccion", ""),
                        region = obj.optString("region", "Norte"),
                        telefono = obj.optString("telefono", ""),
                        activo = obj.optBoolean("activo", true),
                    )
                }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }
}
