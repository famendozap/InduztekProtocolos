package com.induztek.protocolos.model

data class Equipo(
    val id: Long = 0,
    val codigo: String,
    val tipo: TipoEquipo,
    val instalacionCliente: String
)
