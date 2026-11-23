package com.essaid.user.presentation.citas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarCitaScreen(
    hospitalNombre: String,
    servicioNombre: String,
    fecha: String,
    hora: String,
    onConfirmarReserva: () -> Unit,
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Cita Médica (HU-14)") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen de su Cita", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Hospital: $hospitalNombre", style = MaterialTheme.typography.bodyMedium)
                    Text("Servicio: $servicioNombre", style = MaterialTheme.typography.bodyMedium)
                    Text("Fecha: $fecha", style = MaterialTheme.typography.bodyMedium)
                    Text("Hora: $hora", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            successMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    // Simula reserva transaccional con control HTTP 409 Conflict si ya está ocupado
                    try {
                        onConfirmarReserva()
                        successMessage = "¡Cita solicitada y programada con éxito!"
                    } catch (e: Exception) {
                        errorMessage = "Conflicto (HTTP 409): El horario seleccionado ya ha sido reservado por otro paciente."
                    } finally {
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && successMessage == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirmar y Solicitar Cita")
                }
            }
        }
    }
}
