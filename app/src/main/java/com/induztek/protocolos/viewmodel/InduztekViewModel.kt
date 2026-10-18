package com.induztek.protocolos.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.induztek.protocolos.data.local.AppDatabase
import com.induztek.protocolos.data.repository.InduztekRepository
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.Medicion
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.model.TipoEquipo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel único de la app: mantiene la lista de equipos, el historial del
 * equipo seleccionado y las operaciones de creación (equipo y protocolo).
 * Según crezca el proyecto conviene separarlo en varios ViewModels por pantalla.
 */
class InduztekViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InduztekRepository

    init {
        val dbHelper = com.induztek.protocolos.data.local.InduztekDbHelper(application)
        repository = InduztekRepository(dbHelper)
    }

    val equipos: StateFlow<List<Equipo>> =
        repository.observarEquipos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todosLosProtocolos: StateFlow<List<Protocolo>> =
        repository.observarTodosLosProtocolos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _equipoSeleccionadoId = MutableStateFlow<Long?>(null)
    val equipoSeleccionadoId: StateFlow<Long?> = _equipoSeleccionadoId

    val historialEquipoSeleccionado: StateFlow<List<Protocolo>> =
        _equipoSeleccionadoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observarHistorialPorEquipo(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError

    fun seleccionarEquipo(equipoId: Long) {
        _equipoSeleccionadoId.value = equipoId
    }

    fun limpiarError() {
        _mensajeError.value = null
    }

    fun crearEquipo(codigo: String, tipo: TipoEquipo, instalacionCliente: String, onCreado: (Long) -> Unit) {
        val codigoLimpio = codigo.trim().uppercase()
        if (codigoLimpio.isBlank()) {
            _mensajeError.value = "El código del equipo no puede estar vacío"
            return
        }
        if (instalacionCliente.isBlank()) {
            _mensajeError.value = "Indica la instalación o ubicación del cliente"
            return
        }
        val instalacionLimpia = instalacionCliente.trim()
        viewModelScope.launch {
            // RN-01: el TAG es único dentro de cada instalación, no globalmente.
            val existente = repository.buscarEquipo(codigoLimpio, instalacionLimpia)
            if (existente != null) {
                _mensajeError.value = "Ya existe el equipo $codigoLimpio en $instalacionLimpia"
                return@launch
            }
            val id = try {
                repository.guardarEquipo(
                    Equipo(codigo = codigoLimpio, tipo = tipo, instalacionCliente = instalacionLimpia)
                )
            } catch (e: SQLiteConstraintException) {
                _mensajeError.value = "Ya existe el equipo $codigoLimpio en $instalacionLimpia"
                return@launch
            }
            onCreado(id)
        }
    }

    fun registrarProtocolo(
        equipoId: Long,
        mediciones: List<Medicion>,
        observaciones: String,
        onRegistrado: () -> Unit
    ) {
        if (mediciones.isEmpty()) {
            _mensajeError.value = "Agrega al menos una medición antes de guardar"
            return
        }
        viewModelScope.launch {
            repository.registrarProtocolo(
                Protocolo(
                    equipoId = equipoId,
                    fechaHora = LocalDateTime.now(),
                    mediciones = mediciones,
                    // RN-04: el técnico solo registra en terreno; formalizar es del ingeniero/jefe
                    // (requiere roles, aún no implementados).
                    estado = EstadoProtocolo.REGISTRADO_EN_TERRENO,
                    observaciones = observaciones.trim()
                )
            )
            onRegistrado()
        }
    }

    fun formalizarProtocolo(protocolo: Protocolo) {
        viewModelScope.launch {
            repository.actualizarProtocolo(protocolo.copy(estado = EstadoProtocolo.FORMALIZADO))
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
