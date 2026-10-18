package com.induztek.protocolos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.induztek.protocolos.model.DatoGrafico

@Composable
fun GraficoBarras(
    datos: List<DatoGrafico>,
    modifier: Modifier = Modifier
) {
    val medidorTexto = rememberTextMeasurer()
    val colorTexto = MaterialTheme.colorScheme.onSurface
    val colorEje = MaterialTheme.colorScheme.outline

    val progreso = remember(datos) { Animatable(0f) }
    LaunchedEffect(datos) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        if (datos.isEmpty()) return@Canvas

        val margenSuperior = 20.dp.toPx()
        val margenInferior = 24.dp.toPx()
        val altoDisponible = size.height - margenSuperior - margenInferior
        val maxValor = (datos.maxOfOrNull { it.valor } ?: 1f).coerceAtLeast(1f)

        val anchoBarra = (size.width / (datos.size * 2))
        val espacio = anchoBarra

        drawLine(
            color = colorEje,
            start = Offset(0f, size.height - margenInferior),
            end = Offset(size.width, size.height - margenInferior),
            strokeWidth = 2.dp.toPx()
        )

        datos.forEachIndexed { indice, dato ->
            val altoBarra = (dato.valor / maxValor) * altoDisponible * progreso.value
            val x = espacio / 2 + indice * (anchoBarra + espacio)
            val y = size.height - margenInferior - altoBarra

            drawRoundRect(
                color = dato.color,
                topLeft = Offset(x, y),
                size = Size(anchoBarra, altoBarra),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            val textoValor = dato.valor.toInt().toString()
            val layoutValor = medidorTexto.measure(textoValor, style = TextStyle(color = colorTexto, fontSize = 12.sp))
            drawText(
                textLayoutResult = layoutValor,
                topLeft = Offset(x + (anchoBarra - layoutValor.size.width) / 2, y - layoutValor.size.height - 2.dp.toPx())
            )

            val layoutEtiqueta = medidorTexto.measure(dato.etiqueta, style = TextStyle(color = colorTexto, fontSize = 11.sp))
            drawText(
                textLayoutResult = layoutEtiqueta,
                topLeft = Offset(x + (anchoBarra - layoutEtiqueta.size.width) / 2, size.height - margenInferior + 4.dp.toPx())
            )
        }
    }
}
