package com.induztek.protocolos.util

data class EstadoReglasContrasena(
    val tieneLongitudMinima: Boolean = false,
    val tieneMayuscula: Boolean = false,
    val tieneMinuscula: Boolean = false,
    val tieneNumero: Boolean = false,
    val tieneSimbolo: Boolean = false,
    val sinEspacios: Boolean = false,
    val coinciden: Boolean = false
) {
    val esValida: Boolean
        get() = tieneLongitudMinima &&
                tieneMayuscula &&
                tieneMinuscula &&
                tieneNumero &&
                tieneSimbolo &&
                sinEspacios &&
                coinciden
}

object ValidadorContrasena {

    private const val SIMBOLOS = "!@#$%^&*()_+-=[]{}|;:'\",.<>?/`~\\"

    fun evaluar(contrasena: String, confirmacion: String = contrasena): EstadoReglasContrasena {
        val tieneLongitud = contrasena.length >= 8
        val tieneMayus = contrasena.any { it.isUpperCase() }
        val tieneMinus = contrasena.any { it.isLowerCase() }
        val tieneNum = contrasena.any { it.isDigit() }
        val tieneSimb = contrasena.any { it in SIMBOLOS }
        val sinEsp = contrasena.isNotEmpty() && !contrasena.contains(" ")
        val coincidenClaves = contrasena.isNotEmpty() && contrasena == confirmacion

        return EstadoReglasContrasena(
            tieneLongitudMinima = tieneLongitud,
            tieneMayuscula = tieneMayus,
            tieneMinuscula = tieneMinus,
            tieneNumero = tieneNum,
            tieneSimbolo = tieneSimb,
            sinEspacios = sinEsp,
            coinciden = coincidenClaves
        )
    }

    fun obtenerMensajeError(contrasena: String, confirmacion: String): String? {
        val estado = evaluar(contrasena, confirmacion)
        if (estado.esValida) return null

        return when {
            contrasena.isBlank() -> "La contraseña no puede estar vacía"
            !estado.sinEspacios -> "La contraseña no debe contener espacios en blanco"
            !estado.tieneLongitudMinima -> "La contraseña debe tener al menos 8 caracteres"
            !estado.tieneMayuscula -> "Falta incluir al menos una letra mayúscula (A-Z)"
            !estado.tieneMinuscula -> "Falta incluir al menos una letra minúscula (a-z)"
            !estado.tieneNumero -> "Falta incluir al menos un número (0-9)"
            !estado.tieneSimbolo -> "Falta incluir al menos un símbolo especial (ej. @, #, $, %)"
            !estado.coinciden -> "Las contraseñas no coinciden"
            else -> "La contraseña no cumple con todas las reglas de seguridad"
        }
    }
}
