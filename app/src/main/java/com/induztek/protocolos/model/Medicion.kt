package com.induztek.protocolos.model

/**
 * Un valor de medición individual tomado durante la prueba (ej. "Resistencia de aislación" = "120" "MΩ").
 * Un Protocolo puede tener varias mediciones, ya que cada tipo de equipo/prueba registra distintos parámetros.
 */
data class Medicion(
    val nombre: String,
    val valor: String,
    val unidad: String = ""
) {
    companion object {
        private val NUMERO = Regex("^-?\\d+(\\.\\d+)?$")

        /**
         * Crea una medición validando que el valor sea numérico (acepta coma o punto
         * decimal y lo guarda con punto). Devuelve null si el nombre está vacío o el valor no es número.
         */
        fun desdeTexto(nombre: String, valor: String, unidad: String = ""): Medicion? {
            val valorNormalizado = valor.trim().replace(',', '.')
            if (nombre.isBlank() || !NUMERO.matches(valorNormalizado)) return null
            return Medicion(nombre.trim(), valorNormalizado, unidad.trim())
        }
    }
}
