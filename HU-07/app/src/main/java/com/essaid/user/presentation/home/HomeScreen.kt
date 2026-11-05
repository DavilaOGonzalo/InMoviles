package com.essaid.user.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val homeRole = HomeRole.from(session.user.role)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cerrar Sesión")
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
                HomeRole.Doctor -> PatientSearchScreen(
                    viewModel = searchViewModel,
                    medicalHistoryViewModel = patientMedicalHistoryViewModel,
                    atencionesViewModel = patientAtencionesViewModel,
                    token = session.token,
                    isDemoMode = BuildConfig.USE_MOCK_API,
                    modifier = Modifier.fillMaxWidth(),
                )
                HomeRole.Patient -> PatientProfileScreen(
                    viewModel = profileViewModel,
                    token = session.token,
                    modifier = Modifier.fillMaxWidth(),
                )
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
