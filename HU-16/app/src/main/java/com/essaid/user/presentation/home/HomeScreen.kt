package com.essaid.user.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
        bottomBar = {
            NavigationBar {
                when (homeRole) {
                    HomeRole.Patient -> {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Inicio") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text("Mi Perfil") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Hospitales") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            label = { Text("Citas") }
                        )
                    }
                    HomeRole.Doctor -> {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Inicio") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text("Mi Perfil") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Pacientes") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Hospitales") }
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
                        0 -> InicioScreen(session = session, onNavigateToTab = { selectedTab = it })
                        1 -> PatientProfileScreen(
                            viewModel = profileViewModel,
                            token = session.token,
                            onLogout = onLogout,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        2 -> HospitalDirectoryScreen(
                            viewModel = hospitalViewModel,
                            token = session.token,
                            isStaff = false
                        )
                        3 -> CitasScreen(
                            viewModel = citasViewModel,
                            token = session.token,
                            pacienteId = session.user.id?.toString() ?: "P-001"
                        )
                    }
                }
                HomeRole.Doctor -> {
                    when (selectedTab) {
                        0 -> InicioScreen(session = session, onNavigateToTab = { selectedTab = it })
                        1 -> DoctorProfileScreen(session = session, onLogout = onLogout)
                        2 -> PatientSearchScreen(
                            viewModel = searchViewModel,
                            medicalHistoryViewModel = patientMedicalHistoryViewModel,
                            atencionesViewModel = patientAtencionesViewModel,
                            token = session.token,
                            isDemoMode = BuildConfig.USE_MOCK_API,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        3 -> HospitalDirectoryScreen(
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

@Composable
fun DoctorProfileScreen(
    session: AuthSession,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Perfil Profesional del Médico", style = MaterialTheme.typography.titleLarge)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=300",
                            contentDescription = "Foto del doctor",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column {
                        Text(session.user.name ?: "Dr. Médico EssAid", style = MaterialTheme.typography.titleMedium)
                        Text(session.user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        Text("Rol: Médico Especialista", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider()
                Text("Colegiatura CMP: CMP 45892", style = MaterialTheme.typography.bodyMedium)
                Text("Hospital Asignado: Hospital Norte EssAid", style = MaterialTheme.typography.bodyMedium)
                Text("Especialidad: Medicina General y Cardiología", style = MaterialTheme.typography.bodyMedium)
                Text("Estado: Activo en sistema EssAid", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Cerrar Sesión")
        }
    }
}
