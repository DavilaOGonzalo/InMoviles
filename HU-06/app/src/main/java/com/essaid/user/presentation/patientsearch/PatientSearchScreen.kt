package com.essaid.user.presentation.patientsearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.essaid.user.domain.model.PatientSearchResult
import com.essaid.user.presentation.patienthistory.PatientMedicalHistoryScreen
import com.essaid.user.presentation.patienthistory.PatientMedicalHistoryViewModel

@Composable
fun PatientSearchScreen(
    viewModel: PatientSearchViewModel,
    medicalHistoryViewModel: PatientMedicalHistoryViewModel,
    token: String,
    isDemoMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token) {
        viewModel.setSessionToken(token)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.selectedPatient?.let { patient ->
            Button(onClick = viewModel::returnToResults) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Volver a resultados")
            }
            PatientSummary(patient, isDemoMode)
            PatientMedicalHistoryScreen(
                viewModel = medicalHistoryViewModel,
                token = token,
                patient = patient,
                isDemoMode = isDemoMode,
            )
            return@Column
        }

        Text(
            text = "Buscar paciente",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Busca por DNI exacto o por nombre/apellido.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("DNI o nombre/apellido") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
        )

        when (state.status) {
            PatientSearchStatus.Idle -> Text(
                text = if (state.query.isBlank()) "Ingresa un DNI o nombre para comenzar."
                else "Buscando cuando termines de escribir…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PatientSearchStatus.ValidationError -> StatusMessage(state.message)
            PatientSearchStatus.Loading -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text("Buscando pacientes…")
            }
            PatientSearchStatus.Success -> state.patients.forEach { patient ->
                PatientResultCard(patient, onClick = { viewModel.selectPatient(patient) })
            }
            PatientSearchStatus.Empty -> StatusMessage("No se encontraron pacientes para esa búsqueda.")
            PatientSearchStatus.Unauthorized,
            PatientSearchStatus.Forbidden,
            PatientSearchStatus.NetworkError,
            PatientSearchStatus.ServerError -> {
                StatusMessage(state.message)
                Button(onClick = { viewModel.retry(token) }) {
                    Text("Reintentar")
                }
            }
        }
    }
}

@Composable
private fun PatientResultCard(patient: PatientSearchResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${patient.nombre} ${patient.apellido}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text("DNI: ${patient.dni ?: "No registrado"}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Nacimiento: ${patient.fechaNacimiento ?: "No registrado"} · " +
                    "Sexo: ${patient.sexo ?: "No registrado"} · " +
                    "Sangre: ${patient.tipoSangre ?: "No registrado"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PatientSummary(patient: PatientSearchResult, isDemoMode: Boolean) {
    Text("Perfil del paciente", style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PatientDetail("Nombre", "${patient.nombre} ${patient.apellido}")
            PatientDetail("DNI", patient.dni ?: "No registrado")
            PatientDetail("Fecha de nacimiento", patient.fechaNacimiento ?: "No registrado")
            PatientDetail("Sexo", patient.sexo ?: "No registrado")
        }
    }
    Spacer(Modifier.height(20.dp))
    Text(
        "Información Médica",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(8.dp))
    MedicalInfoCard("Tipo de Sangre", patient.tipoSangre ?: "No registrado")
    MedicalInfoCard("Alergias", patient.alergias.displayValue("No registradas"))
    MedicalInfoCard("Condiciones Crónicas", patient.condicionesCronicas.displayValue("Ninguna registrada"))
    MedicalInfoCard("Medicamentos Actuales", patient.medicamentosActuales.displayValue("Ninguno"))
    val emergencyContact = listOfNotNull(
        patient.contactoEmergencia?.takeIf(String::isNotBlank),
        patient.telefonoEmergencia?.takeIf(String::isNotBlank)?.let { "($it)" },
    ).joinToString(" ")
    MedicalInfoCard("Contacto de Emergencia", emergencyContact.ifBlank { "No registrado" })
    MedicalInfoCard("Observaciones", patient.observacionesMedicas?.takeIf(String::isNotBlank) ?: "Sin observaciones")
    MedicalInfoCard(
        "Última Actualización",
        patient.informacionMedicaActualizada ?: "No registrada",
    )
    if (isDemoMode) {
        Text(
            "Datos ficticios de demostración. Confirma la información médica con el paciente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun MedicalInfoCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun List<String>.displayValue(emptyValue: String): String =
    filter(String::isNotBlank).takeIf(List<String>::isNotEmpty)?.joinToString(", ") ?: emptyValue

@Composable
private fun PatientDetail(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StatusMessage(message: String?) {
    Text(
        text = message ?: "No se pudo completar la búsqueda.",
        color = MaterialTheme.colorScheme.error,
    )
}
