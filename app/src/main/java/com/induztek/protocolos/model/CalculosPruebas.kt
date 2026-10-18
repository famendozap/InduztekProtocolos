package com.induztek.protocolos.model

import java.util.Locale

object CalculosPruebas {

    /**
     * Calcula el Índice de Polarización (IP): IP = R10min / R1min
     * @return Par con el valor del IP y la evaluación del estado
     */
    fun calcularIndicePolarizacion(r1min: Double, r10min: Double): Pair<Double, String> {
        if (r1min <= 0) return 0.0 to "Inválido"
        val ip = r10min / r1min
        val evaluacion = when {
            ip < 1.5 -> "Peligroso (Aislamiento deficiente)"
            ip in 1.5..2.0 -> "Dudoso"
            ip in 2.0..4.0 -> "Bueno"
            else -> "Excelente"
        }
        return ip to evaluacion
    }

    /**
     * Calcula el TTR (Relación de Transformación) y la variación % respecto al valor esperado (placa).
     * Alerta si la variación supera la tolerancia del 25%.
     */
    fun calcularTtr(vPrimario: Double, vSecundario: Double, vPlacaEsperado: Double): ResultadoTtr {
        if (vSecundario <= 0 || vPlacaEsperado <= 0) {
            return ResultadoTtr(0.0, 0.0, true, "Valores de voltaje no válidos")
        }
        val ttrMedido = vPrimario / vSecundario
        val variacionPorcentaje = (Math.abs(ttrMedido - vPlacaEsperado) / vPlacaEsperado) * 100.0
        val superaTolerancia = variacionPorcentaje > 25.0
        val mensaje = if (superaTolerancia) {
            String.format(Locale.US, "¡ALERTA! Variación del %.2f%% supera la tolerancia del 25%%", variacionPorcentaje)
        } else {
            String.format(Locale.US, "Variación del %.2f%% dentro de rango tolerado", variacionPorcentaje)
        }
        return ResultadoTtr(ttrMedido, variacionPorcentaje, superaTolerancia, mensaje)
    }

    data class ResultadoTtr(
        val ttrMedido: Double,
        val variacionPorcentaje: Double,
        val superaTolerancia: Boolean,
        val mensaje: String
    )
}
