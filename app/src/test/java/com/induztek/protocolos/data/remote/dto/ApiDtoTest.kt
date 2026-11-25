package com.induztek.protocolos.data.remote.dto

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.induztek.protocolos.model.Equipo
import com.induztek.protocolos.model.EstadoProtocolo
import com.induztek.protocolos.model.Medicion
import com.induztek.protocolos.model.Protocolo
import com.induztek.protocolos.model.TipoEquipo
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ApiDtoTest {
    @Test
    fun `equipo request omits server generated id`() {
        val equipo = Equipo(
            id = 42,
            codigo = "TAG-42",
            tipo = TipoEquipo.TRANSFORMADOR,
            instalacionCliente = "Planta Norte"
        )

        val json = JsonParser.parseString(Gson().toJson(equipo.toRequestDto())).asJsonObject

        assertFalse(json.has("id"))
        assertEquals("TAG-42", json.get("codigo").asString)
        assertEquals("TRANSFORMADOR", json.get("tipo").asString)
    }

    @Test
    fun `protocolo request uses backend fields and ISO date`() {
        val protocolo = Protocolo(
            id = 25,
            equipoId = 42,
            fechaHora = LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            mediciones = listOf(Medicion("Aislamiento", "1.5", "GOhm")),
            estado = EstadoProtocolo.REGISTRADO_EN_TERRENO,
            tecnicoNombre = "Tecnico"
        )

        val json = JsonParser.parseString(Gson().toJson(protocolo.toRequestDto())).asJsonObject

        assertFalse(json.has("id"))
        assertFalse(json.has("estado"))
        assertEquals("2026-01-02T03:04:05", json.get("fechaHora").asString)
        assertEquals("Aislamiento", json.getAsJsonArray("mediciones")[0].asJsonObject.get("nombre").asString)
    }

    @Test
    fun `null measurement unit from backend maps to empty unit`() {
        val json = """
            {
              "id": 25,
              "equipoId": 42,
              "fechaHora": "2026-01-02T03:04:05",
              "mediciones": [{"nombre": "Aislamiento", "valor": "1.5", "unidad": null}],
              "estado": "REGISTRADO_EN_TERRENO",
              "observaciones": null,
              "fotoEvidenciaBytes": null,
              "tecnicoNombre": "Tecnico",
              "ingenieroNombre": null,
              "temperaturaAmbiente": null,
              "humedadRelativa": null,
              "instrumentosUsados": null,
              "resultadoGeneral": null,
              "ubicacionGPS": null
            }
        """.trimIndent()

        val response = Gson().fromJson(json, ProtocoloResponseDto::class.java).toProtocolo()

        assertEquals("", response.mediciones.single().unidad)
    }
}
