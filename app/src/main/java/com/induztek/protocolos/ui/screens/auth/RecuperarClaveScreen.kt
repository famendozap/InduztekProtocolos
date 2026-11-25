package com.induztek.protocolos.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.induztek.protocolos.ui.components.ReglasContrasenaCard
import com.induztek.protocolos.util.ValidadorContrasena
import com.induztek.protocolos.viewmodel.auth.FormularioRecuperarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarClaveScreen(
    cargando: Boolean = false,
    mensajeError: String? = null,
    recuperarState: FormularioRecuperarState = FormularioRecuperarState(),
    onRecuperarClick: (username: String, respuestaSeguridad: String, nuevaClave: String, confirmacion: String) -> Unit,
    onVolver: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var respuestaSeguridad by remember { mutableStateOf("") }
    var nuevaClave by remember { mutableStateOf("") }
    var confirmacionClave by remember { mutableStateOf("") }

    val estadoReglas = remember(nuevaClave, confirmacionClave) {
        ValidadorContrasena.evaluar(nuevaClave, confirmacionClave)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Ingresa tus datos para restablecer la clave",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de Usuario") },
                singleLine = true,
                isError = recuperarState.usernameError != null,
                supportingText = recuperarState.usernameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = "¿Ciudad de nacimiento?",
                onValueChange = {},
                readOnly = true,
                label = { Text("Pregunta de Seguridad") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = respuestaSeguridad,
                onValueChange = { respuestaSeguridad = it },
                label = { Text("Respuesta de Seguridad") },
                placeholder = { Text("Ej. Santiago") },
                singleLine = true,
                isError = recuperarState.respuestaError != null,
                supportingText = recuperarState.respuestaError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nuevaClave,
                onValueChange = { nuevaClave = it },
                label = { Text("Nueva Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = recuperarState.nuevaContrasenaError != null,
                supportingText = recuperarState.nuevaContrasenaError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmacionClave,
                onValueChange = { confirmacionClave = it },
                label = { Text("Confirmar Nueva Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = recuperarState.confirmacionError != null,
                supportingText = recuperarState.confirmacionError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            ReglasContrasenaCard(estado = estadoReglas)

            AnimatedVisibility(visible = mensajeError != null, enter = fadeIn(), exit = fadeOut()) {
                if (mensajeError != null) {
                    Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onRecuperarClick(username, respuestaSeguridad, nuevaClave, confirmacionClave)
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
                    Text("Restablecer Contraseña")
                }
            }
        }
    }
}
