package com.essaid.user.data.repository

import com.essaid.user.data.remote.HospitalDataSource
import com.essaid.user.domain.model.HospitalListOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HospitalRepository(private val ds: HospitalDataSource) {
    suspend fun obtenerHospitales(token: String): HospitalListOutcome = withContext(Dispatchers.IO) {
        try {
            val res = ds.listarHospitales(token)
            if (res.isEmpty()) HospitalListOutcome.Empty else HospitalListOutcome.Success(res)
        } catch (e: Exception) {
            HospitalListOutcome.ServerError(500, e.message ?: "Error desconocido")
        }
    }
}
