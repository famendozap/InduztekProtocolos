package com.induztek.protocolos.ui

import android.app.Application
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.induztek.protocolos.ui.screens.EstadisticasScreen
import com.induztek.protocolos.ui.screens.ListaEquiposScreen
import com.induztek.protocolos.ui.screens.NuevaPruebaScreen
import com.induztek.protocolos.ui.screens.NuevoEquipoScreen
import com.induztek.protocolos.ui.screens.PerfilScreen
import com.induztek.protocolos.ui.screens.auth.CambiarClaveScreen
import com.induztek.protocolos.ui.screens.auth.LoginScreen
import com.induztek.protocolos.ui.screens.auth.RecuperarClaveScreen
import com.induztek.protocolos.ui.screens.auth.RegistroScreen
import com.induztek.protocolos.viewmodel.InduztekViewModel
import com.induztek.protocolos.viewmodel.auth.AuthViewModel

private object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR_CLAVE = "recuperarClave"
    const val PERFIL = "perfil"
    const val CAMBIAR_CLAVE = "cambiarClave"

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
    val clientes by viewModel.clientes.collectAsState()
    val historial by viewModel.historialEquipoSeleccionado.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val equipoFormState by viewModel.equipoFormState.collectAsState()
    val protocoloFormState by viewModel.protocoloFormState.collectAsState()

    val usuarioActual by authViewModel.usuarioActual.collectAsState()
    val authMensajeError by authViewModel.mensajeError.collectAsState()
    val authMensajeExito by authViewModel.mensajeExito.collectAsState()
    val cargando by authViewModel.cargando.collectAsState()

    val loginUsernameError by authViewModel.loginUsernameError.collectAsState()
    val loginContrasenaError by authViewModel.loginContrasenaError.collectAsState()
    val registroState by authViewModel.registroState.collectAsState()
    val recuperarState by authViewModel.recuperarState.collectAsState()
    val cambiarClaveState by authViewModel.cambiarClaveState.collectAsState()

    LaunchedEffect(usuarioActual) {
        if (usuarioActual != null) {
            val destinoActual = navController.currentDestination?.route
            if (destinoActual == Rutas.LOGIN || destinoActual == Rutas.REGISTRO) {
                navController.navigate(Rutas.LISTA) {
                    popUpTo(Rutas.LOGIN) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) + fadeIn() },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) + fadeOut() },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) + fadeIn() },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) + fadeOut() }
    ) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                cargando = cargando,
                mensajeError = authMensajeError,
                mensajeExito = authMensajeExito,
                usernameError = loginUsernameError,
                contrasenaError = loginContrasenaError,
                onLoginClick = { username, password ->
                    authViewModel.login(username, password)
                },
                onIrARegistro = {
                    authViewModel.logout()
                    authViewModel.limpiarError()
                    authViewModel.limpiarErroresFormulario()
                    navController.navigate(Rutas.REGISTRO)
                },
                onIrARecuperarClave = {
                    authViewModel.limpiarError()
                    authViewModel.limpiarErroresFormulario()
                    navController.navigate(Rutas.RECUPERAR_CLAVE)
                }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                cargando = cargando,
                mensajeError = authMensajeError,
                registroState = registroState,
                onLimpiarError = { authViewModel.limpiarError() },
                onRegistrarClick = { username, nombre, contrasena, confirmacion, rol, fotoBytes, preg, resp, campo1, campo2 ->
                    authViewModel.registrar(username, nombre, contrasena, confirmacion, rol, fotoBytes, preg, resp, campo1, campo2) {
                        navController.navigate(Rutas.LISTA) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                },
                onVolverALogin = {
                    authViewModel.limpiarError()
                    authViewModel.limpiarErroresFormulario()
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.RECUPERAR_CLAVE) {
            RecuperarClaveScreen(
                cargando = cargando,
                mensajeError = authMensajeError,
                recuperarState = recuperarState,
                onRecuperarClick = { username, resp, nuevaClave, conf ->
                    authViewModel.recuperarClave(username, resp, nuevaClave, conf) {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.RECUPERAR_CLAVE) { inclusive = true }
                        }
                    }
                },
                onVolver = {
                    authViewModel.limpiarError()
                    authViewModel.limpiarErroresFormulario()
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.PERFIL) {
            PerfilScreen(
                usuario = usuarioActual,
                cargando = cargando,
                mensajeError = authMensajeError,
                mensajeExito = authMensajeExito,
                onGuardarPerfil = { nombre, fotoBytes, campo1, campo2 ->
                    authViewModel.modificarPerfil(nombre, fotoBytes, campo1, campo2) {}
                },
                onIrACambiarClave = {
                    authViewModel.limpiarError()
                    authViewModel.limpiarErroresFormulario()
                    navController.navigate(Rutas.CAMBIAR_CLAVE)
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.CAMBIAR_CLAVE) {
            CambiarClaveScreen(
                cargando = cargando,
                mensajeError = authMensajeError,
                cambiarClaveState = cambiarClaveState,
                onCambiarClick = { actual, nueva, conf ->
                    authViewModel.cambiarClave(actual, nueva, conf) {
                        navController.popBackStack()
                    }
                },
                onVolver = {
                    authViewModel.limpiarError()
                    authViewModel.limpiarErroresFormulario()
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
                onVerPerfil = { navController.navigate(Rutas.PERFIL) },
                onCerrarSesion = {
                    authViewModel.logout()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.LISTA) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.ESTADISTICAS) {
            EstadisticasScreen(
                equipos = equipos,
                protocolos = todosLosProtocolos,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.NUEVO_EQUIPO) {
            NuevoEquipoScreen(
                clientes = clientes,
                mensajeError = mensajeError,
                equipoFormState = equipoFormState,
                onLimpiarError = { viewModel.limpiarError() },
                onGuardar = { codigo, tipo, instalacion, marca, modelo, serie, pot, ten, cli, gps ->
                    viewModel.crearEquipo(usuarioActual, codigo, tipo, instalacion, marca, modelo, serie, pot, ten, cli, gps) { nuevoId ->
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
                usuario = usuarioActual,
                equipo = equipos.find { it.id == equipoId },
                historial = historial,
                onNuevaPrueba = { navController.navigate(Rutas.nuevaPrueba(equipoId)) },
                onFormalizarProtocolo = { protocolo -> viewModel.formalizarProtocolo(usuarioActual, protocolo) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.NUEVA_PRUEBA, arguments = argumentosEquipo) { entry ->
            val equipoId = entry.arguments?.getLong(Rutas.ARG_EQUIPO_ID) ?: return@composable

            NuevaPruebaScreen(
                codigoEquipo = equipos.find { it.id == equipoId }?.codigo ?: "",
                mensajeError = mensajeError,
                protocoloFormState = protocoloFormState,
                onLimpiarError = { viewModel.limpiarError() },
                onGuardar = { mediciones, observaciones, fotoEvidenciaBytes, temp, hum, inst, res, gps ->
                    viewModel.registrarProtocolo(
                        usuarioActual = usuarioActual,
                        equipoId = equipoId,
                        mediciones = mediciones,
                        observaciones = observaciones,
                        fotoEvidenciaBytes = fotoEvidenciaBytes,
                        temperaturaAmbiente = temp,
                        humedadRelativa = hum,
                        instrumentosUsados = inst,
                        resultadoGeneral = res,
                        ubicacionGPS = gps
                    ) {
                        navController.popBackStack()
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
