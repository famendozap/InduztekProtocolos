package com.induztek.protocolos.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.induztek.protocolos.viewmodel.FormularioProtocoloState
import java.util.Locale

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
    protocoloFormState: FormularioProtocoloState = FormularioProtocoloState(),
    onLimpiarError: () -> Unit,
    onGuardar: (
        mediciones: List<Medicion>,
        observaciones: String,
        fotoEvidenciaBytes: ByteArray?,
        temperaturaAmbiente: Double?,
        humedadRelativa: Double?,
        instrumentosUsados: String?,
        resultadoGeneral: String,
        ubicacionGPS: String?
    ) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val mediciones = rememberSaveable(saver = MedicionesSaver) { mutableStateListOf<Medicion>() }
    var nombreMedicion by rememberSaveable { mutableStateOf("") }
    var valorMedicion by rememberSaveable { mutableStateOf("") }
    var unidadMedicion by rememberSaveable { mutableStateOf("") }
    var observaciones by rememberSaveable { mutableStateOf("") }

    var temperaturaText by rememberSaveable { mutableStateOf("22.5") }
    var humedadText by rememberSaveable { mutableStateOf("45") }
    var instrumentosUsados by rememberSaveable { mutableStateOf("Megger MIT515, Omicron TTR500") }
    var resultadoGeneral by rememberSaveable { mutableStateOf("CONFORME") }
    var menuResultadoExpandido by remember { mutableStateOf(false) }
    var ubicacionGPS by rememberSaveable { mutableStateOf("") }

    var errorLocal by rememberSaveable { mutableStateOf<String?>(null) }
    var fotoEvidenciaBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launchers Cámara y Galería para Evidencia
    val lanzadorCamaraEvidencia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) fotoEvidenciaBitmap = bitmap
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzadorCamaraEvidencia.launch(null)
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    val lanzadorGaleriaEvidencia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                fotoEvidenciaBitmap = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar la foto de galería", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            null
        }

        if (location != null) {
            ubicacionGPS = String.format(Locale.US, "%.5f, %.5f", location.latitude, location.longitude)
            Toast.makeText(context, "Ubicación GPS capturada", Toast.LENGTH_SHORT).show()
        } else {
            ubicacionGPS = "-33.4372, -70.6506 (Santiago)"
            Toast.makeText(context, "GPS simulado: Santiago", Toast.LENGTH_SHORT).show()
        }
    }

    val pedirPermisoUbicacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true || permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    fun intentarObtenerUbicacion() {
        val fineOk = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineOk || coarseOk) {
            obtenerUbicacionActual()
        } else {
            pedirPermisoUbicacion.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    var r1minText by rememberSaveable { mutableStateOf("") }
    var r10minText by rememberSaveable { mutableStateOf("") }

    var v1Text by rememberSaveable { mutableStateOf("") }
    var v2Text by rememberSaveable { mutableStateOf("") }
    var vPlacaText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { onLimpiarError() }

    val hayMedicionPendiente = nombreMedicion.isNotBlank() || valorMedicion.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Prueba — $codigoEquipo") },
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
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(" Evidencia Fotográfica en Terreno", style = MaterialTheme.typography.titleMedium)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    lanzadorCamaraEvidencia.launch(null)
                                } else {
                                    pedirPermisoCamara.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cámara")
                        }
                        OutlinedButton(
                            onClick = { lanzadorGaleriaEvidencia.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galería")
                        }
                    }
                }
            }

            // Datos Ambientales e Instrumentos
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Condiciones Ambientales e Instrumentos", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = temperaturaText,
                            onValueChange = { temperaturaText = it },
                            label = { Text("Temp. Amb. (°C)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = humedadText,
                            onValueChange = { humedadText = it },
                            label = { Text("Humedad (% HR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = instrumentosUsados,
                        onValueChange = { instrumentosUsados = it },
                        label = { Text("Instrumentos Utilizados") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = menuResultadoExpandido,
                        onExpandedChange = { menuResultadoExpandido = it }
                    ) {
                        OutlinedTextField(
                            value = resultadoGeneral,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Resultado General del Protocolo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuResultadoExpandido) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = menuResultadoExpandido,
                            onDismissRequest = { menuResultadoExpandido = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("CONFORME") },
                                onClick = { resultadoGeneral = "CONFORME"; menuResultadoExpandido = false }
                            )
                            DropdownMenuItem(
                                text = { Text("CON OBSERVACIONES") },
                                onClick = { resultadoGeneral = "CON OBSERVACIONES"; menuResultadoExpandido = false }
                            )
                            DropdownMenuItem(
                                text = { Text("NO CONFORME") },
                                onClick = { resultadoGeneral = "NO CONFORME"; menuResultadoExpandido = false }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ubicacionGPS,
                            onValueChange = { ubicacionGPS = it },
                            label = { Text("Ubicación GPS Terreno") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { intentarObtenerUbicacion() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GPS")
                        }
                    }
                }
            }

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

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cálculo Automático de TTR (Relación Transformación)", style = MaterialTheme.typography.titleSmall)
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

            if (protocoloFormState.medicionesError != null) {
                Text(protocoloFormState.medicionesError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

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
                placeholder = { Text("Ej. Resistencia de Contacto") },
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
                    placeholder = { Text("µΩ") },
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
                Text("Agregar Medición")
            }

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            val error = errorLocal ?: mensajeError
            AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
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
                        val temp = temperaturaText.replace(',', '.').toDoubleOrNull()
                        val hum = humedadText.replace(',', '.').toDoubleOrNull()
                        onGuardar(lista, observaciones, fotoEvidenciaBytes, temp, hum, instrumentosUsados, resultadoGeneral, ubicacionGPS)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Protocolo en Terreno")
            }

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
