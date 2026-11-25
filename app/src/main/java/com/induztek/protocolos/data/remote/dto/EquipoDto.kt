package com.induztek.protocolos.data.remote.dto

import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.TipoEquipo

data class EquipoRequestDto(
    val codigo: String,
    val tipo: TipoEquipo,
    val instalacionCliente: String,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val potenciaKVA: Double?,
    val tensionNominalKV: Double?,
    val clienteId: Long?,
    val clienteNombre: String?,
    val ubicacionGPS: String?
)

data class EquipoResponseDto(
    val id: Long,
    val codigo: String,
    val tipo: TipoEquipo,
    val instalacionCliente: String,
    val marca: String?,
    val modelo: String?,
    val numeroSerie: String?,
    val potenciaKVA: Double?,
    val tensionNominalKV: Double?,
    val clienteId: Long?,
    val clienteNombre: String?,
    val ubicacionGPS: String?
) {
    fun toEquipo() = Equipo(
        id = id,
        codigo = codigo,
        tipo = tipo,
        instalacionCliente = instalacionCliente,
        marca = marca ?: "Genérica",
        modelo = modelo ?: "Estándar",
        numeroSerie = numeroSerie ?: "N/A",
        potenciaKVA = potenciaKVA,
        tensionNominalKV = tensionNominalKV,
        clienteId = clienteId,
        clienteNombre = clienteNombre,
        ubicacionGPS = ubicacionGPS
    )
}

fun Equipo.toRequestDto() = EquipoRequestDto(
    codigo = codigo,
    tipo = tipo,
    instalacionCliente = instalacionCliente,
    marca = marca,
    modelo = modelo,
    numeroSerie = numeroSerie,
    potenciaKVA = potenciaKVA,
    tensionNominalKV = tensionNominalKV,
    clienteId = clienteId,
    clienteNombre = clienteNombre,
    ubicacionGPS = ubicacionGPS
)
