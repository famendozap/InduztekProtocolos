package com.induztek.protocolos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.data.local.ConvertidorImagen
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.Usuario
import com.induztek.protocolos.ui.components.AvatarCircular
import com.induztek.protocolos.util.PermisosRol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEquiposScreen(
    usuario: Usuario?,
    equipos: List<Equipo>,
    onEquipoClick: (Equipo) -> Unit,
    onNuevoEquipoClick: () -> Unit,
    onVerEstadisticas: () -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val puedeCrear = PermisosRol.puedeCrearEquipo(usuario?.rol)
    val esCliente = PermisosRol.esSoloConsulta(usuario?.rol)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Induztek — Equipos") },
                actions = {
                    IconButton(onClick = onVerPerfil) {
                        val userBitmap = usuario?.fotoBytes?.let { ConvertidorImagen.desdeBytes(it) }
                        if (userBitmap != null) {
                            AvatarCircular(bitmap = userBitmap, tamano = 32.dp)
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Mi Perfil")
                        }
                    }
                    IconButton(onClick = onVerEstadisticas) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Ver Estadísticas y Gráficos"
                        )
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (puedeCrear) {
                FloatingActionButton(onClick = onNuevoEquipoClick) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar equipo")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (usuario != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVerPerfil() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val userBitmap = usuario.fotoBytes?.let { ConvertidorImagen.desdeBytes(it) }
                            AvatarCircular(bitmap = userBitmap, tamano = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = usuario.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ver / Editar Perfil",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = usuario.rol.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (esCliente) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = " Vista de Cliente — Solo Consulta de Instalaciones y Protocolos",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            if (equipos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Aún no hay equipos registrados",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (puedeCrear) {
                            Text(
                                "Toca el botón + para registrar el primero",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(equipos, key = { it.id }) { equipo ->
                        EquipoCard(equipo = equipo, onClick = { onEquipoClick(equipo) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipoCard(equipo: Equipo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    equipo.codigo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    equipo.tipo.etiqueta,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "Marca: ${equipo.marca} | Modelo: ${equipo.modelo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                equipo.instalacionCliente,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
