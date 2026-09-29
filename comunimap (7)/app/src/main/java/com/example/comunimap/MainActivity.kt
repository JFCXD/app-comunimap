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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.comunimap.models.Usuario
import com.example.comunimap.screens.InicioScreen
import com.example.comunimap.screens.LoginScreen
import com.example.comunimap.screens.MapaScreen
import com.example.comunimap.screens.MasScreen
import com.example.comunimap.screens.ReportesScreen
import com.example.comunimap.screens.UsuariosScreen

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
// FLUJO PRINCIPAL CON CONTROL DE SESIÓN
// =====================================================

@Composable
fun ComuniMapApp() {
    // Sesión en memoria: si es null, muestra Login; si existe, muestra la aplicación principal
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
    var destinoActual by remember { mutableStateOf(DestinoNavegacion.INICIO) }

    if (usuarioActual == null) {
        // -------------------------------------------------
        // PANTALLA DE LOGIN
        // -------------------------------------------------
        LoginScreen(
            onLoginSuccess = { usuario ->
                usuarioActual = usuario
                destinoActual = DestinoNavegacion.INICIO
            }
        )
    } else {
        // -------------------------------------------------
        // APLICACIÓN PRINCIPAL (CON SESIÓN ACTIVA)
        // -------------------------------------------------
        if (destinoActual != DestinoNavegacion.INICIO) {
            BackHandler {
                destinoActual = DestinoNavegacion.INICIO
            }
        }

        Scaffold(
            containerColor = DarkNavyBg,
            bottomBar = {
                NavigationBar(
                    containerColor = DarkNavBottomBar,
                    contentColor = Color(0xFF94A3B8),
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    DestinoNavegacion.values().forEach { destino ->
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
                        onNavigateToReportes = { destinoActual = DestinoNavegacion.REPORTES },
                        onNavigateToMapa = { destinoActual = DestinoNavegacion.MAPA },
                        onNavigateToUsuarios = { destinoActual = DestinoNavegacion.USUARIOS }
                    )
                    DestinoNavegacion.REPORTES -> ReportesScreen()
                    DestinoNavegacion.MAPA -> MapaScreen(
                        usuarioActual = usuarioActual
                    )
                    DestinoNavegacion.USUARIOS -> UsuariosScreen()
                    DestinoNavegacion.MAS -> MasScreen(
                        usuarioActual = usuarioActual,
                        onCerrarSesion = {
                            usuarioActual = null
                            destinoActual = DestinoNavegacion.INICIO
                        }
                    )
                }
            }
        }
    }
}
