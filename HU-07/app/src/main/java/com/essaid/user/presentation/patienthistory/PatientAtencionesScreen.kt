package com.essaid.user.presentation.patienthistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.essaid.user.domain.model.PatientAtencion
import com.essaid.user.domain.model.PatientSearchResult

@Composable
fun PatientAtencionesScreen(
    viewModel: PatientAtencionesViewModel,
    token: String,
    patient: PatientSearchResult,
    isDemoMode: Boolean,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var desdeInput by remember { mutableStateOf(state.desde ?: "") }
    var hastaInput by remember { mutableStateOf(state.hasta ?: "") }

    LaunchedEffect(token, patient.id) {
        viewModel.loadAtenciones(token, patient.id)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Historial de Atenciones Médicas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        // Date Filters Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Filtrar por fecha", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = desdeInput,
                        onValueChange = { desdeInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Desde (YYYY-MM-DD)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = hastaInput,
                        onValueChange = { hastaInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Hasta (YYYY-MM-DD)") },
                        singleLine = true,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (desdeInput.isNotBlank() || hastaInput.isNotBlank()) {
                        TextButton(
                            onClick = {
                                desdeInput = ""
                                hastaInput = ""
                                viewModel.updateFilters(token, patient.id, null, null)
                            },
                        ) {
                            Text("Limpiar")
                        }
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateFilters(token, patient.id, desdeInput, hastaInput)
                        },
                    ) {
                        Text("Aplicar Filtros")
                    }
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

        if (state.patientId != patient.id || state.status == PatientAtencionesStatus.Loading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text("Consultando atenciones anteriores…")
            }
        } else {
            when (state.status) {
                PatientAtencionesStatus.Idle,
                PatientAtencionesStatus.Loading -> Unit
                PatientAtencionesStatus.Success,
                PatientAtencionesStatus.Empty -> {
                    if (state.status == PatientAtencionesStatus.Empty) {
                        Text(
                            text = "No hay atenciones médicas registradas para este paciente.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.atenciones.forEach { atencion ->
                            AtencionCard(atencion, onClick = { viewModel.selectAtencion(atencion) })
                        }
                        // Pagination controls
                        val resp = state.response
                        if (resp != null && (resp.next != null || resp.previous != null)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.loadPreviousPage(token, patient.id) },
                                    enabled = resp.previous != null && state.currentPage > 1,
                                ) {
                                    Text("Anterior")
                                }
                                Text("Página ${state.currentPage}", style = MaterialTheme.typography.bodyMedium)
                                OutlinedButton(
                                    onClick = { viewModel.loadNextPage(token, patient.id) },
                                    enabled = resp.next != null,
                                ) {
                                    Text("Siguiente")
                                }
                            }
                        }
                    }
                }
                PatientAtencionesStatus.ValidationError,
                PatientAtencionesStatus.Unauthorized,
                PatientAtencionesStatus.Forbidden,
                PatientAtencionesStatus.NotFound,
                PatientAtencionesStatus.NetworkError,
                PatientAtencionesStatus.ServerError -> {
                    Text(
                        text = state.message ?: "No se pudieron consultar las atenciones.",
                        color = MaterialTheme.colorScheme.error,
                    )
                    if (state.status != PatientAtencionesStatus.Unauthorized &&
                        state.status != PatientAtencionesStatus.Forbidden &&
                        state.status != PatientAtencionesStatus.NotFound
                    ) {
                        OutlinedButton(onClick = { viewModel.loadAtenciones(token, patient.id) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }

        state.selectedAtencion?.let { atencion ->
            AtencionDetailDialog(atencion = atencion, onDismiss = { viewModel.selectAtencion(null) })
        }
    }
}

@Composable
private fun AtencionCard(atencion: PatientAtencion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    text = "Fecha: ${atencion.fechaAtencion.take(16).replace('T', ' ')}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                atencion.codigoCie10?.let { cie ->
                    Text(
                        text = "CIE-10: $cie",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Text(
                text = "Motivo: ${atencion.motivoConsulta}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Diagnóstico: ${atencion.diagnostico}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Tratamiento: ${atencion.tratamiento}",
                style = MaterialTheme.typography.bodyMedium,
            )
            atencion.medico?.let { doc ->
                Text(
                    text = "Médico tratante: Dr(a). ${doc.fullName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Toca para ver detalles completos y signos vitales",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun AtencionDetailDialog(atencion: PatientAtencion, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de Atención Médica") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DetailItem("Fecha", atencion.fechaAtencion.take(16).replace('T', ' '))
                atencion.medico?.let { DetailItem("Médico tratante", "Dr(a). ${it.fullName}") }
                DetailItem("Motivo de consulta", atencion.motivoConsulta)
                DetailItem("Diagnóstico", atencion.diagnostico)
                atencion.codigoCie10?.let { DetailItem("Código CIE-10", it) }
                DetailItem("Tratamiento prescrito", atencion.tratamiento)

                Spacer(Modifier.height(4.dp))
                Text("Signos Vitales y Antropometría", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                DetailItem("Presión arterial", atencion.presionArterial ?: "No registrada")
                DetailItem("Frecuencia cardíaca", atencion.frecuenciaCardiaca?.let { "$it lpm" } ?: "No registrada")
                DetailItem("Temperatura", atencion.temperatura?.let { "$it °C" } ?: "No registrada")
                DetailItem("Saturación O2", atencion.saturacionO2?.let { "$it %" } ?: "No registrada")
                DetailItem("Peso", atencion.pesoKg?.let { "$it kg" } ?: "No registrado")
                DetailItem("Talla", atencion.tallaCm?.let { "$it cm" } ?: "No registrada")

                atencion.notas?.takeIf(String::isNotBlank)?.let {
                    Spacer(Modifier.height(4.dp))
                    DetailItem("Notas médicas", it)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
