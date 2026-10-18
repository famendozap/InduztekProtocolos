package com.induztek.protocolos.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculosPruebasTest {

    @Test
    fun `calcularIndicePolarizacion con valores normales retorna Bueno`() {
        val r1min = 100.0
        val r10min = 250.0 // IP = 2.5 -> Bueno

        val (ip, evaluacion) = CalculosPruebas.calcularIndicePolarizacion(r1min, r10min)

        assertEquals(2.5, ip, 0.01)
        assertEquals("Bueno", evaluacion)
    }

    @Test
    fun `calcularIndicePolarizacion menor a 1 coma 5 retorna Peligroso`() {
        val r1min = 100.0
        val r10min = 120.0 // IP = 1.2 -> Peligroso

        val (ip, evaluacion) = CalculosPruebas.calcularIndicePolarizacion(r1min, r10min)

        assertEquals(1.2, ip, 0.01)
        assertEquals("Peligroso (Aislamiento deficiente)", evaluacion)
    }

    @Test
    fun `calcularTtr dentro de rango no genera alerta`() {
        val v1 = 10000.0
        val v2 = 230.0
        val vPlaca = 43.48 // 10000 / 230 = 43.478

        val resultado = CalculosPruebas.calcularTtr(v1, v2, vPlaca)

        assertFalse(resultado.superaTolerancia)
        assertTrue(resultado.variacionPorcentaje < 25.0)
    }

    @Test
    fun `calcularTtr fuera de rango 25 por ciento genera alerta`() {
        val v1 = 10000.0
        val v2 = 100.0 // TTR medido = 100
        val vPlaca = 43.48 // Variacion > 100%

        val resultado = CalculosPruebas.calcularTtr(v1, v2, vPlaca)

        assertTrue(resultado.superaTolerancia)
        assertTrue(resultado.mensaje.contains("¡ALERTA!"))
    }
}
