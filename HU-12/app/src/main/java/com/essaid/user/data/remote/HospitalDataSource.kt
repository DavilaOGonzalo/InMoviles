package com.essaid.user.data.remote

import com.essaid.user.domain.model.Hospital

interface HospitalDataSource {
    suspend fun listarHospitales(token: String): List<Hospital>
}
