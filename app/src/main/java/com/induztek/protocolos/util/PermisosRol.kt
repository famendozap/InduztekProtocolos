package com.induztek.protocolos.util

import com.induztek.protocolos.model.Rol

object PermisosRol {

    /**
     * Solo los roles no clientes pueden registrar equipos en el sistema.
     */
    fun puedeCrearEquipo(rol: Rol?): Boolean {
        if (rol == null) return false
        return rol != Rol.CLIENTE
    }

    /**
     * "El que formaliza no crea protocolos".
     * Solo el Técnico de terreno crea y registra protocolos/pruebas en terreno.
     * Ingenieros y Jefes formalizan o supervisan, no registran pruebas en terreno.
     */
    fun puedeCrearProtocolo(rol: Rol?): Boolean {
        if (rol == null) return false
        return rol == Rol.TECNICO
    }

    /**
     * "Solo el ingeniero formaliza".
     * Únicamente el usuario con rol INGENIERO tiene la facultad de formalizar un protocolo.
     */
    fun puedeFormalizarProtocolo(rol: Rol?): Boolean {
        if (rol == null) return false
        return rol == Rol.INGENIERO
    }

    /**
     * El cliente tiene acceso de solo consulta a sus equipos y protocolos.
     */
    fun esSoloConsulta(rol: Rol?): Boolean {
        if (rol == null) return true
        return rol == Rol.CLIENTE
    }
}
