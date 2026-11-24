package com.essaid.user.data.remote

import com.essaid.user.domain.model.MedicalHistoryCategory
import com.essaid.user.domain.model.MedicalHistoryEntry
import com.essaid.user.domain.model.PatientMedicalHistory
import com.essaid.user.domain.model.PatientMedicalHistoryOutcome
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SupabasePatientMedicalHistoryHttpClient(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val apiKey: String = SupabaseConfig.ANON_KEY,
    private val connectionFactory: (URL) -> HttpURLConnection = {
        it.openConnection() as HttpURLConnection
    },
) : PatientMedicalHistoryDataSource {

    override suspend fun getHistory(token: String, patientId: Long): PatientMedicalHistoryOutcome {
        val urlString = "$baseUrl/rest/v1/medical_profiles?patient_id_num=eq.$patientId&limit=1"
        val connection = connectionFactory(URL(urlString)).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer ${if (token.isNotBlank()) token else apiKey}")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            when (val statusCode = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    val jsonArray = JSONArray(body)
                    val profileObj = if (jsonArray.length() > 0) jsonArray.getJSONObject(0) else null

                    val conditions = parseArray(profileObj?.optJSONArray("condiciones_cronicas"))
                    val allergies = parseArray(profileObj?.optJSONArray("alergias"))
                    val meds = parseArray(profileObj?.optJSONArray("medicamentos_actuales"))

                    val groups = MedicalHistoryCategory.entries.associateWith { category ->
                        when (category) {
                            MedicalHistoryCategory.Condition -> conditions.mapIndexed { idx, cond ->
                                MedicalHistoryEntry(
                                    id = (100 + idx).toLong(),
                                    description = cond,
                                    approximateDate = "2026-01-15",
                                    active = true,
                                    recordedBy = "Dr. Asignado EssAid",
                                    createdAt = "2026-01-15T09:00:00Z"
                                )
                            }
                            MedicalHistoryCategory.Allergy -> allergies.mapIndexed { idx, alg ->
                                MedicalHistoryEntry(
                                    id = (200 + idx).toLong(),
                                    description = alg,
                                    approximateDate = "2026-01-15",
                                    active = true,
                                    recordedBy = "Dr. Asignado EssAid",
                                    createdAt = "2026-01-15T09:00:00Z"
                                )
                            }
                            MedicalHistoryCategory.Medication -> meds.mapIndexed { idx, med ->
                                MedicalHistoryEntry(
                                    id = (300 + idx).toLong(),
                                    description = med,
                                    approximateDate = "2026-02-01",
                                    active = true,
                                    recordedBy = "Dr. Asignado EssAid",
                                    createdAt = "2026-02-01T10:00:00Z"
                                )
                            }
                            else -> emptyList()
                        }
                    }
                    PatientMedicalHistoryOutcome.Success(PatientMedicalHistory(groups))
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PatientMedicalHistoryOutcome.Unauthorized
                else -> PatientMedicalHistoryOutcome.ServerError(statusCode)
            }
        } catch (_: IOException) {
            PatientMedicalHistoryOutcome.NetworkError
        } catch (_: Exception) {
            PatientMedicalHistoryOutcome.ServerError()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseArray(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { arr.getString(it) }.filter { it.isNotBlank() }
    }
}
