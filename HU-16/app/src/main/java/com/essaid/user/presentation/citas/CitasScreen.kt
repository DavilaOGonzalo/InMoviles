package com.essaid.user.presentation.citas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.domain.model.CitaMedica
import com.essaid.user.domain.model.MisCitasOutcome
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    viewModel: CitasViewModel,
    token: String,
    pacienteId: String,
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mis Citas, 1: Reservar Cita
    var cancelCitaTarget by remember { mutableStateOf<CitaMedica?>(null) }
    var motivoCancelacion by remember { mutableStateOf("") }

    LaunchedEffect(pacienteId) {
        viewModel.cargarMisCitas(token, pacienteId)
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
                // Reservar Cita Wizard
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stepTitle = when (state.selectedStep) {
                            1 -> "Paso 1: Selecciona Hospital"
                            2 -> "Paso 2: Selecciona Especialidad"
                            3 -> "Paso 3: Selecciona Doctor"
                            else -> "Paso 4: Perfil y Horarios del Doctor"
                        }
                        Text(text = stepTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        if (state.selectedStep > 1) {
                            TextButton(onClick = { viewModel.goToStep(state.selectedStep - 1) }) {
                                Text("Atrás")
                            }
                        }
                    }

                    when (state.selectedStep) {
                        1 -> {
                            Text("Elige uno de nuestros 3 hospitales EssAid:", style = MaterialTheme.typography.bodyMedium)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.hospitales) { hospital ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectHospital(hospital) },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(hospital.nombre, style = MaterialTheme.typography.titleMedium)
                                                Text(hospital.direccion, style = MaterialTheme.typography.bodySmall)
                                                Text("Tel: ${hospital.telefono}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                            }
                                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Text("Hospital: ${state.selectedHospital?.nombre}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            Text("Elige la especialidad médica:", style = MaterialTheme.typography.bodyMedium)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.especialidades) { esp ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectSpecialty(esp) },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(esp.nombre, style = MaterialTheme.typography.titleMedium)
                                                Text(esp.descripcion, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            Text("Especialidad: ${state.selectedSpecialty?.nombre}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            Text("Elige al doctor especialista:", style = MaterialTheme.typography.bodyMedium)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.doctores) { doc ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectDoctor(doc) },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(50.dp),
                                                shape = MaterialTheme.shapes.medium,
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(doc.nombre, style = MaterialTheme.typography.titleMedium)
                                                Text(doc.colegiatura, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                Text(doc.hospitalNombre, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                        4 -> {
                            val doc = state.selectedDoctor
                            if (doc != null) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        modifier = Modifier.size(64.dp),
                                                        shape = MaterialTheme.shapes.extraLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                                                        }
                                                    }
                                                    Column {
                                                        Text(doc.nombre, style = MaterialTheme.typography.titleLarge)
                                                        Text(doc.especialidad, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                                        Text(doc.colegiatura, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                                Divider()
                                                Text("Hospital: ${doc.hospitalNombre}", style = MaterialTheme.typography.bodyMedium)
                                                Text(doc.biografia, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }

                                    item {
                                        Text("Selecciona el día de atención:", style = MaterialTheme.typography.titleMedium)
                                    }

                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            state.doctorDiasHorarios.forEach { (fecha, _) ->
                                                val formatter = DateTimeFormatter.ofPattern("EEE dd", Locale("es", "ES"))
                                                val label = fecha.format(formatter).replaceFirstChar { it.uppercase() }
                                                FilterChip(
                                                    selected = state.selectedFecha == fecha,
                                                    onClick = { viewModel.selectFecha(fecha) },
                                                    label = { Text(label) }
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        Text("Horarios disponibles para ${state.selectedFecha}:", style = MaterialTheme.typography.titleMedium)
                                    }

                                    item {
                                        val currentBloques = state.doctorDiasHorarios.firstOrNull { it.first == state.selectedFecha }?.second ?: emptyList()
                                        if (currentBloques.isEmpty()) {
                                            Text("No hay horarios disponibles para esta fecha.", style = MaterialTheme.typography.bodySmall)
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                currentBloques.forEach { bloque ->
                                                    FilterChip(
                                                        selected = state.selectedHora == bloque.hora,
                                                        onClick = { if (bloque.disponible) viewModel.selectHora(bloque.hora) },
                                                        enabled = bloque.disponible,
                                                        label = { Text(bloque.hora) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                state.selectedHora?.let { hora ->
                                                    viewModel.reservarCita(
                                                        token = token,
                                                        pacienteId = pacienteId,
                                                        hospitalId = doc.hospitalId,
                                                        fecha = state.selectedFecha.toString(),
                                                        hora = hora
                                                    )
                                                }
                                            },
                                            enabled = state.selectedHora != null && !state.isLoading,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Confirmar Reserva de Cita con ${doc.nombre}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

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
