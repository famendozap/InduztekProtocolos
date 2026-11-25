package com.induztek.protocolos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.util.EstadoReglasContrasena

@Composable
fun ReglasContrasenaCard(
    estado: EstadoReglasContrasena,
    mostrarCoincidencia: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Reglas de Seguridad de Contraseña (más de 5):",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ItemRegla(cumplida = estado.tieneLongitudMinima, texto = "Al menos 8 caracteres")
            ItemRegla(cumplida = estado.tieneMayuscula, texto = "Al menos una letra mayúscula (A-Z)")
            ItemRegla(cumplida = estado.tieneMinuscula, texto = "Al menos una letra minúscula (a-z)")
            ItemRegla(cumplida = estado.tieneNumero, texto = "Al menos un número (0-9)")
            ItemRegla(cumplida = estado.tieneSimbolo, texto = "Al menos un carácter especial/símbolo (@, #, $, %)")
            ItemRegla(cumplida = estado.sinEspacios, texto = "Sin espacios en blanco")
            if (mostrarCoincidencia) {
                ItemRegla(cumplida = estado.coinciden, texto = "Ambas contraseñas coinciden")
            }
        }
    }
}

@Composable
private fun ItemRegla(cumplida: Boolean, texto: String) {
    val colorIcono by animateColorAsState(
        targetValue = if (cumplida) Color(0xFF2E7D32) else Color(0xFFC62828),
        label = "colorRegla"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (cumplida) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = if (cumplida) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
