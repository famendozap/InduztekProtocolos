package com.induztek.protocolos.data.remote.dto

import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Medicion
import com.induztek.protocolos.model.Protocolo
import java.time.LocalDateTime

data class ProtocoloRequestDto(
    val equipoId: Long,
    val fechaHora: String,
    val mediciones: List<MedicionDto>,
    val observaciones: String?,
    val fotoEvidenciaBytes: ByteArray?,
    val tecnicoNombre: String,
    val temperaturaAmbiente: Double?,
    val humedadRelativa: Double?,
    val instrumentosUsados: String?,
    val resultadoGeneral: String?,
    val ubicacionGPS: String?
)

data class ProtocoloResponseDto(
    val id: Long,
    val equipoId: Long,
    val fechaHora: String,
    val mediciones: List<MedicionDto>,
    val estado: EstadoProtocolo,
    val observaciones: String?,
    val fotoEvidenciaBytes: ByteArray?,
    val tecnicoNombre: String,
    val ingenieroNombre: String?,
    val temperaturaAmbiente: Double?,
    val humedadRelativa: Double?,
    val instrumentosUsados: String?,
    val resultadoGeneral: String?,
    val ubicacionGPS: String?
) {
    fun toProtocolo() = Protocolo(
        id = id,
        equipoId = equipoId,
        fechaHora = LocalDateTime.parse(fechaHora),
        mediciones = mediciones.map { it.toMedicion() },
        estado = estado,
        observaciones = observaciones.orEmpty(),
        fotoEvidenciaBytes = fotoEvidenciaBytes,
        tecnicoNombre = tecnicoNombre,
        ingenieroNombre = ingenieroNombre,
        temperaturaAmbiente = temperaturaAmbiente,
        humedadRelativa = humedadRelativa,
        instrumentosUsados = instrumentosUsados,
        resultadoGeneral = resultadoGeneral ?: "CONFORME",
        ubicacionGPS = ubicacionGPS
    )
}

fun Protocolo.toRequestDto() = ProtocoloRequestDto(
    equipoId = equipoId,
    fechaHora = fechaHora.toString(),
    mediciones = mediciones.map { MedicionDto(it.nombre, it.valor, it.unidad) },
    observaciones = observaciones,
    fotoEvidenciaBytes = fotoEvidenciaBytes,
    tecnicoNombre = tecnicoNombre,
    temperaturaAmbiente = temperaturaAmbiente,
    humedadRelativa = humedadRelativa,
    instrumentosUsados = instrumentosUsados,
    resultadoGeneral = resultadoGeneral,
    ubicacionGPS = ubicacionGPS
)

data class FormalizarRequestDto(
    val ingenieroNombre: String
)

data class MedicionDto(
    val nombre: String,
    val valor: String,
    val unidad: String?
) {
    fun toMedicion() = Medicion(
        nombre = nombre,
        valor = valor,
        unidad = unidad.orEmpty()
    )
}
