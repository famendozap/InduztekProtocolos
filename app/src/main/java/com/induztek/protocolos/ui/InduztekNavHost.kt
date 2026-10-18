package com.induztek.protocolos.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.induztek.protocolos.ui.screens.DetalleEquipoScreen
import com.induztek.protocolos.ui.screens.ListaEquiposScreen
import com.induztek.protocolos.ui.screens.NuevaPruebaScreen
import com.induztek.protocolos.ui.screens.NuevoEquipoScreen
import com.induztek.protocolos.ui.screens.auth.LoginScreen
import com.induztek.protocolos.ui.screens.auth.RegistroScreen
import com.induztek.protocolos.viewmodel.InduztekViewModel
import com.induztek.protocolos.viewmodel.auth.AuthViewModel

private object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val ARG_EQUIPO_ID = "equipoId"
    const val LISTA = "lista"
    const val ESTADISTICAS = "estadisticas"
    const val NUEVO_EQUIPO = "nuevoEquipo"
    const val DETALLE_EQUIPO = "detalle/{$ARG_EQUIPO_ID}"
    const val NUEVA_PRUEBA = "nuevaPrueba/{$ARG_EQUIPO_ID}"

    fun detalle(equipoId: Long) = "detalle/$equipoId"
    fun nuevaPrueba(equipoId: Long) = "nuevaPrueba/$equipoId"
}

private val argumentosEquipo = listOf(
    navArgument(Rutas.ARG_EQUIPO_ID) { type = NavType.LongType }
)

@Composable
fun InduztekApp(application: Application) {
    val viewModel: InduztekViewModel = viewModel(factory = InduztekViewModel.factory(application))
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(application))
    val navController = rememberNavController()

    val equipos by viewModel.equipos.collectAsState()
    val todosLosProtocolos by viewModel.todosLosProtocolos.collectAsState()
    val historial by viewModel.historialEquipoSeleccionado.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    
    val usuarioActual by authViewModel.usuarioActual.collectAsState()
    val authMensajeError by authViewModel.mensajeError.collectAsState()
    val cargando by authViewModel.cargando.collectAsState()

    // Redirección limpia al iniciar sesión o registrarse
    LaunchedEffect(usuarioActual) {
        if (usuarioActual != null) {
            navController.navigate(Rutas.LISTA) {
                popUpTo(Rutas.LOGIN) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                cargando = cargando,
                mensajeError = authMensajeError,
                onLoginClick = { username, password ->
                    authViewModel.login(username, password)
                },
                onIrARegistro = {
                    authViewModel.limpiarError()
                    navController.navigate(Rutas.REGISTRO)
                }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                cargando = cargando,
                mensajeError = authMensajeError,
                onRegistrarClick = { username, nombre, contrasena, rol ->
                    authViewModel.registrar(username, nombre, contrasena, rol)
                },
                onVolverALogin = {
                    authViewModel.limpiarError()
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.LISTA) {
            ListaEquiposScreen(
                usuario = usuarioActual,
                equipos = equipos,
                onEquipoClick = { equipo ->
                    viewModel.seleccionarEquipo(equipo.id)
                    navController.navigate(Rutas.detalle(equipo.id))
                },
                onNuevoEquipoClick = { navController.navigate(Rutas.NUEVO_EQUIPO) },
                onVerEstadisticas = { navController.navigate(Rutas.ESTADISTICAS) },
                onCerrarSesion = {
                    authViewModel.logout()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.LISTA) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.ESTADISTICAS) {
            com.induztek.protocolos.ui.screens.EstadisticasScreen(
                equipos = equipos,
                protocolos = todosLosProtocolos,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.NUEVO_EQUIPO) {
            NuevoEquipoScreen(
                mensajeError = mensajeError,
                onLimpiarError = { viewModel.limpiarError() },
                onGuardar = { codigo, tipo, instalacion ->
                    viewModel.crearEquipo(codigo, tipo, instalacion) { nuevoId ->
                        viewModel.seleccionarEquipo(nuevoId)
                        navController.navigate(Rutas.detalle(nuevoId)) {
                            popUpTo(Rutas.LISTA)
                        }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.DETALLE_EQUIPO, arguments = argumentosEquipo) { entry ->
            val equipoId = entry.arguments?.getLong(Rutas.ARG_EQUIPO_ID) ?: return@composable
            LaunchedEffect(equipoId) { viewModel.seleccionarEquipo(equipoId) }

            DetalleEquipoScreen(
                equipo = equipos.find { it.id == equipoId },
                historial = historial,
                onNuevaPrueba = { navController.navigate(Rutas.nuevaPrueba(equipoId)) },
                onFormalizarProtocolo = { protocolo -> viewModel.formalizarProtocolo(protocolo) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.NUEVA_PRUEBA, arguments = argumentosEquipo) { entry ->
            val equipoId = entry.arguments?.getLong(Rutas.ARG_EQUIPO_ID) ?: return@composable

            NuevaPruebaScreen(
                codigoEquipo = equipos.find { it.id == equipoId }?.codigo ?: "",
                mensajeError = mensajeError,
                onLimpiarError = { viewModel.limpiarError() },
                onGuardar = { mediciones, observaciones ->
                    viewModel.registrarProtocolo(equipoId, mediciones, observaciones) {
                        navController.popBackStack()
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
