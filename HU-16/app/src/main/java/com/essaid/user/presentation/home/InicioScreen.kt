package com.essaid.user.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.essaid.user.domain.model.AuthSession

@Composable
fun InicioScreen(
    session: AuthSession,
    onNavigateToTab: (Int) -> Unit,
) {
    val isPatient = session.user.role?.lowercase() != "medico"
    val userName = session.user.name ?: if (isPatient) "Paciente EssAid" else "Dr. Médico"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¡Hola, $userName!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (isPatient)
                        "Bienvenido al Portal del Paciente EssAid. Aquí puedes gestionar tus citas, consultar hospitales y revisar tu información médica."
                    else
                        "Bienvenido al Portal Médico EssAid. Aquí puedes buscar pacientes, revisar historiales clínicos y gestionar la atención.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text("Acceso Rápido", style = MaterialTheme.typography.titleMedium)

        if (isPatient) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Reservar Cita",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(3) }
                )
                QuickActionCard(
                    title = "Hospitales",
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(2) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Mi Perfil",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(1) }
                )
                QuickActionCard(
                    title = "Ayuda / Info",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Buscar Paciente",
                    icon = Icons.Default.Search,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(2) }
                )
                QuickActionCard(
                    title = "Hospitales",
                    icon = Icons.Default.Home,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(3) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Mi Perfil",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(1) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
