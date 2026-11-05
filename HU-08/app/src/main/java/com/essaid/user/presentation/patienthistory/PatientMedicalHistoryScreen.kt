package com.essaid.user.presentation.patienthistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.essaid.user.domain.model.MedicalHistoryCategory
import com.essaid.user.domain.model.MedicalHistoryEntry
import com.essaid.user.domain.model.PatientMedicalHistory
import com.essaid.user.domain.model.PatientSearchResult
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException

@Composable
fun PatientMedicalHistoryScreen(
    viewModel: PatientMedicalHistoryViewModel,
    token: String,
    patient: PatientSearchResult,
    isDemoMode: Boolean,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token, patient.id) {
        viewModel.loadHistory(token, patient.id)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Antecedentes médicos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        PatientMedicalHeader(patient)
        if (isDemoMode) {
            Text(
                text = "Modo demostración: no se consultan antecedentes reales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (state.patientId != patient.id || state.status == PatientMedicalHistoryStatus.Loading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text("Consultando antecedentes…")
            }
        } else {
            when (state.status) {
                PatientMedicalHistoryStatus.Idle,
                PatientMedicalHistoryStatus.Loading -> Unit
                PatientMedicalHistoryStatus.Success,
                PatientMedicalHistoryStatus.Empty -> {
                    if (state.status == PatientMedicalHistoryStatus.Empty) {
                        Text(
                            text = "No hay antecedentes registrados para este paciente.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    state.history?.let { HistoryCategories(it) }
                }
                PatientMedicalHistoryStatus.Unauthorized,
                PatientMedicalHistoryStatus.Forbidden,
                PatientMedicalHistoryStatus.NotFound,
                PatientMedicalHistoryStatus.NetworkError,
                PatientMedicalHistoryStatus.ServerError -> {
                    Text(
                        text = state.message ?: "No se pudieron consultar los antecedentes.",
                        color = MaterialTheme.colorScheme.error,
                    )
                    if (state.status != PatientMedicalHistoryStatus.Unauthorized &&
                        state.status != PatientMedicalHistoryStatus.Forbidden &&
                        state.status != PatientMedicalHistoryStatus.NotFound
                    ) {
                        OutlinedButton(onClick = { viewModel.loadHistory(token, patient.id) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientMedicalHeader(patient: PatientSearchResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${patient.nombre} ${patient.apellido}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text("Tipo de sangre: ${patient.tipoSangre ?: "No registrado"}")
            Text("Sexo: ${patient.sexo ?: "No registrado"}")
            Text("Edad: ${patient.fechaNacimiento?.patientAge() ?: "No disponible"}")
        }
    }
}

@Composable
private fun HistoryCategories(history: PatientMedicalHistory) {
    MedicalHistoryCategory.entries.forEach { category ->
        val entries = history.entriesByCategory[category].orEmpty()
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (entries.isEmpty()) {
                    Text("Sin registros", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    entries.forEach { HistoryEntry(it) }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntry(entry: MedicalHistoryEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(entry.description, style = MaterialTheme.typography.bodyLarge)
        entry.approximateDate?.let { Text("Fecha aproximada: $it", style = MaterialTheme.typography.bodySmall) }
        Text(
            text = if (entry.active) "Activo" else "Inactivo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        entry.recordedBy?.let { Text("Registrado por: $it", style = MaterialTheme.typography.bodySmall) }
        entry.createdAt?.let { Text("Registrado el: $it", style = MaterialTheme.typography.bodySmall) }
        Spacer(modifier = Modifier.size(2.dp))
    }
}

private fun String.patientAge(): String? {
    val birthDate = try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        return null
    }
    val today = LocalDate.now()
    if (birthDate.isAfter(today)) return null
    return "${Period.between(birthDate, today).years} años"
}
