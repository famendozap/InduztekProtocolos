package com.induztek.protocolos.model

/**
 * Tipos de equipo eléctrico que Induztek prueba en terreno.
 * Basado en la sección 2.1 del caso: "transformadores, relés de protección, celdas, cables, entre otros".
 */
enum class TipoEquipo(val etiqueta: String) {
    TRANSFORMADOR("Transformador"),
    RELE_PROTECCION("Relé de protección"),
    CELDA("Celda"),
    CABLE("Cable"),
    OTRO("Otro")
}
