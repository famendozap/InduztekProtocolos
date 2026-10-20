package com.induztek.protocolos.ui.screens.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    cargando: Boolean = false,
    mensajeError: String? = null,
    onRegistrarClick: (username: String, nombre: String, contrasena: String, rol: Rol, fotoBytes: ByteArray?) -> Unit,
    onVolverALogin: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(Rol.TECNICO) }
    var menuRolExpandido by remember { mutableStateOf(false) }
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Lanzador de Cámara de Compose
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoBitmap = bitmap
        }
    }

    // Solicitante de Permiso de Cámara
    val pedirPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            lanzadorCamara.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    fun abrirCamara() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            lanzadorCamara.launch(null)
        } else {
            pedirPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Crear Nueva Cuenta", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar con cámara
        AvatarCircular(bitmap = fotoBitmap, tamano = 72.dp)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = { abrirCamara() }) {
            Text(" Tomar Foto de Credencial")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre Completo (ej. Juan Pérez)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                    onClick = { rolSeleccionado = Rol.TECNICO; menuRolExpandido = false }
                )
                DropdownMenuItem(
                    text = { Text("Ingeniero de Pruebas") },
                    onClick = { rolSeleccionado = Rol.INGENIERO; menuRolExpandido = false }
                )
                DropdownMenuItem(
                    text = { Text("Jefe Técnico") },
                    onClick = { rolSeleccionado = Rol.JEFE; menuRolExpandido = false }
                )
                DropdownMenuItem(
                    text = { Text("Usuario Cliente (Empresa)") },
                    onClick = { rolSeleccionado = Rol.CLIENTE; menuRolExpandido = false }
                )
            }
        }

        if (mensajeError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val fotoBytes = fotoBitmap?.let { ConvertidorImagen.aBytes(it) }
                onRegistrarClick(username, nombre, contrasena, rolSeleccionado, fotoBytes)
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

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onVolverALogin) {
            Text("¿Ya tienes cuenta? Inicia Sesión aquí")
        }
    }
}
