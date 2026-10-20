package com.induztek.protocolos.model

import java.time.LocalDateTime

/**
 * Protocolo de una prueba eléctrica realizada en terreno sobre un Equipo.
 *
 * @param equipoId Equipo sobre el que se realizó la prueba.
 * @param fechaHora Momento en que se registró la prueba en terreno.
 * @param mediciones Resultados y valores de medición de la prueba.
 * @param estado Estado del protocolo: registrado en terreno o formalizado.
 * @param observaciones Notas libres del técnico (opcional).
 * @param fotoEvidenciaBytes Foto de evidencia en terreno tomada con la cámara.
 */
data class Protocolo(
    val id: Long = 0,
    val equipoId: Long,
    val fechaHora: LocalDateTime,
    val mediciones: List<Medicion>,
    val estado: EstadoProtocolo,
    val observaciones: String = "",
    val fotoEvidenciaBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Protocolo
        return id == other.id && equipoId == other.equipoId && estado == other.estado
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + equipoId.hashCode()
        result = 31 * result + estado.hashCode()
        return result
    }
}
