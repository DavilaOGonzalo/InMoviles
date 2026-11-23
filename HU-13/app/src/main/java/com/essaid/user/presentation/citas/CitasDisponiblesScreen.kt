package com.essaid.user.presentation.citas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.domain.model.BloqueHorario

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CitasDisponiblesScreen(
    bloques: List<BloqueHorario>,
    fechaSeleccionada: String,
    onFechaChange: (String) -> Unit,
    onSeleccionarBloque: (BloqueHorario) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultar Citas Disponibles (HU-13)") },
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
                value = fechaSeleccionada,
                onValueChange = onFechaChange,
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Horarios disponibles para la fecha:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (bloques.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay horarios configurados para esta fecha.")
                }
            } else {
                LazyVerticalGrid(
                    cells = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bloques) { bloque ->
                        Button(
                            onClick = { if (bloque.disponible) onSeleccionarBloque(bloque) },
                            enabled = bloque.disponible,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (bloque.disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(text = "${bloque.hora} (${if (bloque.disponible) "Libre" else "Ocupado"})")
                        }
                    }
                }
            }
        }
    }
}
