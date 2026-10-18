package com.induztek.protocolos.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InstrumentoTest {

    @Test
    fun `instrumento con fecha de calibracion futura no esta vencido`() {
        val instrumento = Instrumento(
            id = 1,
            nombre = "Megómetro Fluke",
            numeroSerie = "FLK-9901",
            fechaCalibracion = LocalDate.now().minusMonths(6),
            fechaVencimientoCalibracion = LocalDate.now().plusMonths(6)
        )

        assertFalse(instrumento.estaVencido)
    }

    @Test
    fun `instrumento con fecha de calibracion pasada esta vencido`() {
        val instrumento = Instrumento(
            id = 2,
            nombre = "Probador TTR",
            numeroSerie = "TTR-2020",
            fechaCalibracion = LocalDate.now().minusYears(2),
            fechaVencimientoCalibracion = LocalDate.now().minusMonths(1)
        )

        assertTrue(instrumento.estaVencido)
    }
}
