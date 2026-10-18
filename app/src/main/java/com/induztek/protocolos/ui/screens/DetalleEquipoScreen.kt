package com.induztek.protocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.ui.theme.NaranjoTerreno
import com.induztek.protocolos.ui.theme.VerdeFormalizado
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEquipoScreen(
    equipo: Equipo?,
    historial: List<Protocolo>,
    onNuevaPrueba: () -> Unit,
    onFormalizarProtocolo: (Protocolo) -> Unit = {},
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(equipo?.codigo ?: "Equipo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaPrueba) {
                Icon(Icons.Default.Add, contentDescription = "Registrar prueba")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (equipo != null) {
                Surface(color = MaterialTheme.colorScheme.primary) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            equipo.tipo.etiqueta,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            equipo.instalacionCliente,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (historial.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sin pruebas registradas aún para este equipo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historial, key = { it.id }) { protocolo ->
                        ProtocoloCard(protocolo, onFormalizarProtocolo)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocoloCard(
    protocolo: Protocolo,
    onFormalizarProtocolo: (Protocolo) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    protocolo.fechaHora.format(formatter),
                    fontWeight = FontWeight.Bold
                )
                EstadoChip(protocolo.estado)
            }
            protocolo.mediciones.forEach { medicion ->
                Text("• ${medicion.nombre}: ${medicion.valor} ${medicion.unidad}".trim())
            }
            if (protocolo.observaciones.isNotBlank()) {
                Text(
                    protocolo.observaciones,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (protocolo.estado == EstadoProtocolo.REGISTRADO_EN_TERRENO) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onFormalizarProtocolo(protocolo) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Formalizar Protocolo (Ingeniero / Jefe)")
                }
            }
        }
    }
}

@Composable
private fun EstadoChip(estado: EstadoProtocolo) {
    val color = if (estado == EstadoProtocolo.FORMALIZADO) VerdeFormalizado else NaranjoTerreno
    Surface(color = color.copy(alpha = 0.15f)) {
        Text(
            estado.etiqueta,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
