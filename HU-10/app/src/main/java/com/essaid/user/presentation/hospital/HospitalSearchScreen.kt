package com.essaid.user.presentation.hospital

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.domain.validation.HospitalSearchValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalSearchScreen(
    viewModel: HospitalViewModel,
    token: String,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarHospitales(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Hospital por Ubicación (HU-10)") },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Ingrese distrito, provincia o región") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            val isSearchValid = HospitalSearchValidator.validarTerminoUbicacion(searchQuery)
            if (!isSearchValid && searchQuery.isNotEmpty()) {
                Text(
                    text = "Ingrese al menos 2 caracteres para buscar.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is HospitalUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HospitalUiState.Success -> {
                    val filtered = if (searchQuery.isBlank() || !isSearchValid) {
                        state.hospitales
                    } else {
                        state.hospitales.filter { 
                            it.direccion.contains(searchQuery, ignoreCase = true) || 
                            it.region.contains(searchQuery, ignoreCase = true) ||
                            it.nombre.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron hospitales para la ubicación: '$searchQuery'")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { hospital ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = hospital.nombre, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Ubicación: ${hospital.direccion} (${hospital.region})", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Teléfono: ${hospital.telefono}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
                is HospitalUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
