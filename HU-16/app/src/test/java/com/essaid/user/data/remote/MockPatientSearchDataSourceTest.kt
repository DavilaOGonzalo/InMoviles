package com.essaid.user.data.remote

import com.essaid.user.domain.model.PatientSearchOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockPatientSearchDataSourceTest {
    private val dataSource = MockPatientSearchDataSource()

    @Test
    fun `searches fictitious patient by exact seeded DNI`() = runTest {
        val result = dataSource.searchPatients("doctor-token", "88000003")

        assertTrue(result is PatientSearchOutcome.Success)
        val patients = (result as PatientSearchOutcome.Success).patients
        assertEquals(1, patients.size)
        assertEquals("88000003", patients.single().dni)
        assertEquals("Andrea Paola", patients.single().nombre)
        assertEquals(listOf("Polvo doméstico (dato ficticio)"), patients.single().alergias)
        assertTrue(patients.single().observacionesMedicas.orEmpty().contains("ficticia"))
        assertEquals("Patricia Medina (Madre)", patients.single().contactoEmergencia)
        assertEquals("2026-03-01", patients.single().informacionMedicaActualizada)
    }

    @Test
    fun `searches seeded patients by partial name`() = runTest {
        val result = dataSource.searchPatients("doctor-token", "Rosa")

        assertTrue(result is PatientSearchOutcome.Success)
        assertEquals(1, (result as PatientSearchOutcome.Success).patients.size)
    }
}
