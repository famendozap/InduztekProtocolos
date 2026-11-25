package com.induztek.protocolos.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.data.local.ConvertidorImagen
import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.model.Usuario
import com.induztek.protocolos.ui.components.AvatarCircular

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    cargando: Boolean = false,
    mensajeError: String? = null,
    mensajeExito: String? = null,
    onGuardarPerfil: (nombre: String, fotoBytes: ByteArray?, campoRol1: String, campoRol2: String) -> Unit,
    onIrACambiarClave: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var editando by remember { mutableStateOf(false) }

    var nombre by remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var fotoBitmap by remember(usuario) {
        mutableStateOf<Bitmap?>(usuario?.fotoBytes?.let { ConvertidorImagen.desdeBytes(it) })
    }

    val (labelRol1, labelRol2) = remember(usuario?.rol) {
        when (usuario?.rol) {
            Rol.TECNICO -> Pair("Certificación SEC", "Especialidad Técnica")
            Rol.INGENIERO -> Pair("RUT / N° Colegiatura (CIP)", "Registro Profesional / Firma")
            Rol.JEFE -> Pair("Departamento / Sucursal", "Código de Autorización")
            Rol.CLIENTE -> Pair("Nombre de la Empresa", "RUT Empresa")
            null -> Pair("Dato 1", "Dato 2")
        }
    }

    var campoRol1 by remember(usuario) {
        mutableStateOf(
            when (usuario?.rol) {
                Rol.TECNICO -> usuario.secCertificacion ?: ""
                Rol.INGENIERO -> usuario.rutColegiatura ?: ""
                Rol.JEFE -> usuario.departamentoSucursal ?: ""
                Rol.CLIENTE -> usuario.empresaNombre ?: ""
                null -> ""
            }
        )
    }

    var campoRol2 by remember(usuario) {
        mutableStateOf(
            when (usuario?.rol) {
                Rol.TECNICO -> usuario.especialidadTecnica ?: ""
                Rol.INGENIERO -> usuario.registroProfesional ?: ""
                Rol.JEFE -> usuario.codigoAutorizacion ?: ""
                Rol.CLIENTE -> usuario.rutEmpresa ?: ""
                null -> ""
            }
        )
    }

    // Launchers para Cámara y Galería
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
                Toast.makeText(context, "Error al cargar imagen de la galería", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { editando = !editando }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarCircular(bitmap = fotoBitmap, tamano = 90.dp)

            if (editando) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Usuario: ${usuario?.username ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Rol: ${usuario?.rol?.name ?: ""}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (editando) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre Completo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = campoRol1,
                            onValueChange = { campoRol1 = it },
                            label = { Text(labelRol1) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = campoRol2,
                            onValueChange = { campoRol2 = it },
                            label = { Text(labelRol2) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Nombre: ${usuario?.nombre ?: ""}", style = MaterialTheme.typography.bodyMedium)
                        Text("$labelRol1: ${campoRol1.ifBlank { "No registrado" }}", style = MaterialTheme.typography.bodyMedium)
                        Text("$labelRol2: ${campoRol2.ifBlank { "No registrado" }}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AnimatedVisibility(visible = mensajeError != null, enter = fadeIn(), exit = fadeOut()) {
                if (mensajeError != null) Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
            }

            AnimatedVisibility(visible = mensajeExito != null, enter = fadeIn(), exit = fadeOut()) {
                if (mensajeExito != null) Text(text = mensajeExito, color = MaterialTheme.colorScheme.primary)
            }

            if (editando) {
                Button(
                    onClick = {
                        val bytes = fotoBitmap?.let { ConvertidorImagen.aBytes(it) }
                        onGuardarPerfil(nombre, bytes, campoRol1, campoRol2)
                        editando = false
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (cargando) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Guardar Cambios de Perfil")
                    }
                }
            }

            OutlinedButton(
                onClick = onIrACambiarClave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar Contraseña")
            }
        }
    }
}
