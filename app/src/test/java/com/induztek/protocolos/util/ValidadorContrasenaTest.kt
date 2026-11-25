package com.induztek.protocolos.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorContrasenaTest {

    @Test
    fun `9 testValidadorContrasenaCumpleTodasReglas`() {
        val claveValida = "Induztek#2025"
        val estado = ValidadorContrasena.evaluar(claveValida, claveValida)

        assertTrue(estado.esValida)
    }

    @Test
    fun `10 testValidadorContrasenaDetectaSinSimbolo`() {
        val claveSinSimb = "Induztek2025"
        val estado = ValidadorContrasena.evaluar(claveSinSimb, claveSinSimb)

        assertFalse(estado.tieneSimbolo)
        assertFalse(estado.esValida)
    }
}
