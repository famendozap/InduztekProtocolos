package com.induztek.protocolos.model

import java.time.LocalDateTime

/**
 * Protocolo de una prueba eléctrica realizada en terreno sobre un Equipo.
 * Es el "respaldo inmediato" que hoy el cliente no recibe (sección 2.3 del caso).
 *
 * @param equipoId Equipo sobre el que se realizó la prueba.
 * @param fechaHora Momento en que se registró la prueba en terreno.
 * @param mediciones Resultados y valores de medición de la prueba.
 * @param estado Estado del protocolo: registrado en terreno o formalizado.
 * @param observaciones Notas libres del técnico (opcional).
 */
data class Protocolo(
    val id: Long = 0,
    val equipoId: Long,
    val fechaHora: LocalDateTime,
    val mediciones: List<Medicion>,
    val estado: EstadoProtocolo,
    val observaciones: String = ""
)
