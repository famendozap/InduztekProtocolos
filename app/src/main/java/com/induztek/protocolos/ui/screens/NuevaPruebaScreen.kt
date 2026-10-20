package com.induztek.protocolos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.induztek.protocolos.data.local.ConvertidorImagen
import com.induztek.protocolos.model.CalculosPruebas
import com.induztek.protocolos.model.Medicion
import java.util.Locale

/** Permite que la lista de mediciones sobreviva a una rotación de pantalla. */
private val MedicionesSaver = listSaver<SnapshotStateList<Medicion>, ArrayList<String>>(
    save = { lista -> lista.map { arrayListOf(it.nombre, it.valor, it.unidad) } },
    restore = { guardado ->
        mutableStateListOf<Medicion>().apply {
            addAll(guardado.map { Medicion(it[0], it[1], it[2]) })
        }
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaPruebaScreen(
    codigoEquipo: String,
    mensajeError: String?,
    onLimpiarError: () -> Unit,
    onGuardar: (mediciones: List<Medicion>, observaciones: String, fotoEvidenciaBytes: ByteArray?) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val mediciones = rememberSaveable(saver = MedicionesSaver) { mutableStateListOf<Medicion>() }
    var nombreMedicion by rememberSaveable { mutableStateOf("") }
    var valorMedicion by rememberSaveable { mutableStateOf("") }
    var unidadMedicion by rememberSaveable { mutableStateOf("") }
    var observaciones by rememberSaveable { mutableStateOf("") }
    var errorLocal by rememberSaveable { mutableStateOf<String?>(null) }
    var fotoEvidenciaBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Lanzador de Cámara para Evidencia Fotográfica en Terreno
    val lanzadorCamaraEvidencia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoEvidenciaBitmap = bitmap
        }
    }

    // Solicitante de Permiso de Cámara
    val pedirPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            lanzadorCamaraEvidencia.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    fun abrirCamara() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            lanzadorCamaraEvidencia.launch(null)
        } else {
            pedirPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    // Estados para Asistente IP (Índice de Polarización)
    var r1minText by rememberSaveable { mutableStateOf("") }
    var r10minText by rememberSaveable { mutableStateOf("") }

    // Estados para Asistente TTR
    var v1Text by rememberSaveable { mutableStateOf("") }
    var v2Text by rememberSaveable { mutableStateOf("") }
    var vPlacaText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { onLimpiarError() }

    val hayMedicionPendiente = nombreMedicion.isNotBlank() || valorMedicion.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva prueba — $codigoEquipo") },
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
            // --- FOTO DE EVIDENCIA EN TERRENO CON CÁMARA ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📷 Evidencia Fotográfica en Terreno", style = MaterialTheme.typography.titleMedium)
                    if (fotoEvidenciaBitmap != null) {
                        Image(
                            bitmap = fotoEvidenciaBitmap!!.asImageBitmap(),
                            contentDescription = "Foto Evidencia",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    OutlinedButton(
                        onClick = { abrirCamara() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (fotoEvidenciaBitmap == null) "📷 Tomar Foto de Evidencia" else "📷 Cambiar Foto de Evidencia")
                    }
                }
            }

            // --- ASISTENTE DE CÁLCULO IP ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚡ Cálculo Automático de IP (Índice de Polarización)", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = r1minText,
                            onValueChange = { r1minText = it },
                            label = { Text("R (1 min) MΩ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = r10minText,
                            onValueChange = { r10minText = it },
                            label = { Text("R (10 min) MΩ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            val r1 = r1minText.replace(',', '.').toDoubleOrNull()
                            val r10 = r10minText.replace(',', '.').toDoubleOrNull()
                            if (r1 != null && r10 != null && r1 > 0) {
                                val (ip, evaluacion) = CalculosPruebas.calcularIndicePolarizacion(r1, r10)
                                mediciones.add(Medicion("Resistencia 1 min", r1.toString(), "MΩ"))
                                mediciones.add(Medicion("Resistencia 10 min", r10.toString(), "MΩ"))
                                mediciones.add(Medicion("Índice Polarización (IP)", String.format(Locale.US, "%.2f", ip), "($evaluacion)"))
                                r1minText = ""
                                r10minText = ""
                            } else {
                                errorLocal = "Ingresa valores numéricos válidos para R 1min y R 10min"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calcular IP y Agregar")
                    }
                }
            }

            // --- ASISTENTE DE CÁLCULO TTR ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔄 Cálculo Automático de TTR (Relación Transformación)", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = v1Text,
                            onValueChange = { v1Text = it },
                            label = { Text("V Primario") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = v2Text,
                            onValueChange = { v2Text = it },
                            label = { Text("V Secundario") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = vPlacaText,
                            onValueChange = { vPlacaText = it },
                            label = { Text("Ratio Placa") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            val v1 = v1Text.replace(',', '.').toDoubleOrNull()
                            val v2 = v2Text.replace(',', '.').toDoubleOrNull()
                            val vPlaca = vPlacaText.replace(',', '.').toDoubleOrNull()
                            if (v1 != null && v2 != null && vPlaca != null) {
                                val res = CalculosPruebas.calcularTtr(v1, v2, vPlaca)
                                mediciones.add(Medicion("TTR Medido", String.format(Locale.US, "%.2f", res.ttrMedido), ""))
                                mediciones.add(Medicion("TTR Variación", String.format(Locale.US, "%.2f", res.variacionPorcentaje), "%"))
                                mediciones.add(Medicion("TTR Estado", if (res.superaTolerancia) "ALERTA > 25%" else "OK", res.mensaje))
                                v1Text = ""
                                v2Text = ""
                                vPlacaText = ""
                            } else {
                                errorLocal = "Ingresa voltajes numéricos válidos para TTR"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calcular TTR y Agregar")
                    }
                }
            }

            Text("Lista de Mediciones Registradas", style = MaterialTheme.typography.titleMedium)

            mediciones.forEachIndexed { index, medicion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${medicion.nombre}: ${medicion.valor} ${medicion.unidad}".trim())
                        }
                        IconButton(onClick = { mediciones.removeAt(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar medición")
                        }
                    }
                }
            }

            Text("Agregar Medición Libre", style = MaterialTheme.typography.titleSmall)

            OutlinedTextField(
                value = nombreMedicion,
                onValueChange = { nombreMedicion = it; errorLocal = null },
                label = { Text("Parámetro") },
                placeholder = { Text("Ej. Temperatura de aceite") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = valorMedicion,
                    onValueChange = { valorMedicion = it; errorLocal = null },
                    label = { Text("Valor") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unidadMedicion,
                    onValueChange = { unidadMedicion = it },
                    label = { Text("Unidad") },
                    placeholder = { Text("°C") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = {
                    val nueva = Medicion.desdeTexto(nombreMedicion, valorMedicion, unidadMedicion)
                    if (nueva == null) {
                        errorLocal = "Indica el parámetro y un valor numérico"
                    } else {
                        mediciones.add(nueva)
                        nombreMedicion = ""
                        valorMedicion = ""
                        unidadMedicion = ""
                        errorLocal = null
                        onLimpiarError()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar medición")
            }

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            val error = errorLocal ?: mensajeError
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val lista = mediciones.toMutableList()
                    var valido = true
                    if (hayMedicionPendiente) {
                        val pendiente = Medicion.desdeTexto(nombreMedicion, valorMedicion, unidadMedicion)
                        if (pendiente == null) {
                            errorLocal = "La medición escrita no es válida"
                            valido = false
                        } else {
                            lista.add(pendiente)
                        }
                    }
                    if (valido) {
                        errorLocal = null
                        val fotoEvidenciaBytes = fotoEvidenciaBitmap?.let { ConvertidorImagen.aBytes(it) }
                        onGuardar(lista, observaciones, fotoEvidenciaBytes)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar protocolo")
            }

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
