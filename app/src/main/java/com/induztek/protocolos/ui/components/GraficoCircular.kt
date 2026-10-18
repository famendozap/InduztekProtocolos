package com.induztek.protocolos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.model.DatoGrafico
import java.util.Locale

@Composable
fun GraficoCircular(
    datos: List<DatoGrafico>,
    modifier: Modifier = Modifier
) {
    val progreso = remember(datos) { Animatable(0f) }
    LaunchedEffect(datos) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val total = datos.sumOf { it.valor.toDouble() }.toFloat()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(12.dp)
    ) {
        if (total > 0f) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    var anguloInicio = -90f
                    datos.forEach { dato ->
                        val barrido = (dato.valor / total) * 360f * progreso.value
                        drawArc(
                            color = dato.color,
                            startAngle = anguloInicio,
                            sweepAngle = barrido,
                            useCenter = false,
                            style = Stroke(width = 32.dp.toPx())
                        )
                        anguloInicio += barrido
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                datos.forEach { dato ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(dato.color)
                        )
                        val pct = if (total > 0) (dato.valor / total) * 100 else 0f
                        Text(
                            text = String.format(Locale.US, "%s: %d (%.1f%%)", dato.etiqueta, dato.valor.toInt(), pct),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            Text("Sin datos para graficar", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
