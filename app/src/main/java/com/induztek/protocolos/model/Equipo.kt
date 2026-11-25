package com.induztek.protocolos.model

data class Equipo(
    val id: Long = 0,
    val codigo: String,
    val tipo: TipoEquipo,
    val instalacionCliente: String,
    val marca: String = "Genérica",
    val modelo: String = "Estándar",
    val numeroSerie: String = "N/A",
    val potenciaKVA: Double? = null,
    val tensionNominalKV: Double? = null,
    val clienteId: Long? = null,
    val clienteNombre: String? = null,
    val ubicacionGPS: String? = null
)
