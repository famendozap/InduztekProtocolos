package com.induztek.protocolos.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.induztek.protocolos.model.TipoEquipo
import com.induztek.protocolos.model.Usuario
import com.induztek.protocolos.viewmodel.FormularioEquipoState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoEquipoScreen(
    clientes: List<Usuario> = emptyList(),
    mensajeError: String?,
    equipoFormState: FormularioEquipoState = FormularioEquipoState(),
    onLimpiarError: () -> Unit,
    onGuardar: (
        codigo: String,
        tipo: TipoEquipo,
        instalacionCliente: String,
        marca: String,
        modelo: String,
        numeroSerie: String,
        potenciaKVA: Double?,
        tensionNominalKV: Double?,
        clienteSeleccionado: Usuario?,
        ubicacionGPS: String?
    ) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var codigo by rememberSaveable { mutableStateOf("") }
    var tipoNombre by rememberSaveable { mutableStateOf(TipoEquipo.TRANSFORMADOR.name) }
    var instalacion by rememberSaveable { mutableStateOf("") }
    var marca by rememberSaveable { mutableStateOf("ABB") }
    var modelo by rememberSaveable { mutableStateOf("T-500") }
    var numeroSerie by rememberSaveable { mutableStateOf("") }
    var potenciaText by rememberSaveable { mutableStateOf("") }
    var tensionText by rememberSaveable { mutableStateOf("") }
    var ubicacionGPS by rememberSaveable { mutableStateOf("") }

    var clienteSeleccionado by remember { mutableStateOf<Usuario?>(null) }

    var menuTipoExpandido by remember { mutableStateOf(false) }
    var menuClienteExpandido by remember { mutableStateOf(false) }

    val tipoSeleccionado = TipoEquipo.valueOf(tipoNombre)

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
            Toast.makeText(context, "GPS simulated: Santiago", Toast.LENGTH_SHORT).show()
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

    LaunchedEffect(Unit) { onLimpiarError() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Equipo") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Selector de Cliente -> Autocompleta Dirección
            ExposedDropdownMenuBox(
                expanded = menuClienteExpandido,
                onExpandedChange = { menuClienteExpandido = it }
            ) {
                OutlinedTextField(
                    value = clienteSeleccionado?.let { "${it.empresaNombre ?: it.nombre} (${it.username})" } ?: "Seleccionar Cliente de la lista",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente Empresa") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuClienteExpandido) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = menuClienteExpandido,
                    onDismissRequest = { menuClienteExpandido = false }
                ) {
                    if (clientes.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay clientes registrados aún") },
                            onClick = { menuClienteExpandido = false }
                        )
                    } else {
                        clientes.forEach { cli ->
                            DropdownMenuItem(
                                text = { Text("${cli.empresaNombre ?: cli.nombre} — ${cli.direccionEmpresa ?: "Sin dir."}") },
                                onClick = {
                                    clienteSeleccionado = cli
                                    menuClienteExpandido = false
                                    // La dirección se completa sola!
                                    if (!cli.direccionEmpresa.isNullOrBlank()) {
                                        instalacion = cli.direccionEmpresa
                                    } else if (!cli.empresaNombre.isNullOrBlank()) {
                                        instalacion = "Planta " + cli.empresaNombre
                                    }
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it; onLimpiarError() },
                label = { Text("Código del Equipo (TAG)") },
                placeholder = { Text("Ej. TR-0231") },
                singleLine = true,
                isError = equipoFormState.codigoError != null,
                supportingText = equipoFormState.codigoError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = menuTipoExpandido,
                onExpandedChange = { menuTipoExpandido = it }
            ) {
                OutlinedTextField(
                    value = tipoSeleccionado.etiqueta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Equipo") },
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
                label = { Text("Instalación / Dirección del Cliente") },
                placeholder = { Text("Ej. Planta Lampa, Sala Eléctrica 2") },
                isError = equipoFormState.instalacionError != null,
                supportingText = equipoFormState.instalacionError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca") },
                    singleLine = true,
                    isError = equipoFormState.marcaError != null,
                    supportingText = equipoFormState.marcaError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = { Text("Modelo") },
                    singleLine = true,
                    isError = equipoFormState.modeloError != null,
                    supportingText = equipoFormState.modeloError?.let { { Text(it) } },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = numeroSerie,
                onValueChange = { numeroSerie = it },
                label = { Text("N° de Serie") },
                placeholder = { Text("SN-2024-9912") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = potenciaText,
                    onValueChange = { potenciaText = it },
                    label = { Text("Potencia (kVA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = tensionText,
                    onValueChange = { tensionText = it },
                    label = { Text("Tensión (kV)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ubicacionGPS,
                    onValueChange = { ubicacionGPS = it },
                    label = { Text("Ubicación GPS") },
                    placeholder = { Text("Lat, Long") },
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

            AnimatedVisibility(visible = mensajeError != null, enter = fadeIn(), exit = fadeOut()) {
                if (mensajeError != null) {
                    Text(mensajeError, color = MaterialTheme.colorScheme.error)
                }
            }

            Button(
                onClick = {
                    val pot = potenciaText.replace(',', '.').toDoubleOrNull()
                    val ten = tensionText.replace(',', '.').toDoubleOrNull()
                    onGuardar(codigo, tipoSeleccionado, instalacion, marca, modelo, numeroSerie, pot, ten, clienteSeleccionado, ubicacionGPS)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Equipo")
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
