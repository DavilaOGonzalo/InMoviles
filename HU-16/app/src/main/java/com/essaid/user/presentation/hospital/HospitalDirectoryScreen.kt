package com.essaid.user.presentation.hospital

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.di.AppContainer
import com.essaid.user.domain.model.Hospital
import com.essaid.user.domain.model.ServicioMedico

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDirectoryScreen(
    viewModel: HospitalViewModel,
    token: String,
    isStaff: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf<String?>(null) }
    var selectedHospitalForServices by remember { mutableStateOf<Hospital?>(null) }

    val servicesViewModel = remember { HospitalServicesViewModel(AppContainer.hospitalServicesRepository) }

    LaunchedEffect(Unit) {
        viewModel.cargarHospitales(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Directorio de Hospitales") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search and Location filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar hospital por nombre o dirección...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Region filter chips
            val regions = listOf("Todas", "Lima", "Norte", "Sur", "Centro")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(regions) { region ->
                    val isSelected = (region == "Todas" && selectedRegion == null) || selectedRegion == region
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedRegion = if (region == "Todas") null else region
                        },
                        label = { Text(region) }
                    )
                }
            }

            when (val state = uiState) {
                is HospitalUiState.Idle, is HospitalUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HospitalUiState.Success -> {
                    val filtered = state.hospitales.filter { h ->
                        val matchesSearch = searchQuery.isBlank() ||
                            h.nombre.contains(searchQuery, ignoreCase = true) ||
                            h.direccion.contains(searchQuery, ignoreCase = true)
                        val matchesRegion = selectedRegion == null || h.region.equals(selectedRegion, ignoreCase = true)
                        matchesSearch && matchesRegion
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron hospitales con los filtros seleccionados.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { hospital ->
                                HospitalCard(
                                    hospital = hospital,
                                    onViewServices = {
                                        selectedHospitalForServices = hospital
                                        servicesViewModel.cargarServicios(token, hospital.id)
                                    }
                                )
                            }
                        }
                    }
                }
                is HospitalUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay hospitales registrados en el sistema.")
                    }
                }
                is HospitalUiState.Unauthorized -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sesión no autorizada. Por favor inicie sesión nuevamente.")
                    }
                }
                is HospitalUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

        // Modal Bottom Sheet for Medical Services
        selectedHospitalForServices?.let { hospital ->
            ModalBottomSheet(
                onDismissRequest = { selectedHospitalForServices = null }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Servicios en ${hospital.nombre}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Consulta de servicios médicos disponibles y gestión para personal de salud.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val servicesState by servicesViewModel.uiState.collectAsState()
                    when (val sState = servicesState) {
                        is HospitalServicesUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is HospitalServicesUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sState.servicios) { servicio ->
                                    ServiceItemCard(
                                        servicio = servicio,
                                        isStaff = isStaff,
                                        onToggleAvailability = { nuevoEstado ->
                                            servicesViewModel.toggleDisponibilidad(token, servicio.id, hospital.id, nuevoEstado)
                                        }
                                    )
                                }
                            }
                        }
                        is HospitalServicesUiState.Empty -> {
                            Text("No hay servicios médicos registrados para este hospital.")
                        }
                        is HospitalServicesUiState.Error -> {
                            Text(text = sState.message, color = MaterialTheme.colorScheme.error)
                        }
                        else -> Unit
                    }

                    Button(
                        onClick = { selectedHospitalForServices = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun HospitalCard(
    hospital: Hospital,
    onViewServices: () -> Unit
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
                Text(text = hospital.nombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Badge { Text(if (hospital.activo) "Activo" else "Inactivo") }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Text(text = "Dirección: ${hospital.direccion}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Región: ${hospital.region}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Teléfono: ${hospital.telefono}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onViewServices,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Ver Servicios")
            }
        }
    }
}

@Composable
private fun ServiceItemCard(
    servicio: ServicioMedico,
    isStaff: Boolean,
    onToggleAvailability: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = servicio.nombreServicio, style = MaterialTheme.typography.titleSmall)
                Text(text = servicio.descripcion, style = MaterialTheme.typography.bodySmall)
                Text(text = "Costo ref: S/ ${servicio.costoReferencial}", style = MaterialTheme.typography.labelSmall)
            }
            if (isStaff) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (servicio.disponible) "Disponible" else "No disponible", style = MaterialTheme.typography.labelSmall)
                    Switch(
                        checked = servicio.disponible,
                        onCheckedChange = onToggleAvailability
                    )
                }
            } else {
                Badge(
                    containerColor = if (servicio.disponible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(if (servicio.disponible) "Disponible" else "No disponible")
                }
            }
        }
    }
}
