package com.induztek.protocolos.data.repository

import com.induztek.protocolos.data.local.InduztekDbHelper
import com.induztek.protocolos.data.remote.InduztekApiService
import com.induztek.protocolos.data.remote.RetrofitClient
import com.induztek.protocolos.data.remote.requireBody
import com.induztek.protocolos.data.remote.dto.CambiarContrasenaRequestDto
import com.induztek.protocolos.data.remote.dto.LoginRequestDto
import com.induztek.protocolos.data.remote.dto.PerfilRequestDto
import com.induztek.protocolos.data.remote.dto.RecuperarRequestDto
import com.induztek.protocolos.data.remote.dto.RegistroRequestDto
import com.induztek.protocolos.data.remote.dto.toRequestDto
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class InduztekRepository(
    private val dbHelper: InduztekDbHelper,
    private val api: InduztekApiService = RetrofitClient.apiService
) {
    private val _equiposFlow = MutableStateFlow<List<Equipo>>(emptyList())
    val equiposFlow: Flow<List<Equipo>> = _equiposFlow.asStateFlow()

    private val _historialFlow = MutableStateFlow<List<Protocolo>>(emptyList())
    val historialFlow: Flow<List<Protocolo>> = _historialFlow.asStateFlow()

    private val _todosProtocolosFlow = MutableStateFlow<List<Protocolo>>(emptyList())
    val todosProtocolosFlow: Flow<List<Protocolo>> = _todosProtocolosFlow.asStateFlow()

    private val _clientesFlow = MutableStateFlow<List<Usuario>>(emptyList())
    val clientesFlow: Flow<List<Usuario>> = _clientesFlow.asStateFlow()

    private suspend fun refrescarEquipos() {
        _equiposFlow.value = withContext(Dispatchers.IO) {
            api.obtenerEquipos().requireBody().map { it.toEquipo() }
        }
    }

    private suspend fun refrescarHistorial(equipoId: Long) {
        _historialFlow.value = withContext(Dispatchers.IO) {
            api.obtenerProtocolosPorEquipo(equipoId).requireBody().map { it.toProtocolo() }
        }
    }

    private suspend fun refrescarTodosProtocolos() {
        _todosProtocolosFlow.value = withContext(Dispatchers.IO) {
            api.obtenerProtocolos().requireBody().map { it.toProtocolo() }
        }
    }

    suspend fun refrescarClientes() {
        _clientesFlow.value = withContext(Dispatchers.IO) {
            api.obtenerUsuarios(Rol.CLIENTE.name).requireBody().map { it.toUsuario() }
        }
    }

    suspend fun refrescarDatosDesdeServidor() {
        refrescarEquipos()
        refrescarTodosProtocolos()
        refrescarClientes()
    }

    suspend fun cargarHistorialEquipo(equipoId: Long) {
        refrescarHistorial(equipoId)
    }

    suspend fun iniciarSesion(username: String, contrasena: String): Usuario = withContext(Dispatchers.IO) {
        val login = api.login(LoginRequestDto(username, contrasena)).requireBody()
        api.obtenerUsuario(login.id).requireBody().toUsuario().copy(token = login.token)
    }

    suspend fun registrarUsuario(request: RegistroRequestDto): Usuario = withContext(Dispatchers.IO) {
        api.registrarUsuario(request).requireBody().toUsuario()
    }

    fun observarEquipos(): Flow<List<Equipo>> {
        _equiposFlow.value = dbHelper.listarEquipos()
        return equiposFlow
    }

    fun observarHistorialPorEquipo(equipoId: Long): Flow<List<Protocolo>> {
        _historialFlow.value = dbHelper.listarProtocolosPorEquipo(equipoId)
        return historialFlow
    }

    fun observarTodosLosProtocolos(): Flow<List<Protocolo>> {
        _todosProtocolosFlow.value = dbHelper.listarTodosLosProtocolos()
        return todosProtocolosFlow
    }

    fun observarClientes(): Flow<List<Usuario>> {
        _clientesFlow.value = dbHelper.obtenerClientes()
        return clientesFlow
    }

    suspend fun buscarEquipo(codigo: String, instalacion: String): Equipo? = withContext(Dispatchers.IO) {
        _equiposFlow.value.firstOrNull {
            it.codigo.equals(codigo, ignoreCase = true) &&
                it.instalacionCliente.equals(instalacion, ignoreCase = true)
        } ?: dbHelper.buscarEquipo(codigo, instalacion)
    }

    suspend fun obtenerEquipo(equipoId: Long): Equipo? = withContext(Dispatchers.IO) {
        _equiposFlow.value.firstOrNull { it.id == equipoId } ?: dbHelper.obtenerEquipoPorId(equipoId)
    }

    suspend fun guardarEquipo(equipo: Equipo): Long = withContext(Dispatchers.IO) {
        val creado = api.crearEquipo(equipo.toRequestDto()).requireBody().toEquipo()
        _equiposFlow.value = _equiposFlow.value.filterNot { it.id == creado.id } + creado
        creado.id
    }

    suspend fun registrarProtocolo(protocolo: Protocolo): Long = withContext(Dispatchers.IO) {
        val creado = api.crearProtocolo(protocolo.toRequestDto()).requireBody().toProtocolo()
        _historialFlow.value = _historialFlow.value.filterNot { it.id == creado.id } + creado
        _todosProtocolosFlow.value = _todosProtocolosFlow.value.filterNot { it.id == creado.id } + creado
        creado.id
    }

    suspend fun actualizarProtocolo(protocolo: Protocolo) {
        val response = withContext(Dispatchers.IO) {
            api.formalizarProtocolo(
                protocolo.id,
                com.induztek.protocolos.data.remote.dto.FormalizarRequestDto(
                    protocolo.ingenieroNombre.orEmpty()
                )
            ).requireBody().toProtocolo()
        }
        _historialFlow.value = _historialFlow.value.map { if (it.id == response.id) response else it }
        _todosProtocolosFlow.value = _todosProtocolosFlow.value.map { if (it.id == response.id) response else it }
    }

    suspend fun recuperarContrasena(username: String, respuestaSeguridad: String, nuevaContrasena: String): Boolean = withContext(Dispatchers.IO) {
        val response = api.recuperarContrasena(
            RecuperarRequestDto(username, respuestaSeguridad, nuevaContrasena)
        )
        if (!response.isSuccessful && response.code() != 400 && response.code() != 404) {
            throw HttpException(response)
        }
        response.isSuccessful
    }

    suspend fun cambiarContrasena(userId: Long, contrasenaActual: String, nuevaContrasena: String): Boolean = withContext(Dispatchers.IO) {
        val response = api.cambiarContrasena(
            userId,
            CambiarContrasenaRequestDto(contrasenaActual, nuevaContrasena)
        )
        if (!response.isSuccessful && response.code() != 400 && response.code() != 404) {
            throw HttpException(response)
        }
        response.isSuccessful
    }

    suspend fun actualizarPerfil(usuario: Usuario): Boolean = withContext(Dispatchers.IO) {
        val response = api.actualizarPerfil(
            usuario.id,
            PerfilRequestDto(
                nombre = usuario.nombre,
                foto = usuario.fotoBytes,
                secCertificacion = usuario.secCertificacion,
                especialidadTecnica = usuario.especialidadTecnica,
                rutColegiatura = usuario.rutColegiatura,
                registroProfesional = usuario.registroProfesional,
                departamentoSucursal = usuario.departamentoSucursal,
                codigoAutorizacion = usuario.codigoAutorizacion,
                empresaNombre = usuario.empresaNombre,
                rutEmpresa = usuario.rutEmpresa,
                direccionEmpresa = usuario.direccionEmpresa,
                telefonoContacto = usuario.telefonoContacto
            )
        )
        if (!response.isSuccessful) {
            if (response.code() != 400 && response.code() != 404) throw HttpException(response)
            return@withContext false
        }
        val updated = response.requireBody().toUsuario()
        _clientesFlow.value = _clientesFlow.value.map { if (it.id == updated.id) updated else it }
        true
    }

    suspend fun obtenerUsuarioPorUsername(username: String): Usuario? = withContext(Dispatchers.IO) {
        dbHelper.obtenerUsuarioPorUsername(username)
    }
}
