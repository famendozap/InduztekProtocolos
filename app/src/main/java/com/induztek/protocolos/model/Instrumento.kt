package com.induztek.protocolos.model

import java.time.LocalDate

/**
 * Instrumento de prueba utilizado para realizar las mediciones (ej. Megómetros, Analizadores TTR).
 * Permite controlar las fechas de calibración exigidas por la norma.
 */
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
