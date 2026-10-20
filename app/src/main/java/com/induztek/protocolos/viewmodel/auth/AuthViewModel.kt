package com.induztek.protocolos.viewmodel.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.data.local.InduztekDbHelper
import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    fun login(username: String, contrasena: String) {
        if (username.isBlank() || contrasena.isBlank()) {
            _mensajeError.value = "Ingresa usuario y contraseña"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            val usuarioEncontrado = withContext(Dispatchers.IO) {
                dbHelper.autenticarUsuario(username, contrasena)
            }

            if (usuarioEncontrado != null) {
                _usuarioActual.value = usuarioEncontrado
            } else {
                _mensajeError.value = "Usuario o contraseña incorrectos. Si no tienes cuenta, ¡regístrate!"
            }

            _cargando.value = false
        }
    }

    fun registrar(username: String, nombre: String, contrasena: String, rol: Rol, fotoBytes: ByteArray? = null) {
        if (username.isBlank() || nombre.isBlank() || contrasena.isBlank()) {
            _mensajeError.value = "Por favor completa todos los campos"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            val resultadoId = withContext(Dispatchers.IO) {
                dbHelper.registrarUsuario(username, nombre, contrasena, rol.name, fotoBytes)
            }

            if (resultadoId != -1L) {
                _usuarioActual.value = Usuario(id = resultadoId, username = username.trim().lowercase(), nombre = nombre.trim(), rol = rol, fotoBytes = fotoBytes)
            } else {
                _mensajeError.value = "El usuario '$username' ya existe. Elige otro usuario."
            }

            _cargando.value = false
        }
    }

    fun logout() {
        _usuarioActual.value = null
    }

    fun limpiarError() {
        _mensajeError.value = null
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(application) as T
        }
    }
}
