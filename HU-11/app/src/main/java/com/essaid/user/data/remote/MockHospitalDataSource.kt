package com.essaid.user.data.remote

import com.essaid.user.domain.model.Hospital

class MockHospitalDataSource : HospitalDataSource {
    private val lista = listOf(
        Hospital("H-001", "Hospital Nacional EssAid Lima Centro", "Av. Grau 123", "Lima", "01-432-1111", true),
        Hospital("H-002", "Hospital EssAid Norte", "Av. Universitaria 456", "Lima", "01-521-2222", true)
    )
    override suspend fun listarHospitales(token: String): List<Hospital> = lista
}
