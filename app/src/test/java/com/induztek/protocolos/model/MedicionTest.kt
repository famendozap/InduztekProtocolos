package com.induztek.protocolos.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicionTest {

    @Test
    fun acepta_punto_decimal() {
        assertEquals(Medicion("Lectura 1 min", "6100", "MΩ"), Medicion.desdeTexto("Lectura 1 min", "6100", "MΩ"))
    }

    @Test
    fun convierte_coma_decimal_a_punto() {
        assertEquals("5.8", Medicion.desdeTexto("Lectura", "5,8", "GΩ")?.valor)
    }

    @Test
    fun acepta_valores_negativos() {
        assertEquals("-12.5", Medicion.desdeTexto("Temperatura", "-12,5", "°C")?.valor)
    }

    @Test
    fun rechaza_valor_no_numerico() {
        assertNull(Medicion.desdeTexto("Lectura", "abc", "MΩ"))
        assertNull(Medicion.desdeTexto("Lectura", "NaN", "MΩ"))
        assertNull(Medicion.desdeTexto("Lectura", "1e5", "MΩ"))
        assertNull(Medicion.desdeTexto("Lectura", "", "MΩ"))
    }

    @Test
    fun rechaza_nombre_vacio() {
        assertNull(Medicion.desdeTexto("  ", "10", "A"))
    }
}
