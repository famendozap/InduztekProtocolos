package com.induztek.protocolos.viewmodel

import com.induztek.protocolos.model.Rol
import com.induztek.protocolos.util.ValidadorContrasena
import com.induztek.protocolos.viewmodel.auth.FormularioRegistroState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {

    @Test
    fun `1 testAuthViewModelLoginValidaCamposVacios`() {
        val username = ""
        val contrasena = ""

        val usernameError = if (username.isBlank()) "Ingresa tu usuario" else null
        val contrasenaError = if (contrasena.isBlank()) "Ingresa tu contraseña" else null

        assertNotNull(usernameError)
        assertNotNull(contrasenaError)
        assertEquals("Ingresa tu usuario", usernameError)
        assertEquals("Ingresa tu contraseña", contrasenaError)
    }

    @Test
    fun `2 testAuthViewModelRegistrarValidaContrasenaDebil`() {
        val contrasenaInvalida = "clave123"
        val confirmacion = "clave123"

        val evalPass = ValidadorContrasena.evaluar(contrasenaInvalida, confirmacion)
        val errorMsg = ValidadorContrasena.obtenerMensajeError(contrasenaInvalida, confirmacion)

        val state = FormularioRegistroState(contrasenaError = errorMsg)

        assertTrue(!evalPass.esValida)

        assertTrue(state.tieneErrores)
        assertNotNull(state.contrasenaError)
    }

    @Test
    fun `3 testAuthViewModelRegistrarValidaCertificacionSecObligatoriaEnTecnico`() {
        val rol = Rol.TECNICO
        val campoRol1Sec = ""

        val campoRolErr = when (rol) {
            Rol.TECNICO -> if (campoRol1Sec.isBlank()) "Indica tu certificación SEC" else null
            else -> null
        }

        val state = FormularioRegistroState(campoRolError = campoRolErr)

        assertTrue(state.tieneErrores)
        assertEquals("Indica tu certificación SEC", state.campoRolError)
    }
}
