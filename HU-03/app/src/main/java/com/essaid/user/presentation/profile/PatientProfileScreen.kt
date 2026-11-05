package com.essaid.user.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PersonalPatientInfo

@Composable
fun PatientProfileScreen(
    viewModel: PatientProfileViewModel,
    token: String,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token) {
        if (state.status == ProfileStatus.Idle) {
            viewModel.loadProfile(token)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Mi Perfil Personal y Médico",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            OutlinedButton(
                onClick = { viewModel.loadProfile(token) },
                enabled = state.status != ProfileStatus.Loading,
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Actualizar")
            }
        }

        when (state.status) {
            ProfileStatus.Idle, ProfileStatus.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Consultando información del paciente...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            ProfileStatus.Empty -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.errorMessage ?: "No se encontró información personal o médica.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile(token) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            ProfileStatus.Error, ProfileStatus.Unauthorized -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.errorMessage ?: "Ocurrió un error al consultar el perfil.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile(token) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            ProfileStatus.Success -> {
                state.profile?.let { profile ->
                    PersonalSection(profile.personal)
                    MedicalSection(profile.medical)
                }
            }
        }
    }
}

@Composable
private fun PersonalSection(personal: PersonalPatientInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Información Personal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow("Nombres y Apellidos", "${personal.nombre} ${personal.apellido}")
            InfoRow("Correo Electrónico", personal.email)
            InfoRow("Documento de Identidad", "${personal.tipoDocumento}: ${personal.numeroDocumento}")
            InfoRow("Celular", personal.celular)
            InfoRow("Fecha de Nacimiento", personal.fechaNacimiento)
            personal.genero?.let { InfoRow("Género", it) }
        }
    }
}

@Composable
private fun MedicalSection(medical: MedicalPatientInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Información Médica",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow("Tipo de Sangre", medical.tipoSangre ?: "No especificado")
            InfoRow("Alergias", if (medical.alergias.isNotEmpty()) medical.alergias.joinToString(", ") else "Ninguna registrada")
            InfoRow("Condiciones Crónicas", if (medical.condicionesCronicas.isNotEmpty()) medical.condicionesCronicas.joinToString(", ") else "Ninguna registrada")
            InfoRow("Medicamentos Actuales", if (medical.medicamentosActuales.isNotEmpty()) medical.medicamentosActuales.joinToString(", ") else "Ninguno")
            if (!medical.contactoEmergenciaNombre.isNullOrBlank()) {
                InfoRow("Contacto de Emergencia", "${medical.contactoEmergenciaNombre} (${medical.contactoEmergenciaTelefono ?: ""})")
            }
            medical.observacionesMedicas?.let { InfoRow("Observaciones", it) }
            medical.ultimaActualizacion?.let { InfoRow("Última Actualización", it) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
