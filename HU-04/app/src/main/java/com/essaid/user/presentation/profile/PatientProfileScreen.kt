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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Mi Perfil Personal y Médico",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

        // Success / General Error message banners
        state.successMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(onClick = { viewModel.clearSuccessMessage() }) {
                        Text("OK")
                    }
                }
            }
        }

        if (state.errorMessage != null && state.status == ProfileStatus.Success) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Text(
                        text = state.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                    )
                }
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
                if (state.isEditing) {
                    EditProfileSection(
                        viewModel = viewModel,
                    )
                } else {
                    state.profile?.let { profile ->
                        PersonalSection(profile.personal)
                        MedicalSection(profile.medical)
                    }
                }
            }
        }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.isEditing) {
                OutlinedButton(
                    onClick = viewModel::cancelEditing,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isUpdating,
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { viewModel.updateProfile(token) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isUpdating,
                ) {
                    if (state.isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.loadProfile(token) },
                    modifier = Modifier.weight(1f),
                    enabled = state.status != ProfileStatus.Loading && !state.isUpdating,
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Actualizar")
                }
                if (state.status == ProfileStatus.Success) {
                    Button(
                        onClick = viewModel::startEditing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Editar")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileSection(
    viewModel: PatientProfileViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.updateForm

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Editar Perfil (Campos Autorizados)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Puedes actualizar tu número celular, contacto de emergencia y datos médicos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = form.celular,
                onValueChange = viewModel::onCelularChanged,
                label = { Text("Celular (*)") },
                isError = state.errorFor("celular") != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            state.errorFor("celular")?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = form.contactoEmergenciaNombre,
                onValueChange = viewModel::onContactoEmergenciaNombreChanged,
                label = { Text("Contacto de Emergencia (Nombre)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = form.contactoEmergenciaTelefono,
                onValueChange = viewModel::onContactoEmergenciaTelefonoChanged,
                label = { Text("Contacto de Emergencia (Teléfono)") },
                isError = state.errorFor("contactoEmergenciaTelefono") != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            state.errorFor("contactoEmergenciaTelefono")?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = form.alergias,
                onValueChange = viewModel::onAlergiasChanged,
                label = { Text("Alergias (separadas por coma)") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = form.condicionesCronicas,
                onValueChange = viewModel::onCondicionesCronicasChanged,
                label = { Text("Condiciones Crónicas (separadas por coma)") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = form.medicamentosActuales,
                onValueChange = viewModel::onMedicamentosActualesChanged,
                label = { Text("Medicamentos Actuales (separados por coma)") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = form.observacionesMedicas,
                onValueChange = viewModel::onObservacionesMedicasChanged,
                label = { Text("Observaciones Médicas") },
                modifier = Modifier.fillMaxWidth(),
            )

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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Información Personal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Información Médica",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
