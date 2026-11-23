package com.essaid.user.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.BuildConfig
import com.essaid.user.di.AppContainer
import com.essaid.user.domain.model.AuthSession
import com.essaid.user.presentation.profile.PatientProfileScreen
import com.essaid.user.presentation.profile.PatientProfileViewModel
import com.essaid.user.presentation.patienthistory.PatientMedicalHistoryViewModel
import com.essaid.user.presentation.patienthistory.PatientAtencionesViewModel
import com.essaid.user.presentation.patientsearch.PatientSearchScreen
import com.essaid.user.presentation.patientsearch.PatientSearchViewModel
import com.essaid.user.presentation.hospital.HospitalDirectoryScreen
import com.essaid.user.presentation.hospital.HospitalViewModel
import com.essaid.user.presentation.citas.CitasScreen
import com.essaid.user.presentation.citas.CitasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    session: AuthSession,
    onLogout: () -> Unit,
) {
    val profileViewModel = remember {
        PatientProfileViewModel(
            repository = AppContainer.patientProfileRepository,
            isDemoMode = BuildConfig.USE_MOCK_API,
        )
    }
    val searchViewModel = remember {
        PatientSearchViewModel(AppContainer.patientSearchRepository)
    }
    val patientMedicalHistoryViewModel = remember {
        PatientMedicalHistoryViewModel(AppContainer.patientMedicalHistoryRepository)
    }
    val patientAtencionesViewModel = remember {
        PatientAtencionesViewModel(AppContainer.patientAtencionesRepository)
    }
    val hospitalViewModel = remember {
        HospitalViewModel(AppContainer.hospitalRepository)
    }
    val citasViewModel = remember {
        CitasViewModel(AppContainer.citaRepository)
    }

    val homeRole = HomeRole.from(session.user.role)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (homeRole == HomeRole.Patient) "Portal Paciente - EssAid" else "Portal Médico - EssAid") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar Sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                when (homeRole) {
                    HomeRole.Patient -> {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text("Mi Perfil") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Hospitales") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            label = { Text("Citas") }
                        )
                    }
                    HomeRole.Doctor -> {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Pacientes") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Hospitales & Staff") }
                        )
                    }
                    HomeRole.Unsupported -> {}
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (homeRole) {
                HomeRole.Patient -> {
                    when (selectedTab) {
                        0 -> PatientProfileScreen(
                            viewModel = profileViewModel,
                            token = session.token,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        1 -> HospitalDirectoryScreen(
                            viewModel = hospitalViewModel,
                            token = session.token,
                            isStaff = false
                        )
                        2 -> CitasScreen(
                            viewModel = citasViewModel,
                            token = session.token,
                            pacienteId = session.user.id?.toString() ?: "P-001"
                        )
                    }
                }
                HomeRole.Doctor -> {
                    when (selectedTab) {
                        0 -> PatientSearchScreen(
                            viewModel = searchViewModel,
                            medicalHistoryViewModel = patientMedicalHistoryViewModel,
                            atencionesViewModel = patientAtencionesViewModel,
                            token = session.token,
                            isDemoMode = BuildConfig.USE_MOCK_API,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        1 -> HospitalDirectoryScreen(
                            viewModel = hospitalViewModel,
                            token = session.token,
                            isStaff = true
                        )
                    }
                }
                HomeRole.Unsupported -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("No tienes un perfil médico o paciente asignado.")
                    Text("Contacta al administrador para revisar el rol de la cuenta.")
                }
            }
        }
    }
}
