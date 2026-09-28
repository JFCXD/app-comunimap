package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.api.AlertasRepository
import com.example.api.ApiClient
import com.example.models.Categoria
import com.example.models.CrearReporteRequest
import com.example.models.Reporte
import com.example.models.Usuario
import com.example.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ====================================================================================================
// 📱 ACTIVIDAD PRINCIPAL (MainActivity) - ComuniMap
// ====================================================================================================
// Punto de entrada de la aplicación Android. Gestiona:
// 1. Configuración de pantalla Edge-to-Edge y motor de temas (Claro/Oscuro/Sistema).
// 2. Control de sesión activa mediante `AlertasViewModel`.
// 3. Sistema de navegación por pestañas (`NavigationBar` con 5 módulos: Inicio, Reportes, Mapa, Usuarios, Más).
// ====================================================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita el diseño inmersivo de borde a borde en Android
        setContent {
            val viewModel: AlertasViewModel = viewModel()
            val modoTema by viewModel.modoTema.collectAsState()

            // Determinación dinámica del tema (Claro, Oscuro o basado en el Sistema Operativo)
            val isDark = when (modoTema) {
                ModoTema.OSCURO -> true
                ModoTema.CLARO -> false
                ModoTema.SISTEMA -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                ComuniMapApp(viewModel = viewModel)
            }
        }
    }
}

// ====================================================================================================
// 🧠 VIEWMODEL GLOBAL (AlertasViewModel)
// ====================================================================================================
// Centraliza el estado de la aplicación y sirve como intermediario entre la UI (Compose) y el
// repositorio de datos remotos (MySQL/PHP API).
//
// 📌 ¿CÓMO MODIFICAR LA LÓGICA DE NEGOCIO?
// 1. Si agregas nuevas entidades (ej: Comentarios, Notificaciones):
//    - Crea un nuevo `StateFlow` aquí y el método correspondiente que llame al `repository`.
// 2. Si cambias a Room Database o Firebase:
//    - Modificas las llamadas dentro de las corrutinas `viewModelScope.launch { ... }`.
// ====================================================================================================

class AlertasViewModel : ViewModel() {
    private val repository = AlertasRepository()

    // ------------------------------------------------------------------------------------------------
    // 📦 FLUJOS DE ESTADO PRINCIPALES (Expuestos como StateFlow inmutables hacia las pantallas)
    // ------------------------------------------------------------------------------------------------
    val reportes: StateFlow<List<Reporte>> = repository.reportes
    val categorias: StateFlow<List<Categoria>> = repository.categorias
    val usuarios: StateFlow<List<Usuario>> = repository.usuarios
    val estaConectadoApi: StateFlow<Boolean?> = repository.estaConectadoApi
    val ultimoMensajeApi: StateFlow<String> = repository.ultimoMensajeApi
    val estaCargando: StateFlow<Boolean> = repository.estaCargando

    // Control de Sesión y Usuario autenticado
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _sesionIniciada = MutableStateFlow(false)
    val sesionIniciada: StateFlow<Boolean> = _sesionIniciada.asStateFlow()

    // Configuración del Tema de la App
    private val _modoTema = MutableStateFlow(ModoTema.SISTEMA)
    val modoTema: StateFlow<ModoTema> = _modoTema.asStateFlow()

    // Reporte actualmente seleccionado para visualización detallada
    private val _reporteSeleccionado = MutableStateFlow<Reporte?>(null)
    val reporteSeleccionado: StateFlow<Reporte?> = _reporteSeleccionado.asStateFlow()

    // Estado de visibilidad del diálogo flotante de nuevo reporte
    private val _mostrarDialogoNuevoReporte = MutableStateFlow(false)
    val mostrarDialogoNuevoReporte: StateFlow<Boolean> = _mostrarDialogoNuevoReporte.asStateFlow()

    init {
        // Carga inicial de datos al iniciar la aplicación
        refrescarDatos()
    }

    /**
     * 🔄 Descarga la información más reciente desde el servidor PHP/MySQL
     */
    fun refrescarDatos() {
        viewModelScope.launch {
            repository.cargarDatosRemotos()
        }
    }

    /**
     * 🔐 Autentica y almacena el usuario activo en memoria
     */
    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        _sesionIniciada.value = true
    }

    /**
     * 🚪 Cierra la sesión activa y retorna a la pantalla de login
     */
    fun cerrarSesion() {
        _usuarioActual.value = null
        _sesionIniciada.value = false
    }

    /**
     * 🎨 Cambia el tema visual de la aplicación (Claro, Oscuro o Sistema)
     */
    fun cambiarModoTema(nuevoModo: ModoTema) {
        _modoTema.value = nuevoModo
    }

    /**
     * 👤 Actualiza los datos del perfil de usuario activo en memoria local
     */
    fun actualizarPerfil(usuarioActualizado: Usuario) {
        _usuarioActual.value = usuarioActualizado
    }

    fun seleccionarReporte(reporte: Reporte?) {
        _reporteSeleccionado.value = reporte
    }

    fun setMostrarDialogoNuevoReporte(mostrar: Boolean) {
        _mostrarDialogoNuevoReporte.value = mostrar
    }

    /**
     * 📝 Crea un nuevo reporte ciudadano y lo envía a la base de datos MySQL (crear_reporte.php)
     */
    fun crearReporte(request: CrearReporteRequest) {
        viewModelScope.launch {
            repository.crearReporte(request)
        }
    }
}

// ====================================================================================================
// 🧭 ENUMERACIÓN DE DESTINOS DE NAVEGACIÓN
// ====================================================================================================
enum class Pantalla(val titulo: String, val ruta: String) {
    INICIO("Inicio", "inicio"),
    REPORTES("Reportes", "reportes"),
    MAPA("Mapa", "mapa"),
    USUARIOS("Usuarios", "usuarios"),
    MAS("Más", "mas")
}

// ====================================================================================================
// 📱 CONTENEDOR PRINCIPAL: GESTIÓN ENTRE LOGIN Y DASHBOARD
// ====================================================================================================
@Composable
fun ComuniMapApp(viewModel: AlertasViewModel = viewModel()) {
    val sesionIniciada by viewModel.sesionIniciada.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()

    if (!sesionIniciada) {
        // Muestra la pantalla de login hasta que el usuario se autentique
        LoginScreen(
            onLoginExitoso = { usuario ->
                viewModel.iniciarSesion(usuario)
            }
        )
    } else {
        // Muestra el dashboard con barra de navegación inferior
        MainDashboard(viewModel = viewModel, usuarioActual = usuarioActual)
    }
}

// ====================================================================================================
// 📊 DASHBOARD PRINCIPAL Y BARRA DE NAVEGACIÓN INFERIOR (Material Design 3)
// ====================================================================================================
@Composable
fun MainDashboard(
    viewModel: AlertasViewModel,
    usuarioActual: Usuario?
) {
    var pantallaActual by remember { mutableStateOf(Pantalla.INICIO) }

    // Colección reactiva de estados desde el ViewModel
    val reportes by viewModel.reportes.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val estaConectadoApi by viewModel.estaConectadoApi.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    val reporteSeleccionado by viewModel.reporteSeleccionado.collectAsState()
    val mostrarDialogoNuevo by viewModel.mostrarDialogoNuevoReporte.collectAsState()
    val modoTema by viewModel.modoTema.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Pestaña 1: Inicio
                NavigationBarItem(
                    selected = pantallaActual == Pantalla.INICIO,
                    onClick = { pantallaActual = Pantalla.INICIO },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = {
                        Text(
                            "Inicio",
                            fontSize = 11.sp,
                            fontWeight = if (pantallaActual == Pantalla.INICIO) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_inicio")
                )

                // Pestaña 2: Reportes
                NavigationBarItem(
                    selected = pantallaActual == Pantalla.REPORTES,
                    onClick = { pantallaActual = Pantalla.REPORTES },
                    icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Reportes") },
                    label = {
                        Text(
                            "Reportes",
                            fontSize = 11.sp,
                            fontWeight = if (pantallaActual == Pantalla.REPORTES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_reportes")
                )

                // Pestaña 3: Mapa Interactivo
                NavigationBarItem(
                    selected = pantallaActual == Pantalla.MAPA,
                    onClick = { pantallaActual = Pantalla.MAPA },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
                    label = {
                        Text(
                            "Mapa",
                            fontSize = 11.sp,
                            fontWeight = if (pantallaActual == Pantalla.MAPA) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_mapa")
                )

                // Pestaña 4: Gestión de Usuarios
                NavigationBarItem(
                    selected = pantallaActual == Pantalla.USUARIOS,
                    onClick = { pantallaActual = Pantalla.USUARIOS },
                    icon = { Icon(Icons.Default.People, contentDescription = "Usuarios") },
                    label = {
                        Text(
                            "Usuarios",
                            fontSize = 11.sp,
                            fontWeight = if (pantallaActual == Pantalla.USUARIOS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_usuarios")
                )

                // Pestaña 5: Más Opciones (Perfil, Temas, Ajustes, Cerrar Sesión)
                NavigationBarItem(
                    selected = pantallaActual == Pantalla.MAS,
                    onClick = { pantallaActual = Pantalla.MAS },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Más") },
                    label = {
                        Text(
                            "Más",
                            fontSize = 11.sp,
                            fontWeight = if (pantallaActual == Pantalla.MAS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_mas")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Conmutación entre pantallas activas
            when (pantallaActual) {
                Pantalla.INICIO -> {
                    InicioScreen(
                        reportes = reportes,
                        categorias = categorias,
                        estaConectadoApi = estaConectadoApi,
                        estaCargando = estaCargando,
                        onRefrescar = { viewModel.refrescarDatos() },
                        onNavigateToMapa = { pantallaActual = Pantalla.MAPA },
                        onNavigateToReportes = { pantallaActual = Pantalla.REPORTES },
                        onSelectReporte = { rep ->
                            viewModel.seleccionarReporte(rep)
                            pantallaActual = Pantalla.REPORTES
                        },
                        onCrearReporteClick = {
                            viewModel.setMostrarDialogoNuevoReporte(true)
                            pantallaActual = Pantalla.REPORTES
                        }
                    )
                }
                Pantalla.REPORTES -> {
                    ReportesScreen(
                        reportes = reportes,
                        categorias = categorias,
                        reporteSeleccionado = reporteSeleccionado,
                        estaCargando = estaCargando,
                        onRefrescar = { viewModel.refrescarDatos() },
                        onSelectReporte = { rep -> viewModel.seleccionarReporte(rep) },
                        onCrearReporte = { nuevo -> viewModel.crearReporte(nuevo) },
                        mostrarDialogoNuevo = mostrarDialogoNuevo,
                        onDismissDialogoNuevo = { viewModel.setMostrarDialogoNuevoReporte(false) },
                        onAbrirDialogoNuevo = { viewModel.setMostrarDialogoNuevoReporte(true) },
                        usuarioActualId = usuarioActual?.idUsuario ?: 1
                    )
                }
                Pantalla.MAPA -> {
                    MapaScreen(
                        reportes = reportes,
                        categorias = categorias,
                        reporteSeleccionado = reporteSeleccionado,
                        onSelectReporte = { rep -> viewModel.seleccionarReporte(rep) },
                        onCrearReporteEnUbicacion = { lat, lng ->
                            viewModel.setMostrarDialogoNuevoReporte(true)
                            pantallaActual = Pantalla.REPORTES
                        }
                    )
                }
                Pantalla.USUARIOS -> {
                    UsuariosScreen(
                        usuarios = usuarios,
                        estaCargando = estaCargando,
                        onRefrescar = { viewModel.refrescarDatos() }
                    )
                }
                Pantalla.MAS -> {
                    MasScreen(
                        usuarioActual = usuarioActual,
                        modoTemaActual = modoTema,
                        onCambiarModoTema = { nuevoTema -> viewModel.cambiarModoTema(nuevoTema) },
                        onActualizarPerfil = { perfil -> viewModel.actualizarPerfil(perfil) },
                        onCerrarSesion = { viewModel.cerrarSesion() }
                    )
                }
            }
        }
    }
}
