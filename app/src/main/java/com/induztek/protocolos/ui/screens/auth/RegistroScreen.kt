package com.induztek.protocolos.ui.screens.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.induztek.protocolos.data.local.ConvertidorImagen
import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.ui.components.AvatarCircular
import com.induztek.protocolos.ui.components.ReglasContrasenaCard
import com.induztek.protocolos.util.ValidadorContrasena
import com.induztek.protocolos.viewmodel.auth.FormularioRegistroState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    cargando: Boolean = false,
    mensajeError: String? = null,
    registroState: FormularioRegistroState = FormularioRegistroState(),
    onLimpiarError: () -> Unit = {},
    onRegistrarClick: (
        username: String,
        nombre: String,
        contrasena: String,
        confirmacion: String,
        rol: Rol,
        fotoBytes: ByteArray?,
        preguntaSeguridad: String,
        respuestaSeguridad: String,
        campoRol1: String,
        campoRol2: String
    ) -> Unit,
    onVolverALogin: () -> Unit
) {
    LaunchedEffect(Unit) { onLimpiarError() }
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(Rol.TECNICO) }
    var menuRolExpandido by remember { mutableStateOf(false) }

    var preguntaSeguridad by remember { mutableStateOf("¿Ciudad de nacimiento?") }
    var respuestaSeguridad by remember { mutableStateOf("") }

    var campoRol1 by remember { mutableStateOf("") }
    var campoRol2 by remember { mutableStateOf("") }

    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val estadoReglas = remember(contrasena, confirmacion) {
        ValidadorContrasena.evaluar(contrasena, confirmacion)
    }

    val (labelRol1, labelRol2) = remember(rolSeleccionado) {
        when (rolSeleccionado) {
            Rol.TECNICO -> Pair("Certificación SEC (ej. SEC-A-1234)", "Especialidad (ej. Alta Tensión)")
            Rol.INGENIERO -> Pair("RUT / N° Colegiatura CIP", "Registro Profesional / Firma")
            Rol.JEFE -> Pair("Departamento / Sucursal", "Código de Autorización")
            Rol.CLIENTE -> Pair("Nombre de la Empresa", "RUT Empresa")
        }
    }

    // Launchers Cámara y Galería
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) fotoBitmap = bitmap
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzadorCamara.launch(null)
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
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
                fotoBitmap = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "Crear Nueva Cuenta", style = MaterialTheme.typography.headlineMedium)

        AvatarCircular(bitmap = fotoBitmap, tamano = 80.dp)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        lanzadorCamara.launch(null)
                    } else {
                        pedirPermisoCamara.launch(Manifest.permission.CAMERA)
                    }
                }
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cámara")
            }
            OutlinedButton(
                onClick = { lanzadorGaleria.launch("image/*") }
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Galería")
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                onLimpiarError()
            },
            label = { Text("Nombre de Usuario (para iniciar sesión)") },
            placeholder = { Text("Ej. carlos123") },
            singleLine = true,
            isError = registroState.usernameError != null,
            supportingText = registroState.usernameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                onLimpiarError()
            },
            label = { Text("Nombre Completo (ej. Carlos Pérez)") },
            placeholder = { Text("Carlos Pérez") },
            singleLine = true,
            isError = registroState.nombreError != null,
            supportingText = registroState.nombreError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                onLimpiarError()
            },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = registroState.contrasenaError != null,
            supportingText = registroState.contrasenaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmacion,
            onValueChange = {
                confirmacion = it
                onLimpiarError()
            },
            label = { Text("Repetir Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = registroState.confirmacionError != null,
            supportingText = registroState.confirmacionError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        ReglasContrasenaCard(estado = estadoReglas)

        OutlinedTextField(
            value = respuestaSeguridad,
            onValueChange = {
                respuestaSeguridad = it
                onLimpiarError()
            },
            label = { Text("Respuesta a: ¿Ciudad de nacimiento?") },
            placeholder = { Text("Ej. Santiago") },
            singleLine = true,
            isError = registroState.respuestaError != null,
            supportingText = registroState.respuestaError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = menuRolExpandido,
            onExpandedChange = { menuRolExpandido = !menuRolExpandido },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = when (rolSeleccionado) {
                    Rol.TECNICO -> "Técnico de Terreno"
                    Rol.INGENIERO -> "Ingeniero de Pruebas"
                    Rol.JEFE -> "Jefe Técnico"
                    Rol.CLIENTE -> "Usuario Cliente (Empresa)"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Rol en la Empresa") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuRolExpandido) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = menuRolExpandido,
                onDismissRequest = { menuRolExpandido = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Técnico de Terreno") },
                    onClick = { rolSeleccionado = Rol.TECNICO; campoRol1 = ""; campoRol2 = ""; menuRolExpandido = false; onLimpiarError() }
                )
                DropdownMenuItem(
                    text = { Text("Ingeniero de Pruebas") },
                    onClick = { rolSeleccionado = Rol.INGENIERO; campoRol1 = ""; campoRol2 = ""; menuRolExpandido = false; onLimpiarError() }
                )
                DropdownMenuItem(
                    text = { Text("Jefe Técnico") },
                    onClick = { rolSeleccionado = Rol.JEFE; campoRol1 = ""; campoRol2 = ""; menuRolExpandido = false; onLimpiarError() }
                )
                DropdownMenuItem(
                    text = { Text("Usuario Cliente (Empresa)") },
                    onClick = { rolSeleccionado = Rol.CLIENTE; campoRol1 = ""; campoRol2 = ""; menuRolExpandido = false; onLimpiarError() }
                )
            }
        }

        // Datos específicos por rol
        OutlinedTextField(
            value = campoRol1,
            onValueChange = {
                campoRol1 = it
                onLimpiarError()
            },
            label = { Text(labelRol1) },
            singleLine = true,
            isError = registroState.campoRolError != null,
            supportingText = registroState.campoRolError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = campoRol2,
            onValueChange = {
                campoRol2 = it
                onLimpiarError()
            },
            label = { Text(labelRol2) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = mensajeError != null, enter = fadeIn(), exit = fadeOut()) {
            if (mensajeError != null) {
                Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val fotoBytes = fotoBitmap?.let { ConvertidorImagen.aBytes(it) }
                onRegistrarClick(
                    username,
                    nombre,
                    contrasena,
                    confirmacion,
                    rolSeleccionado,
                    fotoBytes,
                    preguntaSeguridad,
                    respuestaSeguridad,
                    campoRol1,
                    campoRol2
                )
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Registrar e Ingresar")
            }
        }

        TextButton(onClick = onVolverALogin) {
            Text("¿Ya tienes cuenta? Inicia Sesión aquí")
        }
    }
}
