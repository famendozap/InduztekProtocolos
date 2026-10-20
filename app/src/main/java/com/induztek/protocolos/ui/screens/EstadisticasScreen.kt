package com.induztek.protocolos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.model.DatoGrafico
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.model.TipoEquipo
import com.induztek.protocolos.ui.components.GraficoBarras
import com.induztek.protocolos.ui.components.GraficoCircular

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    equipos: List<Equipo>,
    protocolos: List<Protocolo>,
    onVolver: () -> Unit
) {
    // 1. Conteo real dinámico de Equipos por Tipo
    val transformadoresCount = equipos.count { it.tipo == TipoEquipo.TRANSFORMADOR }.toFloat()
    val releCount = equipos.count { it.tipo == TipoEquipo.RELE_PROTECCION }.toFloat()
    val celdasCount = equipos.count { it.tipo == TipoEquipo.CELDA }.toFloat()
    val cablesCount = equipos.count { it.tipo == TipoEquipo.CABLE }.toFloat()
    val otrosCount = equipos.count { it.tipo == TipoEquipo.OTRO }.toFloat()

    val datosBarras = listOf(
        DatoGrafico("Trafo", transformadoresCount, Color(0xFF1E88E5)),
        DatoGrafico("Relé", releCount, Color(0xFF43A047)),
        DatoGrafico("Celda", celdasCount, Color(0xFFFB8C00)),
        DatoGrafico("Cable", cablesCount, Color(0xFF8E24AA)),
        DatoGrafico("Otro", otrosCount, Color(0xFF00ACC1))
    )

    // 2. Conteo real dinámico de Protocolos por Estado
    val registradosCount = protocolos.count { it.estado == EstadoProtocolo.REGISTRADO_EN_TERRENO }.toFloat()
    val formalizadosCount = protocolos.count { it.estado == EstadoProtocolo.FORMALIZADO }.toFloat()

    val datosTorta = listOf(
        DatoGrafico("En Terreno", registradosCount, Color(0xFFFF9800)),
        DatoGrafico("Formalizados", formalizadosCount, Color(0xFF4CAF50))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Estadísticas en Tiempo Real") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribución de Equipos por Tipo (${equipos.size} Total)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    GraficoBarras(datos = datosBarras)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Estado de Protocolos (${protocolos.size} Total)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    GraficoCircular(datos = datosTorta)
                }
            }
        }
    }
}
