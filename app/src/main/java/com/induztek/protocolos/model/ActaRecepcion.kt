package com.induztek.protocolos.model

import java.time.LocalDateTime

data class ActaRecepcion(
    val id: Long = 0,
    val visitaId: Long,
    val cliente: String,
    val tecnico: String,
    val fechaEmision: LocalDateTime = LocalDateTime.now(),
    val detallesTrabajo: String,
    val recibidaConConformidad: Boolean = true
)
