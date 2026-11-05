package com.essaid.user.presentation.patienthistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.essaid.user.domain.model.ConsultationEntry
import com.essaid.user.domain.model.PatientConsultations
import com.essaid.user.domain.model.PatientSearchResult

@Composable
fun PatientConsultationScreen(
    viewModel: PatientConsultationViewModel,
    token: String,
    patient: PatientSearchResult,
    isDemoMode: Boolean,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token, patient.id) {
        viewModel.loadConsultations(token, patient.id)
    }

    var desdeFilter by remember(patient.id) { mutableStateOf("") }
    var hastaFilter by remember(patient.id) { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Historial de atenciones",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Filtrar por fecha (YYYY-MM-DD)", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = desdeFilter,
                        onValueChange = { desdeFilter = it },
                        label = { Text("Desde") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = hastaFilter,
                        onValueChange = { hastaFilter = it },
                        label = { Text("Hasta") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                OutlinedButton(
                    onClick = {
                        viewModel.applyDateFilter(
                            token,
                            patient.id,
                            desdeFilter.ifBlank { null },
                            hastaFilter.ifBlank { null },
                        )
                    },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Aplicar filtros")
                }
            }
        }

        if (isDemoMode) {
            Text(
                text = "Modo demostración: atenciones médicas simuladas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (state.patientId != patient.id || state.status == PatientConsultationStatus.Loading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text("Consultando atenciones…")
            }
        } else {
            when (state.status) {
                PatientConsultationStatus.Idle,
                PatientConsultationStatus.Loading -> Unit
                PatientConsultationStatus.Success,
                PatientConsultationStatus.Empty -> {
                    if (state.status == PatientConsultationStatus.Empty) {
                        Text(
                            text = "No hay atenciones registradas para este paciente.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    state.consultations?.let { ConsultationsList(it) }
                }
                PatientConsultationStatus.Unauthorized,
                PatientConsultationStatus.Forbidden,
                PatientConsultationStatus.NotFound,
                PatientConsultationStatus.NetworkError,
                PatientConsultationStatus.ServerError -> {
                    Text(
                        text = state.message ?: "No se pudieron consultar las atenciones.",
                        color = MaterialTheme.colorScheme.error,
                    )
                    if (state.status != PatientConsultationStatus.Unauthorized &&
                        state.status != PatientConsultationStatus.Forbidden &&
                        state.status != PatientConsultationStatus.NotFound
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.loadConsultations(
                                    token,
                                    patient.id,
                                    desdeFilter.ifBlank { null },
                                    hastaFilter.ifBlank { null },
                                )
                            }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsultationsList(consultations: PatientConsultations) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        consultations.entries.forEach { entry ->
            ConsultationItemCard(entry)
        }
    }
}

@Composable
private fun ConsultationItemCard(entry: ConsultationEntry) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = entry.fechaAtencion.take(16).replace("T", " "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (entry.codigoCie10.isNotBlank()) {
                    Text(
                        text = "CIE-10: ${entry.codigoCie10}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Text(
                text = "Motivo: ${entry.motivoConsulta}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Diagnóstico: ${entry.diagnostico}",
                style = MaterialTheme.typography.bodyMedium,
            )
            entry.medico?.let {
                Text(
                    text = "Médico: ${it.fullName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.size(4.dp))
                Text("Tratamiento: ${entry.tratamiento}", style = MaterialTheme.typography.bodyMedium)
                entry.notas?.let {
                    if (it.isNotBlank()) {
                        Text("Notas: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
                val vitals = listOfNotNull(
                    entry.presionArterial?.let { "PA: $it" },
                    entry.frecuenciaCardiaca?.let { "FC: $it lpm" },
                    entry.temperatura?.let { "Temp: $it °C" },
                    entry.saturacionO2?.let { "SpO2: $it%" },
                    entry.pesoKg?.let { "Peso: $it kg" },
                    entry.tallaCm?.let { "Talla: $it cm" },
                )
                if (vitals.isNotEmpty()) {
                    Text("Signos vitales / Medidas: ${vitals.joinToString(" | ")}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "Toque para contraer detalles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            } else {
                Text(
                    text = "Toque para ver detalles completos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
