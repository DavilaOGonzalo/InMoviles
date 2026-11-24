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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.essaid.user.domain.model.MedicalPatientInfo
import com.essaid.user.domain.model.PatientProfileUpdateForm
import com.essaid.user.domain.model.PersonalPatientInfo

@Composable
fun PatientProfileScreen(
    viewModel: PatientProfileViewModel,
    token: String,
    onLogout: () -> Unit,
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
                text = "Mi Perfil Personal",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

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
                        OutlinedButton(onClick = { viewModel.loadProfile(token) }) {
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
                ProfileStatus.Idle -> {}
                ProfileStatus.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                ProfileStatus.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = state.errorMessage ?: "Error al cargar perfil.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Button(onClick = { viewModel.loadProfile(token) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Reintentar")
                            }
                        }
                    }
                }
                ProfileStatus.Success, ProfileStatus.Empty, ProfileStatus.Unauthorized -> {
                    val profile = state.profile
                    if (profile != null) {
                        PersonalSection(profile.personal)
                        MedicalSection(
                            medical = profile.medical,
                            isEditing = state.isEditing,
                            form = state.updateForm,
                            fieldErrors = state.fieldErrors,
                            viewModel = viewModel,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!state.isEditing) {
                            Button(
                                onClick = { viewModel.startEditing() },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Editar Información Médica")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelEditing() },
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
                                            strokeWidth = 2.dp,
                                        )
                                    } else {
                                        Text("Guardar Cambios")
                                    }
                                }
                            }
                        }
                    } else {
                        Text(state.errorMessage ?: "No hay datos de perfil disponibles.")
                        Button(onClick = { viewModel.loadProfile(token) }) {
                            Text("Cargar Perfil")
                        }
                    }
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
}

@Composable
private fun PersonalSection(personal: PersonalPatientInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Datos Personales",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            InfoRow("Nombre completo", "${personal.nombre} ${personal.apellido}")
            InfoRow("Documento", "${personal.tipoDocumento}: ${personal.numeroDocumento}")
            InfoRow("Correo electrónico", personal.email)
            InfoRow("Celular", personal.celular)
            InfoRow("Fecha de nacimiento", personal.fechaNacimiento)
            personal.genero?.let { InfoRow("Género", it) }
        }
    }
}

@Composable
private fun MedicalSection(
    medical: MedicalPatientInfo,
    isEditing: Boolean,
    form: PatientProfileUpdateForm,
    fieldErrors: Map<String, String>,
    viewModel: PatientProfileViewModel,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Información Médica de Emergencia",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            if (!isEditing) {
                InfoRow("Tipo de sangre", medical.tipoSangre ?: "No registrado")
                InfoRow("Alergias", if (medical.alergias.isNotEmpty()) medical.alergias.joinToString(", ") else "Ninguna")
                InfoRow("Condiciones crónicas", if (medical.condicionesCronicas.isNotEmpty()) medical.condicionesCronicas.joinToString(", ") else "Ninguna")
                InfoRow("Medicamentos actuales", if (medical.medicamentosActuales.isNotEmpty()) medical.medicamentosActuales.joinToString(", ") else "Ningunos")
                InfoRow("Contacto de emergencia", "${medical.contactoEmergenciaNombre ?: "No registrado"} (${medical.contactoEmergenciaTelefono ?: "-"})")
                InfoRow("Observaciones", medical.observacionesMedicas ?: "Ninguna")
            } else {
                OutlinedTextField(
                    value = form.celular,
                    onValueChange = viewModel::onCelularChanged,
                    label = { Text("Celular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = fieldErrors.containsKey("celular"),
                )
                fieldErrors["celular"]?.let { ErrorText(it) }

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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = form.alergias,
                    onValueChange = viewModel::onAlergiasChanged,
                    label = { Text("Alergias (separadas por comas)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = form.condicionesCronicas,
                    onValueChange = viewModel::onCondicionesCronicasChanged,
                    label = { Text("Condiciones crónicas (separadas por comas)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = form.medicamentosActuales,
                    onValueChange = viewModel::onMedicamentosActualesChanged,
                    label = { Text("Medicamentos actuales (separados por comas)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = form.observacionesMedicas,
                    onValueChange = viewModel::onObservacionesMedicasChanged,
                    label = { Text("Observaciones médicas") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}
