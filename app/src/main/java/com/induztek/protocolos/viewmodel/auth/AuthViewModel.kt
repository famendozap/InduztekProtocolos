package com.induztek.protocolos.viewmodel.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.data.local.InduztekDbHelper
import com.induztek.protocolos.data.remote.EmptyApiResponseException
import com.induztek.protocolos.data.repository.InduztekRepository
import com.induztek.protocolos.data.remote.dto.RegistroRequestDto
import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.model.Usuario
import com.induztek.protocolos.util.ValidadorContrasena
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class FormularioRegistroState(
    val usernameError: String? = null,
    val nombreError: String? = null,
    val contrasenaError: String? = null,
    val confirmacionError: String? = null,
    val preguntaError: String? = null,
    val respuestaError: String? = null,
    val campoRolError: String? = null
) {
    val tieneErrores: Boolean
        get() = usernameError != null || nombreError != null || contrasenaError != null ||
                confirmacionError != null || preguntaError != null || respuestaError != null || campoRolError != null
}

data class FormularioRecuperarState(
    val usernameError: String? = null,
    val respuestaError: String? = null,
    val nuevaContrasenaError: String? = null,
    val confirmacionError: String? = null
)

data class FormularioCambiarClaveState(
    val actualError: String? = null,
    val nuevaError: String? = null,
    val confirmacionError: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = InduztekDbHelper(application)
    private val repository = InduztekRepository(dbHelper)

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // Form states with field-level errors (isError & supportingText)
    private val _loginUsernameError = MutableStateFlow<String?>(null)
    val loginUsernameError: StateFlow<String?> = _loginUsernameError.asStateFlow()

    private val _loginContrasenaError = MutableStateFlow<String?>(null)
    val loginContrasenaError: StateFlow<String?> = _loginContrasenaError.asStateFlow()

    private val _registroState = MutableStateFlow(FormularioRegistroState())
    val registroState: StateFlow<FormularioRegistroState> = _registroState.asStateFlow()

    private val _recuperarState = MutableStateFlow(FormularioRecuperarState())
    val recuperarState: StateFlow<FormularioRecuperarState> = _recuperarState.asStateFlow()

    private val _cambiarClaveState = MutableStateFlow(FormularioCambiarClaveState())
    val cambiarClaveState: StateFlow<FormularioCambiarClaveState> = _cambiarClaveState.asStateFlow()

    fun login(username: String, contrasena: String) {
        _loginUsernameError.value = if (username.isBlank()) "Ingresa tu usuario" else null
        _loginContrasenaError.value = if (contrasena.isBlank()) "Ingresa tu contraseña" else null

        if (_loginUsernameError.value != null || _loginContrasenaError.value != null) {
            _mensajeError.value = "Completa los campos obligatorios"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null
            try {
                _usuarioActual.value = repository.iniciarSesion(username.trim().lowercase(), contrasena)
            } catch (e: HttpException) {
                _mensajeError.value = if (e.code() == 400 || e.code() == 401 || e.code() == 404) {
                    "Usuario o contraseña incorrectos. Verifica tus credenciales."
                } else {
                    "El backend respondió con HTTP ${e.code()} al iniciar sesión."
                }
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo conectar con el backend. Revisa que esté iniciado y la URL de la API."
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta de inicio de sesión inválida."
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    fun registrar(
        username: String,
        nombre: String,
        contrasena: String,
        confirmacion: String,
        rol: Rol,
        fotoBytes: ByteArray? = null,
        preguntaSeguridad: String,
        respuestaSeguridad: String,
        campoRol1: String = "",
        campoRol2: String = "",
        onExito: () -> Unit = {}
    ) {
        _mensajeError.value = null
        val uErr = if (username.trim().length < 3) "El usuario debe tener al menos 3 caracteres" else null
        val nErr = if (nombre.trim().isBlank()) "Ingresa tu nombre completo" else null
        val pErr = if (preguntaSeguridad.isBlank()) "Selecciona una pregunta de seguridad" else null
        val rErr = if (respuestaSeguridad.trim().isBlank()) "Ingresa la respuesta de seguridad" else null

        val evalPass = ValidadorContrasena.evaluar(contrasena, confirmacion)
        val cErr = if (!evalPass.sinEspacios || !evalPass.tieneLongitudMinima || !evalPass.tieneMayuscula ||
            !evalPass.tieneMinuscula || !evalPass.tieneNumero || !evalPass.tieneSimbolo) {
            ValidadorContrasena.obtenerMensajeError(contrasena, confirmacion) ?: "La contraseña no cumple las 5+ reglas"
        } else null

        val confErr = if (contrasena != confirmacion) "Las contraseñas no coinciden" else null

        val campoRolErr = when (rol) {
            Rol.TECNICO -> if (campoRol1.isBlank()) "Indica tu certificación SEC" else null
            Rol.INGENIERO -> if (campoRol1.isBlank()) "Indica tu RUT o N° de Colegiatura" else null
            Rol.JEFE -> if (campoRol1.isBlank()) "Indica tu departamento o sucursal" else null
            Rol.CLIENTE -> if (campoRol1.isBlank()) "Indica el nombre de la empresa" else null
        }

        val nuevoState = FormularioRegistroState(
            usernameError = uErr,
            nombreError = nErr,
            contrasenaError = cErr,
            confirmacionError = confErr,
            preguntaError = pErr,
            respuestaError = rErr,
            campoRolError = campoRolErr
        )
        _registroState.value = nuevoState

        if (nuevoState.tieneErrores) {
            _mensajeError.value = "Por favor corrige los errores marcados en el formulario"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            val userClean = username.trim().lowercase()
            val nomClean = nombre.trim()

            var secCert: String? = null
            var espTec: String? = null
            var rutCol: String? = null
            var regProf: String? = null
            var depto: String? = null
            var codAut: String? = null
            var empNom: String? = null
            var empRut: String? = null
            var empDir: String? = null
            var empTel: String? = null

            when (rol) {
                Rol.TECNICO -> { secCert = campoRol1.trim(); espTec = campoRol2.trim() }
                Rol.INGENIERO -> { rutCol = campoRol1.trim(); regProf = campoRol2.trim() }
                Rol.JEFE -> { depto = campoRol1.trim(); codAut = campoRol2.trim() }
                Rol.CLIENTE -> { empNom = campoRol1.trim(); empRut = campoRol2.trim() }
            }

            try {
                val usuario = repository.registrarUsuario(
                    RegistroRequestDto(
                        username = userClean,
                        nombre = nomClean,
                        contrasena = contrasena,
                        rol = rol.name,
                        preguntaSeguridad = preguntaSeguridad,
                        respuestaSeguridad = respuestaSeguridad,
                        foto = fotoBytes,
                        secCertificacion = secCert,
                        especialidadTecnica = espTec,
                        rutColegiatura = rutCol,
                        registroProfesional = regProf,
                        departamentoSucursal = depto,
                        codigoAutorizacion = codAut,
                        empresaNombre = empNom,
                        rutEmpresa = empRut,
                        direccionEmpresa = empDir,
                        telefonoContacto = empTel
                    )
                )
                _usuarioActual.value = usuario
                onExito()
            } catch (e: HttpException) {
                _mensajeError.value = if (e.code() == 409) {
                    "El usuario '$userClean' ya está registrado. Elige otro nombre de usuario."
                } else {
                    "No se pudo registrar el usuario: error HTTP ${e.code()}."
                }
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo registrar el usuario porque no hay conexión con el backend."
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta de registro inválida."
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    fun recuperarClave(
        username: String,
        respuestaSeguridad: String,
        nuevaContrasena: String,
        confirmacion: String,
        onExito: () -> Unit
    ) {
        val uErr = if (username.isBlank()) "Ingresa tu usuario" else null
        val rErr = if (respuestaSeguridad.isBlank()) "Ingresa tu respuesta de seguridad" else null
        val passEval = ValidadorContrasena.evaluar(nuevaContrasena, confirmacion)
        val nErr = if (!passEval.esValida) ValidadorContrasena.obtenerMensajeError(nuevaContrasena, confirmacion) else null
        val cErr = if (nuevaContrasena != confirmacion) "Las contraseñas no coinciden" else null

        _recuperarState.value = FormularioRecuperarState(
            usernameError = uErr,
            respuestaError = rErr,
            nuevaContrasenaError = nErr,
            confirmacionError = cErr
        )

        if (uErr != null || rErr != null || nErr != null || cErr != null) {
            _mensajeError.value = "Corrige los errores del formulario"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null
            try {
                val ok = repository.recuperarContrasena(username, respuestaSeguridad, nuevaContrasena)
                if (ok) {
                    _mensajeExito.value = "Contraseña restablecida exitosamente. Inicia sesión con tu nueva clave."
                    onExito()
                } else {
                    _mensajeError.value = "Usuario o respuesta de seguridad incorrecta."
                }
            } catch (e: HttpException) {
                _mensajeError.value = "No se pudo recuperar la contraseña: error HTTP ${e.code()}."
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo conectar con el backend para recuperar la contraseña."
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta inválida al recuperar la contraseña."
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cambiarClave(
        contrasenaActual: String,
        nuevaContrasena: String,
        confirmacion: String,
        onExito: () -> Unit
    ) {
        val user = _usuarioActual.value ?: return
        val actErr = if (contrasenaActual.isBlank()) "Ingresa tu contraseña actual" else null
        val passEval = ValidadorContrasena.evaluar(nuevaContrasena, confirmacion)
        val nErr = if (!passEval.esValida) ValidadorContrasena.obtenerMensajeError(nuevaContrasena, confirmacion) else null
        val cErr = if (nuevaContrasena != confirmacion) "Las contraseñas no coinciden" else null

        _cambiarClaveState.value = FormularioCambiarClaveState(
            actualError = actErr,
            nuevaError = nErr,
            confirmacionError = cErr
        )

        if (actErr != null || nErr != null || cErr != null) {
            _mensajeError.value = "Corrige los errores en el formulario"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null
            try {
                val ok = repository.cambiarContrasena(user.id, contrasenaActual, nuevaContrasena)
                if (ok) {
                    _mensajeExito.value = "Contraseña cambiada exitosamente."
                    onExito()
                } else {
                    _mensajeError.value = "La contraseña actual es incorrecta."
                }
            } catch (e: HttpException) {
                _mensajeError.value = "No se pudo cambiar la contraseña: error HTTP ${e.code()}."
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo conectar con el backend para cambiar la contraseña."
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta inválida al cambiar la contraseña."
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    fun modificarPerfil(
        nombre: String,
        fotoBytes: ByteArray?,
        campoRol1: String,
        campoRol2: String,
        onExito: () -> Unit
    ) {
        val user = _usuarioActual.value ?: return
        if (nombre.isBlank()) {
            _mensajeError.value = "El nombre no puede estar vacío"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            val nomClean = nombre.trim()
            var secCert = user.secCertificacion
            var espTec = user.especialidadTecnica
            var rutCol = user.rutColegiatura
            var regProf = user.registroProfesional
            var depto = user.departamentoSucursal
            var codAut = user.codigoAutorizacion
            var empNom = user.empresaNombre
            var empRut = user.rutEmpresa

            when (user.rol) {
                Rol.TECNICO -> { secCert = campoRol1.trim(); espTec = campoRol2.trim() }
                Rol.INGENIERO -> { rutCol = campoRol1.trim(); regProf = campoRol2.trim() }
                Rol.JEFE -> { depto = campoRol1.trim(); codAut = campoRol2.trim() }
                Rol.CLIENTE -> { empNom = campoRol1.trim(); empRut = campoRol2.trim() }
            }

            val actualizado = user.copy(
                nombre = nomClean,
                fotoBytes = fotoBytes ?: user.fotoBytes,
                secCertificacion = secCert,
                especialidadTecnica = espTec,
                rutColegiatura = rutCol,
                registroProfesional = regProf,
                departamentoSucursal = depto,
                codigoAutorizacion = codAut,
                empresaNombre = empNom,
                rutEmpresa = empRut
            )

            try {
                val ok = repository.actualizarPerfil(actualizado)
                if (ok) {
                    _usuarioActual.value = actualizado
                    _mensajeExito.value = "Perfil actualizado correctamente"
                    onExito()
                } else {
                    _mensajeError.value = "No se pudo actualizar el perfil"
                }
            } catch (e: HttpException) {
                _mensajeError.value = "No se pudo actualizar el perfil: error HTTP ${e.code()}."
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo conectar con el backend para actualizar el perfil."
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta inválida al actualizar el perfil."
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
            } finally {
                _cargando.value = false
            }
        }
    }

    fun logout() {
        _usuarioActual.value = null
        _mensajeError.value = null
        _mensajeExito.value = null
        limpiarErroresFormulario()
    }

    fun limpiarError() {
        _mensajeError.value = null
        _mensajeExito.value = null
        _loginUsernameError.value = null
        _loginContrasenaError.value = null
    }

    fun limpiarErroresFormulario() {
        _registroState.value = FormularioRegistroState()
        _recuperarState.value = FormularioRecuperarState()
        _cambiarClaveState.value = FormularioCambiarClaveState()
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(application) as T
        }
    }
}
