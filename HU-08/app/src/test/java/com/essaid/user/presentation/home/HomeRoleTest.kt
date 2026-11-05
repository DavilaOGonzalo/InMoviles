package com.essaid.user.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeRoleTest {
    @Test
    fun `maps database and mobile role names to separate sections`() {
        assertEquals(HomeRole.Doctor, HomeRole.from("medico"))
        assertEquals(HomeRole.Doctor, HomeRole.from("DOCTOR"))
        assertEquals(HomeRole.Patient, HomeRole.from("paciente"))
        assertEquals(HomeRole.Patient, HomeRole.from("PATIENT"))
    }

    @Test
    fun `does not default unassigned or unknown roles to patient section`() {
        assertEquals(HomeRole.Unsupported, HomeRole.from(null))
        assertEquals(HomeRole.Unsupported, HomeRole.from(""))
        assertEquals(HomeRole.Unsupported, HomeRole.from("staff"))
    }
}
