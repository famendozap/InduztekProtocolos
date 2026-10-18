package com.induztek.protocolos.model

/**
 * Estado del protocolo de prueba (sección 3.2 del caso).
 * REGISTRADO_EN_TERRENO: el técnico ya completó el registro en el momento de la visita.
 * FORMALIZADO: el protocolo fue validado/cerrado (equivalente al antiguo "pasar a computador").
 */
enum class EstadoProtocolo(val etiqueta: String) {
    REGISTRADO_EN_TERRENO("Registrado en terreno"),
    FORMALIZADO("Formalizado")
}
