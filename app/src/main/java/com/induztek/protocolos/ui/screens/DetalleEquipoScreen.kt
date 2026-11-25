package com.induztek.protocolos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.data.local.ConvertidorImagen
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.model.Usuario
import com.induztek.protocolos.ui.theme.NaranjoTerreno
import com.induztek.protocolos.ui.theme.VerdeFormalizado
import com.induztek.protocolos.util.PdfProtocoloGenerator
import com.induztek.protocolos.util.PermisosRol
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEquipoScreen(
    usuario: Usuario?,
    equipo: Equipo?,
    historial: List<Protocolo>,
    onNuevaPrueba: () -> Unit,
    onFormalizarProtocolo: (Protocolo) -> Unit = {},
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val puedeCrearPrueba = PermisosRol.puedeCrearProtocolo(usuario?.rol)
    val puedeFormalizar = PermisosRol.puedeFormalizarProtocolo(usuario?.rol)

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
            if (puedeCrearPrueba) {
                FloatingActionButton(onClick = onNuevaPrueba) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar prueba en terreno")
                }
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
                            "${equipo.codigo} — ${equipo.tipo.etiqueta}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Marca: ${equipo.marca} | Modelo: ${equipo.modelo} | N° Serie: ${equipo.numeroSerie}",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (equipo.potenciaKVA != null || equipo.tensionNominalKV != null) {
                            Text(
                                "Potencia: ${equipo.potenciaKVA ?: "--"} kVA | Tensión: ${equipo.tensionNominalKV ?: "--"} kV",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "Instalación: ${equipo.instalacionCliente}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!equipo.ubicacionGPS.isNullOrBlank()) {
                            Text(
                                "GPS: ${equipo.ubicacionGPS}",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (!puedeCrearPrueba && usuario != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (puedeFormalizar) " Rol Ingeniero: Revisa y Formaliza Protocolos (No crea pruebas en terreno)" else " Rol ${usuario.rol.name}: Solo Consulta / Supervisión",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
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
                        ProtocoloCard(
                            equipo = equipo,
                            protocolo = protocolo,
                            puedeFormalizar = puedeFormalizar,
                            onFormalizarProtocolo = onFormalizarProtocolo,
                            onExportarPdf = { PdfProtocoloGenerator.generarYCompartirPdf(context, equipo, protocolo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocoloCard(
    equipo: Equipo?,
    protocolo: Protocolo,
    puedeFormalizar: Boolean,
    onFormalizarProtocolo: (Protocolo) -> Unit,
    onExportarPdf: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Protocolo N° ${protocolo.id} — " + protocolo.fechaHora.format(formatter),
                    fontWeight = FontWeight.Bold
                )
                EstadoChip(protocolo.estado)
            }

            Text("Técnico: ${protocolo.tecnicoNombre.ifBlank { "Técnico Terreno" }}", style = MaterialTheme.typography.bodySmall)
            if (protocolo.ingenieroNombre != null) {
                Text("Formalizado por: ${protocolo.ingenieroNombre}", style = MaterialTheme.typography.bodySmall, color = VerdeFormalizado)
            }

            if (protocolo.temperaturaAmbiente != null || protocolo.humedadRelativa != null) {
                Text(
                    "Ambiente: ${protocolo.temperaturaAmbiente ?: "--"} °C | ${protocolo.humedadRelativa ?: "--"} % HR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            protocolo.mediciones.forEach { medicion ->
                Text("• ${medicion.nombre}: ${medicion.valor} ${medicion.unidad}".trim())
            }

            if (protocolo.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Obs: ${protocolo.observaciones}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (protocolo.fotoEvidenciaBytes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val bitmapEvidencia = remember(protocolo.fotoEvidenciaBytes) {
                    ConvertidorImagen.desdeBytes(protocolo.fotoEvidenciaBytes)
                }
                if (bitmapEvidencia != null) {
                    Image(
                        bitmap = bitmapEvidencia.asImageBitmap(),
                        contentDescription = "Evidencia Fotográfica",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(vertical = 4.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportarPdf,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF")
                }

                if (protocolo.estado == EstadoProtocolo.REGISTRADO_EN_TERRENO && puedeFormalizar) {
                    OutlinedButton(
                        onClick = { onFormalizarProtocolo(protocolo) },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Formalizar (Ingeniero)")
                    }
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
