package com.induztek.protocolos.model

data class Medicion(
    val nombre: String,
    val valor: String,
    val unidad: String = ""
) {
    companion object {
        private val NUMERO = Regex("^-?\\d+(\\.\\d+)?$")

        fun desdeTexto(nombre: String, valor: String, unidad: String = ""): Medicion? {
            val valorNormalizado = valor.trim().replace(',', '.')
            if (nombre.isBlank() || !NUMERO.matches(valorNormalizado)) return null
            return Medicion(nombre.trim(), valorNormalizado, unidad.trim())
        }
    }
}
