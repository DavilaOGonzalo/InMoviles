package com.essaid.user.presentation.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.domain.model.CitaMedica
import com.essaid.user.domain.model.MisCitasOutcome
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    viewModel: CitasViewModel,
    token: String,
    pacienteId: String,
    hospitalId: String = "H-001"
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mis Citas, 1: Reservar Cita

    var fechaInput by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var selectedHora by remember { mutableStateOf<String?>(null) }
    var cancelCitaTarget by remember { mutableStateOf<CitaMedica?>(null) }
    var motivoCancelacion by remember { mutableStateOf("") }

    LaunchedEffect(pacienteId) {
        viewModel.cargarMisCitas(token, pacienteId)
        viewModel.cargarBloques(token, hospitalId, fechaInput)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Citas Médicas") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Mis Citas Programadas") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Reservar Cita") }
                )
            }

            state.successMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = msg, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::clearMessages) { Text("OK") }
                    }
                }
            }

            state.message?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = err, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::clearMessages) { Text("OK") }
                    }
                }
            }

            if (selectedTab == 0) {
                // Mis Citas
                Text("Tus citas médicas agendadas en EssAid:", style = MaterialTheme.typography.titleMedium)
                when (val misCitas = state.misCitas) {
                    is MisCitasOutcome.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes citas médicas programadas actualmente.")
                        }
                    }
                    is MisCitasOutcome.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(misCitas.citas) { cita ->
                                CitaCard(
                                    cita = cita,
                                    onCancel = { cancelCitaTarget = cita }
                                )
                            }
                        }
                    }
                }
            } else {
                // Reservar Cita
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Selecciona fecha y horario disponible:", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = fechaInput,
                        onValueChange = {
                            fechaInput = it
                            viewModel.cargarBloques(token, hospitalId, it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Fecha (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        singleLine = true
                    )

                    Text("Horarios disponibles:", style = MaterialTheme.typography.labelLarge)
                    if (state.bloques.isEmpty()) {
                        Text("No hay bloques horarios disponibles para esta fecha.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        // Chips for time slots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.bloques.forEach { bloque ->
                                FilterChip(
                                    selected = selectedHora == bloque.hora,
                                    onClick = { if (bloque.disponible) selectedHora = bloque.hora },
                                    enabled = bloque.disponible,
                                    label = { Text(bloque.hora) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            selectedHora?.let { hora ->
                                viewModel.reservarCita(token, pacienteId, hospitalId, fechaInput, hora)
                            }
                        },
                        enabled = selectedHora != null && !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirmar Reserva de Cita")
                    }
                }
            }
        }

        // Dialog for Cancelling Appointment
        cancelCitaTarget?.let { cita ->
            AlertDialog(
                onDismissRequest = { cancelCitaTarget = null },
                title = { Text("Cancelar Cita Médica") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("¿Estás seguro de cancelar tu cita para el ${cita.fecha} a las ${cita.hora}?")
                        OutlinedTextField(
                            value = motivoCancelacion,
                            onValueChange = { motivoCancelacion = it },
                            label = { Text("Motivo de cancelación") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cancelarCita(token, cita.id, motivoCancelacion, pacienteId)
                            cancelCitaTarget = null
                            motivoCancelacion = ""
                        }
                    ) {
                        Text("Sí, Cancelar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { cancelCitaTarget = null }) {
                        Text("Volver")
                    }
                }
            )
        }
    }
}

@Composable
private fun CitaCard(
    cita: CitaMedica,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Cita ID: ${cita.id}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Badge(
                    containerColor = if (cita.estado == "PROGRAMADA") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(cita.estado)
                }
            }
            Text(text = "Fecha: ${cita.fecha} · Hora: ${cita.hora}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Hospital ID: ${cita.hospitalId}", style = MaterialTheme.typography.bodySmall)

            if (cita.estado == "PROGRAMADA") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Cancelar Cita")
                }
            }
        }
    }
}
