package com.induztek.protocolos.model

import java.time.LocalDate

data class Instrumento(
    val id: Long = 0,
    val nombre: String,
    val numeroSerie: String,
    val fechaCalibracion: LocalDate,
    val fechaVencimientoCalibracion: LocalDate
) {
    val estaVencido: Boolean
        get() = LocalDate.now().isAfter(fechaVencimientoCalibracion)
}
