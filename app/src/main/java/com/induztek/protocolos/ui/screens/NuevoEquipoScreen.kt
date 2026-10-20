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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.model.TipoEquipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoEquipoScreen(
    mensajeError: String?,
    onLimpiarError: () -> Unit,
    onGuardar: (codigo: String, tipo: TipoEquipo, instalacionCliente: String) -> Unit,
    onVolver: () -> Unit
) {
    var codigo by rememberSaveable { mutableStateOf("") }
    var tipoNombre by rememberSaveable { mutableStateOf(TipoEquipo.TRANSFORMADOR.name) }
    var instalacion by rememberSaveable { mutableStateOf("") }
    var menuTipoExpandido by remember { mutableStateOf(false) }
    val tipoSeleccionado = TipoEquipo.valueOf(tipoNombre)

    LaunchedEffect(Unit) { onLimpiarError() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo equipo") },
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
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it; onLimpiarError() },
                label = { Text("Código del equipo (TAG)") },
                placeholder = { Text("Ej. TR-0231") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = menuTipoExpandido,
                onExpandedChange = { menuTipoExpandido = it }
            ) {
                OutlinedTextField(
                    value = tipoSeleccionado.etiqueta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de equipo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTipoExpandido) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = menuTipoExpandido,
                    onDismissRequest = { menuTipoExpandido = false }
                ) {
                    TipoEquipo.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.etiqueta) },
                            onClick = {
                                tipoNombre = tipo.name
                                menuTipoExpandido = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = instalacion,
                onValueChange = { instalacion = it; onLimpiarError() },
                label = { Text("Instalación / ubicación del cliente") },
                placeholder = { Text("Ej. Planta Lampa, sala eléctrica 2") },
                modifier = Modifier.fillMaxWidth()
            )

            if (mensajeError != null) {
                Text(mensajeError, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { onGuardar(codigo, tipoSeleccionado, instalacion) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar equipo")
            }

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
