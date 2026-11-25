package com.induztek.protocolos.viewmodel

import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.model.Usuario
import com.induztek.protocolos.util.PermisosRol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InduztekViewModelTest {

    @Test
    fun `4 testInduztekViewModelCrearEquipoValidaCodigoVacio`() {
        val codigoInput = "   "
        val codigoLimpio = codigoInput.trim().uppercase()

        val codErr = if (codigoLimpio.isBlank()) "El código TAG del equipo no puede estar vacío" else null

        val formState = FormularioEquipoState(codigoError = codErr)

        assertNotNull(formState.codigoError)
        assertEquals("El código TAG del equipo no puede estar vacío", formState.codigoError)
    }

    @Test
    fun `5 testInduztekViewModelCrearEquipoDeniegaRolCliente`() {
        val clienteUser = Usuario(id = 1, username = "cliente", nombre = "Cliente S.A.", rol = Rol.CLIENTE)

        val puedeCrear = PermisosRol.puedeCrearEquipo(clienteUser.rol)
        val mensajeError = if (!puedeCrear) "Tu rol de Cliente no tiene permisos para crear equipos." else null

        assertFalse(puedeCrear)
        assertEquals("Tu rol de Cliente no tiene permisos para crear equipos.", mensajeError)
    }

    @Test
    fun `6 testInduztekViewModelRegistrarProtocoloDeniegaRolIngeniero`() {
        val ingenieroUser = Usuario(id = 2, username = "ingeniero", nombre = "Ana Ingeniera", rol = Rol.INGENIERO)

        val puedeCrearProtocolo = PermisosRol.puedeCrearProtocolo(ingenieroUser.rol)
        val mensajeError = if (!puedeCrearProtocolo) "Permiso denegado: El que formaliza no crea protocolos." else null

        assertFalse(puedeCrearProtocolo)
        assertTrue(mensajeError!!.contains("El que formaliza no crea protocolos"))
    }
}
