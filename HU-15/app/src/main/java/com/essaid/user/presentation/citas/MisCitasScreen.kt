package com.essaid.user.presentation.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.domain.model.CitaMedica

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisCitasScreen(
    citas: List<CitaMedica>,
    onCancelarCita: (String) -> Unit,
    onBack: () -> Unit
) {
    var listaCitas by remember { mutableStateOf(citas) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Citas Programadas (HU-15 & HU-16)") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (listaCitas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No cuenta con citas médicas programadas.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaCitas) { cita ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Fecha: ${cita.fecha} - Hora: ${cita.hora}", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Hospital ID: ${cita.hospitalId}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Estado: ${cita.estado}", style = MaterialTheme.typography.bodySmall, color = if (cita.estado == "PROGRAMADA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                
                                if (cita.estado == "PROGRAMADA") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            onCancelarCita(cita.id)
                                            listaCitas = listaCitas.map { if (it.id == cita.id) it.copy(estado = "CANCELADA") else it }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Cancelar Cita")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
