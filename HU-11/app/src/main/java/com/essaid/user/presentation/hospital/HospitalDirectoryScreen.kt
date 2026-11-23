package com.essaid.user.presentation.hospital

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDirectoryScreen(
    viewModel: HospitalViewModel,
    token: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarHospitales(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospitales Registrados (HU-09)") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is HospitalUiState.Idle, is HospitalUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is HospitalUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.hospitales) { hospital ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = hospital.nombre, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Dirección: ${hospital.direccion}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Región: ${hospital.region}", style = MaterialTheme.typography.bodySmall)
                                    Text(text = "Teléfono: ${hospital.telefono}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                is HospitalUiState.Empty -> {
                    Text("No hay hospitales registrados en el sistema.")
                }
                is HospitalUiState.Unauthorized -> {
                    Text("Sesión no autorizada. Por favor inicie sesión nuevamente.")
                }
                is HospitalUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarHospitales(token) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}
