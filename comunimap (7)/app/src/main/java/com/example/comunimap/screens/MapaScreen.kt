package com.example.comunimap.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.R
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.Categoria
import com.example.comunimap.models.CrearReporteRequest
import com.example.comunimap.models.Reporte
import com.example.comunimap.models.Usuario
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

// =====================================================
// PANTALLA: MAPA REAL (OPENSTREETMAP / OSMDROID)
// =====================================================

// Coordenadas iniciales centradas en Cochabamba, Bolivia
private const val COCHABAMBA_LAT = -17.3895
private const val COCHABAMBA_LNG = -66.1568
private const val DEFAULT_ZOOM = 13.0

// Colores del tema visual navy
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkCardBg = Color(0xFF162238)
private val AccentBlue = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    usuarioActual: Usuario? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    // Estados para el reporte seleccionado al tocar un marcador existente
    var reporteSeleccionado by remember { mutableStateOf<Reporte?>(null) }

    // =====================================================
    // ESTADOS DEL FLUJO DE CREACIÓN DE REPORTES
    // =====================================================
    var modoSeleccionUbicacion by remember { mutableStateOf(false) }
    var puntoTemporal by remember { mutableStateOf<GeoPoint?>(null) }
    var mostrarFormularioCrear by remember { mutableStateOf(false) }

    // Referencia al MapView
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    // Consulta de los reportes y categorías reales para el mapa
    fun cargarDatos() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. Cargar reportes para marcadores en tiempo real
                val resp = ApiClient.apiService.obtenerReportes()
                Log.d("REPORTES_HTTP", "HTTP: ${resp.code()}")
                if (resp.isSuccessful && resp.body()?.success == true) {
                    reportes = resp.body()?.reportes ?: emptyList()
                    Log.d("REPORTES_RESPONSE", "Cargados ${reportes.size} reportes en el mapa")
                    actualizarMarcadoresEnMapa(
                        context = context,
                        mapView = mapViewInstance,
                        reportes = reportes,
                        puntoTemporal = puntoTemporal,
                        modoSeleccion = modoSeleccionUbicacion,
                        onReporteClick = { rep ->
                            if (!modoSeleccionUbicacion) {
                                reporteSeleccionado = rep
                            }
                        }
                    )
                } else {
                    errorMessage = "No se pudieron obtener los reportes del mapa."
                }

                // 2. Cargar categorías para el formulario
                val respCat = ApiClient.apiService.obtenerCategorias()
                if (respCat.isSuccessful && respCat.body()?.success == true) {
                    categorias = respCat.body()?.categorias ?: emptyList()
                }
            } catch (ex: Exception) {
                Log.e("REPORTES_ERROR", "Error cargando marcadores del mapa", ex)
                errorMessage = "No se pudo conectar con el servidor."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    // Efecto para redibujar marcadores cuando cambia puntoTemporal o modoSeleccionUbicacion
    LaunchedEffect(puntoTemporal, modoSeleccionUbicacion) {
        actualizarMarcadoresEnMapa(
            context = context,
            mapView = mapViewInstance,
            reportes = reportes,
            puntoTemporal = puntoTemporal,
            modoSeleccion = modoSeleccionUbicacion,
            onReporteClick = { rep ->
                if (!modoSeleccionUbicacion) {
                    reporteSeleccionado = rep
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkNavyBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (modoSeleccionUbicacion) "Selecciona Ubicación" else "Mapa de Reportes",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (modoSeleccionUbicacion) "Toca el mapa para ubicar el incidente" else "Cochabamba • Tiempo Real",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    if (modoSeleccionUbicacion) {
                        // Botón para salir del modo selección
                        TextButton(
                            onClick = {
                                modoSeleccionUbicacion = false
                                puntoTemporal = null
                            }
                        ) {
                            Text("Cancelar", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(
                            onClick = { cargarDatos() },
                            enabled = !isLoading,
                            modifier = Modifier.testTag("mapa_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recargar mapa",
                                tint = Color(0xFF60A5FA)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavyBg,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // Controles flotantes en la esquina inferior derecha:
            // 1. Botón circular para centrar mapa
            // 2. Separación visual clara
            // 3. Botón principal "Crear reporte"
            // 4. Margen inferior suficiente respecto a la barra inferior de navegación
            if (!modoSeleccionUbicacion) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = 16.dp, end = 4.dp)
                ) {
                    // Botón circular para centrar mapa
                    FloatingActionButton(
                        onClick = {
                            mapViewInstance?.controller?.animateTo(GeoPoint(COCHABAMBA_LAT, COCHABAMBA_LNG))
                            mapViewInstance?.controller?.setZoom(DEFAULT_ZOOM)
                        },
                        shape = CircleShape,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF60A5FA),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("mapa_center_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Centrar en Cochabamba",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón principal y ÚNICO para crear reporte
                    ExtendedFloatingActionButton(
                        onClick = {
                            modoSeleccionUbicacion = true
                            puntoTemporal = null
                            reporteSeleccionado = null
                        },
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        text = {
                            Text(
                                text = "Crear reporte",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier.testTag("btn_crear_reporte_fab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // =====================================================
            // MAPA DE OPENSTREETMAP (OSMDROID)
            // =====================================================
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = ctx.packageName
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        // Ocultar los botones de zoom legacy de osmdroid en la esquina inferior
                        // para evitar amontonamiento con el botón Crear reporte
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        controller.setZoom(DEFAULT_ZOOM)
                        controller.setCenter(GeoPoint(COCHABAMBA_LAT, COCHABAMBA_LNG))

                        // Evento de toque en el mapa
                        val receiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                if (modoSeleccionUbicacion) {
                                    // Guardar coordenadas y fijar punto temporal
                                    puntoTemporal = p
                                    return true
                                }
                                return false
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean = false
                        }
                        overlays.add(MapEventsOverlay(receiver))

                        mapViewInstance = this
                    }
                },
                update = { mapView ->
                    actualizarMarcadoresEnMapa(
                        context = context,
                        mapView = mapView,
                        reportes = reportes,
                        puntoTemporal = puntoTemporal,
                        modoSeleccion = modoSeleccionUbicacion,
                        onReporteClick = { rep ->
                            if (!modoSeleccionUbicacion) {
                                reporteSeleccionado = rep
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("osm_map_view")
            )

            // =====================================================
            // CONTROLES DE ZOOM (+ / -) EN LA ESQUINA SUPERIOR IZQUIERDA
            // =====================================================
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = if (modoSeleccionUbicacion) 80.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Zoom +
                FloatingActionButton(
                    onClick = { mapViewInstance?.controller?.zoomIn() },
                    shape = RoundedCornerShape(10.dp),
                    containerColor = Color(0xF01E293B),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Acercar",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Botón Zoom -
                FloatingActionButton(
                    onClick = { mapViewInstance?.controller?.zoomOut() },
                    shape = RoundedCornerShape(10.dp),
                    containerColor = Color(0xF01E293B),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Alejar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // =====================================================
            // BANNER SUPERIOR EN MODO SELECCIÓN
            // =====================================================
            if (modoSeleccionUbicacion) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF0162238)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo Crear Reporte",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (puntoTemporal == null)
                                    "Toca el mapa para seleccionar la ubicación del reporte"
                                else
                                    "Ubicación seleccionada. Toca otro punto si deseas cambiarla.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        if (puntoTemporal == null) {
                            IconButton(onClick = {
                                modoSeleccionUbicacion = false
                                puntoTemporal = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // Indicador de carga
            if (isLoading) {
                Card(
                    modifier = Modifier
                        .align(if (modoSeleccionUbicacion) Alignment.Center else Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF60A5FA),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Actualizando reportes...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // =====================================================
            // DETALLE DE REPORTE EXISTENTE (AL TOCAR UN PIN)
            // =====================================================
            reporteSeleccionado?.let { reporte ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("mapa_reporte_info_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reporte.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { reporteSeleccionado = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF94A3B8))
                            }
                        }

                        // Estado con color distintivo (Rojo, Amarillo, Verde)
                        val colorEstado = when (reporte.estado.trim().lowercase()) {
                            "pendiente" -> Color(0xFFEF4444)     // Rojo
                            "en proceso" -> Color(0xFFF59E0B)    // Amarillo
                            "solucionado" -> Color(0xFF10B981)   // Verde
                            else -> Color(0xFF38BDF8)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colorEstado)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reporte.estado,
                                fontWeight = FontWeight.Bold,
                                color = colorEstado,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reporte.descripcion,
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            maxLines = 3
                        )

                        if (!reporte.direccion.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = reporte.direccion,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        if (!reporte.categoria.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Categoría: ${reporte.categoria}",
                                fontSize = 12.sp,
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // =====================================================
            // TARJETA TRAS TOCAR EL MAPA: "UBICACIÓN SELECCIONADA"
            // CON DOS ACCIONES CLARAS: CONTINUAR / CANCELAR
            // =====================================================
            if (modoSeleccionUbicacion && puntoTemporal != null) {
                val p = puntoTemporal!!
                val latStr = String.format(java.util.Locale.US, "%.6f", p.latitude)
                val lngStr = String.format(java.util.Locale.US, "%.6f", p.longitude)
                val coordenadasTexto = "$latStr, $lngStr"

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("tarjeta_punto_temporal"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ubicación seleccionada",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Coordenadas: $coordenadasTexto",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fila con las dos acciones claras: Cancelar y Continuar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Acción 1: Cancelar
                            OutlinedButton(
                                onClick = {
                                    modoSeleccionUbicacion = false
                                    puntoTemporal = null
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_cancelar_seleccion")
                            ) {
                                Text(
                                    text = "Cancelar",
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Acción 2: Continuar con el reporte
                            Button(
                                onClick = {
                                    mostrarFormularioCrear = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("btn_continuar_reporte")
                            ) {
                                Text(
                                    text = "Continuar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =====================================================
    // FORMULARIO MODAL DE CREACIÓN DE REPORTE
    // =====================================================
    if (mostrarFormularioCrear && puntoTemporal != null) {
        val p = puntoTemporal!!
        val latStr = String.format(java.util.Locale.US, "%.6f", p.latitude)
        val lngStr = String.format(java.util.Locale.US, "%.6f", p.longitude)
        val ubicacionFija = "$latStr,$lngStr"

        FormularioCrearReporteDialog(
            ubicacion = ubicacionFija,
            categorias = categorias,
            usuarioActual = usuarioActual,
            onDismiss = {
                mostrarFormularioCrear = false
            },
            onReporteCreado = {
                // Al guardar exitosamente: salir del modo selección, limpiar punto y refrescar marcadores
                modoSeleccionUbicacion = false
                puntoTemporal = null
                mostrarFormularioCrear = false
                cargarDatos()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Reporte creado con éxito en el mapa")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewInstance?.onDetach()
        }
    }
}

// Diálogo modal exclusivo para registrar el reporte con coordenadas fijas y usuario en sesión
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioCrearReporteDialog(
    ubicacion: String,
    categorias: List<Categoria>,
    usuarioActual: Usuario?,
    onDismiss: () -> Unit,
    onReporteCreado: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var selectedCategoriaId by remember { mutableStateOf(categorias.firstOrNull()?.idCategoria ?: "1") }

    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // El autor del reporte se toma obligatoriamente del usuario con sesión activa
    val idUsuarioAutor = usuarioActual?.idUsuario ?: "1"

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = Color(0xFF1E293B),
        titleContentColor = Color.White,
        title = { Text("Nuevo Reporte", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Indicador de autor autenticado
                Text(
                    text = "Reportando como: ${usuarioActual?.nombre ?: "Usuario"} (${usuarioActual?.email ?: ""})",
                    fontSize = 12.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.SemiBold
                )

                // Campo Coordenadas: estrictamente bloqueado / solo lectura
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ubicación en mapa (lat,lng) [Bloqueada]") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color(0xFF93C5FD),
                        unfocusedTextColor = Color(0xFF93C5FD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del reporte *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección / Referencia *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de categoría de la base de datos
                if (categorias.isNotEmpty()) {
                    Text("Categoría:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    var catExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = it }
                    ) {
                        val catNombre = categorias.find { it.idCategoria == selectedCategoriaId }?.nombre ?: "Seleccionar"
                        OutlinedTextField(
                            value = catNombre,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nombre) },
                                    onClick = {
                                        selectedCategoriaId = cat.idCategoria ?: "1"
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (formError != null) {
                    Text(
                        text = formError ?: "",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank() || descripcion.isBlank() || direccion.isBlank()) {
                        formError = "Por favor completa todos los campos obligatorios."
                        return@Button
                    }
                    coroutineScope.launch {
                        isSubmitting = true
                        formError = null
                        try {
                            val req = CrearReporteRequest(
                                titulo = titulo.trim(),
                                descripcion = descripcion.trim(),
                                ubicacion = ubicacion.trim(),
                                direccion = direccion.trim(),
                                idUsuario = idUsuarioAutor,
                                idCategoria = selectedCategoriaId,
                                imagen = null
                            )
                            Log.d("API_MUTATION", "POST crear_reporte.php autor: $idUsuarioAutor coords: $ubicacion")
                            val resp = ApiClient.apiService.crearReporte(req)
                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onReporteCreado()
                            } else {
                                formError = resp.body()?.message ?: "Error al crear reporte: HTTP ${resp.code()}"
                            }
                        } catch (ex: Exception) {
                            Log.e("API_ERROR", "Error creando reporte en mapa", ex)
                            formError = "No se pudo conectar con el servidor."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Crear Reporte", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancelar", color = Color(0xFF94A3B8))
            }
        }
    )
}

// Función auxiliar para dibujar los marcadores de reportes reales con sus colores según estado
private fun actualizarMarcadoresEnMapa(
    context: Context,
    mapView: MapView?,
    reportes: List<Reporte>,
    puntoTemporal: GeoPoint?,
    modoSeleccion: Boolean,
    onReporteClick: (Reporte) -> Unit
) {
    if (mapView == null) return

    // Conservar el primer overlay de eventos y limpiar los demás
    val eventOverlay = mapView.overlays.filterIsInstance<MapEventsOverlay>().firstOrNull()
    mapView.overlays.clear()
    if (eventOverlay != null) {
        mapView.overlays.add(eventOverlay)
    }

    // 1. Agregar marcadores de reportes reales clasificados por color
    reportes.forEach { reporte ->
        val ubicacion = reporte.ubicacion
        if (!ubicacion.isNullOrBlank()) {
            val partes = ubicacion.split(",")
            if (partes.size >= 2) {
                val lat = partes[0].trim().toDoubleOrNull()
                val lng = partes[1].trim().toDoubleOrNull()
                if (lat != null && lng != null) {
                    val marker = Marker(mapView)
                    marker.position = GeoPoint(lat, lng)

                    // Selección del color según el estado del reporte
                    // Pendiente -> Rojo
                    // En Proceso -> Amarillo
                    // Solucionado -> Verde
                    val drawableRes = when (reporte.estado.trim().lowercase()) {
                        "pendiente" -> R.drawable.ic_pin_rojo
                        "en proceso" -> R.drawable.ic_pin_amarillo
                        "solucionado" -> R.drawable.ic_pin_verde
                        else -> R.drawable.ic_pin_rojo
                    }

                    val pinDrawable = ContextCompat.getDrawable(context, drawableRes)
                    marker.icon = pinDrawable
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    marker.title = reporte.titulo
                    marker.subDescription = "Categoría: ${reporte.categoria ?: "N/A"}\nEstado: ${reporte.estado}\nDirección: ${reporte.direccion ?: "N/A"}"

                    marker.setOnMarkerClickListener { _, _ ->
                        if (!modoSeleccion) {
                            onReporteClick(reporte)
                        }
                        true
                    }

                    mapView.overlays.add(marker)
                }
            }
        }
    }

    // 2. Agregar marcador temporal de selección si existe (Azul distintivo)
    if (puntoTemporal != null) {
        val tempMarker = Marker(mapView)
        tempMarker.position = puntoTemporal
        tempMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_pin_seleccion)
        tempMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        tempMarker.title = "Ubicación fijada"
        mapView.overlays.add(tempMarker)
    }

    mapView.invalidate()
}
