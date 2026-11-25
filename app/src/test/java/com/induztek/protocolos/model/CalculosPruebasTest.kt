package com.induztek.protocolos.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculosPruebasTest {

    @Test
    fun `7 testCalcularIndicePolarizacionNormal`() {
        val r1min = 100.0
        val r10min = 250.0

        val (ip, evaluacion) = CalculosPruebas.calcularIndicePolarizacion(r1min, r10min)

        assertEquals(2.5, ip, 0.01)
        assertEquals("Bueno", evaluacion)
    }

    @Test
    fun `8 testCalcularTtrFueraDeToleranciaAlerta`() {
        val v1 = 10000.0
        val v2 = 100.0
        val vPlaca = 43.48

        val resultado = CalculosPruebas.calcularTtr(v1, v2, vPlaca)

        assertTrue(resultado.superaTolerancia)
        assertTrue(resultado.mensaje.contains("¡ALERTA!"))
    }
}
