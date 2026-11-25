package com.induztek.protocolos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.data.local.InduztekDbHelper
import com.induztek.protocolos.data.remote.EmptyApiResponseException
import com.induztek.protocolos.data.repository.InduztekRepository
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Medicion
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.model.TipoEquipo
import com.induztek.protocolos.model.Usuario
import com.induztek.protocolos.util.PermisosRol
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

data class FormularioEquipoState(
    val codigoError: String? = null,
    val instalacionError: String? = null,
    val marcaError: String? = null,
    val modeloError: String? = null
)

data class FormularioProtocoloState(
    val medicionesError: String? = null,
    val observacionesError: String? = null,
    val instrumentosError: String? = null
)

class InduztekViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InduztekRepository

    init {
        val dbHelper = InduztekDbHelper(application)
        repository = InduztekRepository(dbHelper)
    }

    val equipos: StateFlow<List<Equipo>> =
        repository.observarEquipos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todosLosProtocolos: StateFlow<List<Protocolo>> =
        repository.observarTodosLosProtocolos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val clientes: StateFlow<List<Usuario>> =
        repository.observarClientes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _equipoSeleccionadoId = MutableStateFlow<Long?>(null)
    val equipoSeleccionadoId: StateFlow<Long?> = _equipoSeleccionadoId

    val historialEquipoSeleccionado: StateFlow<List<Protocolo>> =
        _equipoSeleccionadoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observarHistorialPorEquipo(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()

    private val _equipoFormState = MutableStateFlow(FormularioEquipoState())
    val equipoFormState: StateFlow<FormularioEquipoState> = _equipoFormState.asStateFlow()

    private val _protocoloFormState = MutableStateFlow(FormularioProtocoloState())
    val protocoloFormState: StateFlow<FormularioProtocoloState> = _protocoloFormState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.refrescarDatosDesdeServidor()
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo conectar con el backend. Revisa que esté iniciado y la URL de la API."
            } catch (e: HttpException) {
                _mensajeError.value = "El backend respondió con HTTP ${e.code()} al cargar los datos."
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió datos que la app no pudo interpretar."
            } catch (e: DateTimeParseException) {
                _mensajeError.value = "El backend devolvió una fecha de protocolo con formato inválido."
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
            }
        }
    }

    fun seleccionarEquipo(equipoId: Long) {
        _equipoSeleccionadoId.value = equipoId
        viewModelScope.launch {
            try {
                repository.cargarHistorialEquipo(equipoId)
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo cargar el historial desde el backend."
            } catch (e: HttpException) {
                _mensajeError.value = "El backend respondió con HTTP ${e.code()} al cargar el historial."
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió un historial con formato inválido."
            } catch (e: DateTimeParseException) {
                _mensajeError.value = "El backend devolvió una fecha de protocolo con formato inválido."
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
            }
        }
    }

    fun limpiarError() {
        _mensajeError.value = null
        _mensajeExito.value = null
        _equipoFormState.value = FormularioEquipoState()
        _protocoloFormState.value = FormularioProtocoloState()
    }

    fun crearEquipo(
        usuarioActual: Usuario?,
        codigo: String,
        tipo: TipoEquipo,
        instalacionCliente: String,
        marca: String,
        modelo: String,
        numeroSerie: String,
        potenciaKVA: Double?,
        tensionNominalKV: Double?,
        clienteSeleccionado: Usuario?,
        ubicacionGPS: String?,
        onCreado: (Long) -> Unit
    ) {
        if (!PermisosRol.puedeCrearEquipo(usuarioActual?.rol)) {
            _mensajeError.value = "Tu rol de Cliente no tiene permisos para crear equipos."
            return
        }

        val codigoLimpio = codigo.trim().uppercase()
        val codErr = if (codigoLimpio.isBlank()) "El código TAG del equipo no puede estar vacío" else null
        val instErr = if (instalacionCliente.isBlank()) "Indica la instalación o dirección del cliente" else null
        val marcaErr = if (marca.isBlank()) "Ingresa la marca del equipo" else null
        val modErr = if (modelo.isBlank()) "Ingresa el modelo del equipo" else null

        _equipoFormState.value = FormularioEquipoState(
            codigoError = codErr,
            instalacionError = instErr,
            marcaError = marcaErr,
            modeloError = modErr
        )

        if (codErr != null || instErr != null || marcaErr != null || modErr != null) {
            _mensajeError.value = "Por favor completa los campos obligatorios del equipo"
            return
        }

        val instalacionLimpia = instalacionCliente.trim()
        viewModelScope.launch {
            val existente = repository.buscarEquipo(codigoLimpio, instalacionLimpia)
            if (existente != null) {
                _mensajeError.value = "Ya existe el equipo $codigoLimpio en $instalacionLimpia"
                return@launch
            }

            val nuevoEquipo = Equipo(
                codigo = codigoLimpio,
                tipo = tipo,
                instalacionCliente = instalacionLimpia,
                marca = marca.trim(),
                modelo = modelo.trim(),
                numeroSerie = if (numeroSerie.isBlank()) "N/A" else numeroSerie.trim(),
                potenciaKVA = potenciaKVA,
                tensionNominalKV = tensionNominalKV,
                clienteId = clienteSeleccionado?.id,
                clienteNombre = clienteSeleccionado?.empresaNombre ?: clienteSeleccionado?.nombre,
                ubicacionGPS = ubicacionGPS
            )

            val id = try {
                repository.guardarEquipo(nuevoEquipo)
            } catch (e: HttpException) {
                _mensajeError.value = if (e.code() == 409) {
                    "Ya existe el equipo $codigoLimpio en $instalacionLimpia"
                } else {
                    "No se pudo guardar el equipo: error HTTP ${e.code()}."
                }
                return@launch
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo guardar el equipo porque no hay conexión con el backend."
                return@launch
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta inválida al guardar el equipo."
                return@launch
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
                return@launch
            }

            _mensajeExito.value = "Equipo $codigoLimpio registrado exitosamente."
            onCreado(id)
        }
    }

    fun registrarProtocolo(
        usuarioActual: Usuario?,
        equipoId: Long,
        mediciones: List<Medicion>,
        observaciones: String,
        fotoEvidenciaBytes: ByteArray? = null,
        temperaturaAmbiente: Double? = null,
        humedadRelativa: Double? = null,
        instrumentosUsados: String? = null,
        resultadoGeneral: String = "CONFORME",
        ubicacionGPS: String? = null,
        onRegistrado: () -> Unit
    ) {
        if (!PermisosRol.puedeCrearProtocolo(usuarioActual?.rol)) {
            _mensajeError.value = "Permiso denegado: El que formaliza no crea protocolos. Esta acción corresponde al Técnico en terreno."
            return
        }

        val medErr = if (mediciones.isEmpty()) "Agrega al menos una medición eléctrica antes de guardar" else null
        _protocoloFormState.value = FormularioProtocoloState(medicionesError = medErr)

        if (medErr != null) {
            _mensajeError.value = medErr
            return
        }

        viewModelScope.launch {
            val nuevoProtocolo = Protocolo(
                equipoId = equipoId,
                fechaHora = LocalDateTime.now(),
                mediciones = mediciones,
                estado = EstadoProtocolo.REGISTRADO_EN_TERRENO,
                observaciones = observaciones.trim(),
                fotoEvidenciaBytes = fotoEvidenciaBytes,
                tecnicoNombre = usuarioActual?.nombre ?: "Técnico de Terreno",
                temperaturaAmbiente = temperaturaAmbiente,
                humedadRelativa = humedadRelativa,
                instrumentosUsados = instrumentosUsados?.trim(),
                resultadoGeneral = resultadoGeneral,
                ubicacionGPS = ubicacionGPS
            )
            try {
                repository.registrarProtocolo(nuevoProtocolo)
            } catch (e: HttpException) {
                _mensajeError.value = "No se pudo registrar el protocolo: error HTTP ${e.code()}."
                return@launch
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo registrar el protocolo porque no hay conexión con el backend."
                return@launch
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta inválida al registrar el protocolo."
                return@launch
            } catch (e: DateTimeParseException) {
                _mensajeError.value = "El backend devolvió una fecha de protocolo con formato inválido."
                return@launch
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
                return@launch
            }
            _mensajeExito.value = "Protocolo registrado exitosamente en terreno."
            onRegistrado()
        }
    }

    fun formalizarProtocolo(usuarioActual: Usuario?, protocolo: Protocolo) {
        if (!PermisosRol.puedeFormalizarProtocolo(usuarioActual?.rol)) {
            _mensajeError.value = "Permiso denegado: Solo el usuario con rol INGENIERO puede formalizar protocolos."
            return
        }

        viewModelScope.launch {
            val formalizado = protocolo.copy(
                estado = EstadoProtocolo.FORMALIZADO,
                ingenieroNombre = usuarioActual?.nombre ?: "Ingeniero de Pruebas"
            )
            try {
                repository.actualizarProtocolo(formalizado)
            } catch (e: HttpException) {
                _mensajeError.value = "No se pudo formalizar el protocolo: error HTTP ${e.code()}."
                return@launch
            } catch (e: IOException) {
                _mensajeError.value = "No se pudo formalizar el protocolo porque no hay conexión con el backend."
                return@launch
            } catch (e: JsonParseException) {
                _mensajeError.value = "El backend devolvió una respuesta inválida al formalizar el protocolo."
                return@launch
            } catch (e: DateTimeParseException) {
                _mensajeError.value = "El backend devolvió una fecha de protocolo con formato inválido."
                return@launch
            } catch (e: EmptyApiResponseException) {
                _mensajeError.value = e.message
                return@launch
            }
            _mensajeExito.value = "Protocolo N° ${protocolo.id} formalizado correctamente por el Ingeniero."
        }
    }

    companion object {
        fun factory(application: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                InduztekViewModel(application) as T
        }
    }
}
