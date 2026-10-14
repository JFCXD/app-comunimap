package com.example.comunimap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.comunimap.api.ApiClient
import com.example.comunimap.components.NotificacionBanner
import com.example.comunimap.models.MarcarNotificacionRequest
import com.example.comunimap.models.Notificacion
import com.example.comunimap.models.Usuario
import com.example.comunimap.screens.InicioScreen
import com.example.comunimap.screens.LoginScreen
import com.example.comunimap.screens.MapaScreen
import com.example.comunimap.screens.MasScreen
import com.example.comunimap.screens.ReportesScreen
import com.example.comunimap.screens.UsuariosScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// =====================================================
// DEFINICIÓN DE PANTALLAS DE NAVEGACIÓN
// =====================================================

enum class DestinoNavegacion(val titulo: String, val icono: ImageVector, val tag: String) {
    INICIO("Inicio", Icons.Default.Home, "nav_inicio"),
    REPORTES("Reportes", Icons.Default.FormatListBulleted, "nav_reportes"),
    MAPA("Mapa", Icons.Default.Map, "nav_mapa"),
    USUARIOS("Usuarios", Icons.Default.People, "nav_usuarios"),
    MAS("Más", Icons.Default.MoreHoriz, "nav_mas")
}

// Colores del tema para consistencia con el diseño visual oscuro / navy
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkNavBottomBar = Color(0xFF0F172A)
private val AccentBlue = Color(0xFF3B82F6)

private val ComuniMapColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFFF97316),
    background = DarkNavyBg,
    surface = Color(0xFF162238),
    onSurface = Color.White
)

// =====================================================
// ACTIVIDAD PRINCIPAL - COMUNIMAP
// =====================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme(colorScheme = ComuniMapColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkNavyBg
                ) {
                    ComuniMapApp()
                }
            }
        }
    }
}

// =====================================================
// FLUJO PRINCIPAL CON CONTROL DE SESIÓN Y ROLES
// =====================================================

@Composable
fun ComuniMapApp() {
    // Sesión en memoria: si es null, muestra Login; si existe, muestra la aplicación principal
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var destinoActual by remember { mutableStateOf(DestinoNavegacion.INICIO) }

    // Estado global de notificaciones internas
    var notificacionVisible by remember { mutableStateOf<Notificacion?>(null) }
    val colaNotificaciones = remember(usuarioActual?.idUsuario) { mutableStateListOf<Notificacion>() }
    val notificacionesMostradas = remember(usuarioActual?.idUsuario) { mutableStateListOf<String>() }

    // 1. Consulta periódica cada 20 segundos de notificaciones no leídas para el usuario en sesión
    LaunchedEffect(usuarioActual?.idUsuario) {
        val uid = usuarioActual?.idUsuario ?: return@LaunchedEffect
        while (isActive) {
            try {
                Log.d("NOTIFICACIONES", "Consultando notificaciones para idUsuario: $uid")
                val response = ApiClient.apiService.obtenerNotificaciones(uid)
                if (response.isSuccessful) {
                    val notifs = response.body()?.notificaciones ?: emptyList()
                    val noLeidas = notifs.filter { it.leida == 0 && !notificacionesMostradas.contains(it.idNotificacion) }
                    Log.d("NOTIFICACIONES", "Total recibidas: ${notifs.size}, no leídas nuevas: ${noLeidas.size}")

                    for (notif in noLeidas) {
                        if (!colaNotificaciones.any { it.idNotificacion == notif.idNotificacion } &&
                            notificacionVisible?.idNotificacion != notif.idNotificacion
                        ) {
                            colaNotificaciones.add(notif)
                        }
                    }
                } else {
                    Log.e("NOTIFICACIONES", "Error al obtener notificaciones: HTTP ${response.code()}")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("NOTIFICACIONES", "Excepción al consultar notificaciones", e)
            }
            delay(20_000L)
        }
    }

    // 2. Procesamiento de la cola de notificaciones una por una (5 segundos visible cada una)
    LaunchedEffect(usuarioActual?.idUsuario) {
        val uid = usuarioActual?.idUsuario ?: return@LaunchedEffect
        while (isActive) {
            if (colaNotificaciones.isNotEmpty()) {
                val siguiente = colaNotificaciones.removeAt(0)
                // Registrar para evitar reaparición durante la misma sesión
                if (!notificacionesMostradas.contains(siguiente.idNotificacion)) {
                    notificacionesMostradas.add(siguiente.idNotificacion)
                }

                notificacionVisible = siguiente

                // Mostrar el banner durante 5 segundos
                delay(5000L)

                // Ocultar banner
                notificacionVisible = null

                // Pequeña pausa para permitir que la animación de salida termine limpiamente
                delay(400L)

                // Marcar como leída en la API
                try {
                    val res = ApiClient.apiService.marcarNotificacionLeida(
                        MarcarNotificacionRequest(idNotificacion = siguiente.idNotificacion)
                    )
                    if (res.isSuccessful && res.body()?.success == true) {
                        Log.d("NOTIFICACIONES", "Notificación ${siguiente.idNotificacion} marcada como leída exitosamente")
                    } else {
                        Log.e("NOTIFICACIONES", "Fallo al marcar leída ${siguiente.idNotificacion}: HTTP ${res.code()}")
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("NOTIFICACIONES", "Error al marcar notificación leída", e)
                }
            } else {
                delay(500L)
            }
        }
    }

    if (usuarioActual == null) {
        // -------------------------------------------------
        // PANTALLA DE LOGIN
        // -------------------------------------------------
        LoginScreen(
            onLoginSuccess = { usuario ->
                usuarioActual = usuario
                destinoActual = DestinoNavegacion.INICIO
                notificacionVisible = null
                colaNotificaciones.clear()
                notificacionesMostradas.clear()
            }
        )
    } else {
        // Detección centralizada de rol: solo Administrador y Usuario (tolerante a espacios)
        val esAdministrador =
            usuarioActual
                ?.rol
                ?.trim()
                ?.equals(
                    "Administrador",
                    ignoreCase = true
                ) == true

        // Seguridad: si el usuario no es Administrador y está en USUARIOS, redirigir a INICIO
        if (!esAdministrador && destinoActual == DestinoNavegacion.USUARIOS) {
            destinoActual = DestinoNavegacion.INICIO
        }

        // Pestañas visibles: Usuario ve [Inicio, Reportes, Mapa, Más]; Administrador ve además [Usuarios]
        val destinosVisibles = if (esAdministrador) {
            DestinoNavegacion.values().toList()
        } else {
            DestinoNavegacion.values().filter { it != DestinoNavegacion.USUARIOS }
        }

        // -------------------------------------------------
        // APLICACIÓN PRINCIPAL (CON SESIÓN ACTIVA)
        // -------------------------------------------------
        if (destinoActual != DestinoNavegacion.INICIO) {
            BackHandler {
                destinoActual = DestinoNavegacion.INICIO
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = DarkNavyBg,
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkNavBottomBar,
                        contentColor = Color(0xFF94A3B8),
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        destinosVisibles.forEach { destino ->
                            val isSelected = destinoActual == destino
                            NavigationBarItem(
                                icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                                label = { Text(destino.titulo) },
                                selected = isSelected,
                                onClick = { destinoActual = destino },
                                modifier = Modifier.testTag(destino.tag),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFF60A5FA),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8),
                                    indicatorColor = Color(0xFF1E293B)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier.padding(innerPadding),
                    color = DarkNavyBg
                ) {
                    when (destinoActual) {
                        DestinoNavegacion.INICIO -> InicioScreen(
                            usuarioActual = usuarioActual,
                            esAdministrador = esAdministrador,
                            onNavigateToReportes = { destinoActual = DestinoNavegacion.REPORTES },
                            onNavigateToMapa = { destinoActual = DestinoNavegacion.MAPA },
                            onNavigateToUsuarios = {
                                if (esAdministrador) {
                                    destinoActual = DestinoNavegacion.USUARIOS
                                }
                            }
                        )
                        DestinoNavegacion.REPORTES -> ReportesScreen(
                            usuarioActual = usuarioActual,
                            esAdministrador = esAdministrador
                        )
                        DestinoNavegacion.MAPA -> MapaScreen(
                            usuarioActual = usuarioActual,
                            esAdministrador = esAdministrador
                        )
                        DestinoNavegacion.USUARIOS -> {
                            if (esAdministrador) {
                                UsuariosScreen()
                            } else {
                                // Protección contra accesos no autorizados
                                destinoActual = DestinoNavegacion.INICIO
                            }
                        }
                        DestinoNavegacion.MAS -> MasScreen(
                            usuarioActual = usuarioActual,
                            onCerrarSesion = {
                                usuarioActual = null
                                destinoActual = DestinoNavegacion.INICIO
                                notificacionVisible = null
                                colaNotificaciones.clear()
                                notificacionesMostradas.clear()
                            }
                        )
                    }
                }
            }

            // Banner flotante de notificación interna en la parte superior
            NotificacionBanner(
                notificacion = notificacionVisible,
                onDismiss = {
                    notificacionVisible = null
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
