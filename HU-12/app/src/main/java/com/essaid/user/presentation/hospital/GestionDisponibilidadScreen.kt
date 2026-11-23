package com.essaid.user.presentation.hospital

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.domain.model.ServicioMedico

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionDisponibilidadScreen(
    serviciosIniciales: List<ServicioMedico>,
    onActualizarDisponibilidad: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var servicios by remember { mutableStateOf(serviciosIniciales) }
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Disponibilidad de Servicios (HU-12)") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            mensajeFeedback?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = msg, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(servicios) { servicio ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = servicio.nombreServicio, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Hospital: ${servicio.hospitalId}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = if (servicio.disponible) "Disponible" else "No disponible",
                                    color = if (servicio.disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = servicio.disponible,
                                onCheckedChange = { nuevoEstado ->
                                    onActualizarDisponibilidad(servicio.id, nuevoEstado)
                                    servicios = servicios.map { if (it.id == servicio.id) it.copy(disponible = nuevoEstado) else it }
                                    mensajeFeedback = "Servicio '${servicio.nombreServicio}' actualizado a: ${if (nuevoEstado) 'Disponible' else 'No disponible'}"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
