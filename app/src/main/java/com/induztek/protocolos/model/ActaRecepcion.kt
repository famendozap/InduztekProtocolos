package com.induztek.protocolos.model

import java.time.LocalDateTime

/**
 * Acta de recepción que entrega respaldo inmediato al cliente tras la visita/prueba técnica.
 */
data class ActaRecepcion(
    val id: Long = 0,
    val visitaId: Long,
    val cliente: String,
    val tecnico: String,
    val fechaEmision: LocalDateTime = LocalDateTime.now(),
    val detallesTrabajo: String,
    val recibidaConConformidad: Boolean = true
)
