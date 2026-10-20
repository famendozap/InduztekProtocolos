package com.induztek.protocolos.model

import java.time.LocalDateTime

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
