package com.induztek.protocolos.data.repository

import com.induztek.protocolos.data.local.InduztekDbHelper
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.Protocolo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class InduztekRepository(
    private val dbHelper: InduztekDbHelper
) {
    private val _equiposFlow = MutableStateFlow<List<Equipo>>(emptyList())
    val equiposFlow: Flow<List<Equipo>> = _equiposFlow.asStateFlow()

    private val _historialFlow = MutableStateFlow<List<Protocolo>>(emptyList())
    val historialFlow: Flow<List<Protocolo>> = _historialFlow.asStateFlow()

    private val _todosProtocolosFlow = MutableStateFlow<List<Protocolo>>(emptyList())
    val todosProtocolosFlow: Flow<List<Protocolo>> = _todosProtocolosFlow.asStateFlow()

    private suspend fun refrescarEquipos() = withContext(Dispatchers.IO) {
        _equiposFlow.value = dbHelper.listarEquipos()
    }

    private suspend fun refrescarHistorial(equipoId: Long) = withContext(Dispatchers.IO) {
        _historialFlow.value = dbHelper.listarProtocolosPorEquipo(equipoId)
    }

    private suspend fun refrescarTodosProtocolos() = withContext(Dispatchers.IO) {
        _todosProtocolosFlow.value = dbHelper.listarTodosLosProtocolos()
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

    suspend fun buscarEquipo(codigo: String, instalacion: String): Equipo? = withContext(Dispatchers.IO) {
        dbHelper.buscarEquipo(codigo, instalacion)
    }

    suspend fun obtenerEquipo(equipoId: Long): Equipo? = withContext(Dispatchers.IO) {
        dbHelper.obtenerEquipoPorId(equipoId)
    }

    suspend fun guardarEquipo(equipo: Equipo): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertarEquipo(equipo)
        refrescarEquipos()
        id
    }

    suspend fun registrarProtocolo(protocolo: Protocolo): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertarProtocolo(protocolo)
        refrescarHistorial(protocolo.equipoId)
        refrescarTodosProtocolos()
        id
    }

    suspend fun actualizarProtocolo(protocolo: Protocolo) = withContext(Dispatchers.IO) {
        dbHelper.actualizarProtocolo(protocolo)
        refrescarHistorial(protocolo.equipoId)
        refrescarTodosProtocolos()
    }
}
