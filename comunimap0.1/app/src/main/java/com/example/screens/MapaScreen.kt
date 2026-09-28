package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Categoria
import com.example.models.Reporte
import com.example.utils.Constantes
//mapa
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch

// ====================================================================================================
// 🗺️ PANTALLA: MAPA INTERACTIVO DE ALERTAS Y REPORTES CIUDADANOS (ComuniMap)
// ====================================================================================================
// Esta pantalla muestra un mapa interactivo con soporte de gestos táctiles:
// 1. Detección de toques sobre pines GPS para abrir la barra/burbuja informativa de la alerta.
// 2. Controles flotantes oscuros de Zoom (+ / -) y Recentrado con iconos color naranja/ámbar.
// 3. Barra tipo burbuja emergente oscura con información de la categoría, estado, título,
//    descripción, dirección y botón de acción directa "Resolver".
// ====================================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    reportes: List<Reporte>,
    categorias: List<Categoria>,
    reporteSeleccionado: Reporte?,
    onSelectReporte: (Reporte?) -> Unit,
    onCambiarEstado: ((Int, String) -> Unit)? = null,
    onCrearReporteEnUbicacion: (Double, Double) -> Unit
) {

    // ============================================================
    // FILTRO DE CATEGORÍAS
    // ============================================================

    var categoriaFiltroId by remember {
        mutableStateOf<Int?>(null)
    }

    // ============================================================
    // MODO PARA CREAR UN NUEVO REPORTE TOCANDO EL MAPA
    // ============================================================

    var modoCrearPin by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    // ============================================================
    // CENTRO INICIAL DEL MAPA: COCHABAMBA
    // ============================================================

    val cochabamba = LatLng(
        Constantes.DEFAULT_MAP_LATITUDE,
        Constantes.DEFAULT_MAP_LONGITUDE
    )

    // ============================================================
    // ESTADO DE LA CÁMARA DE GOOGLE MAPS
    // ============================================================

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            cochabamba,
            14f
        )
    }

    // ============================================================
    // FILTRAR LOS REPORTES REALES DE MYSQL
    // ============================================================

    val reportesVisibles = remember(
        reportes,
        categoriaFiltroId
    ) {

        if (categoriaFiltroId == null) {

            reportes

        } else {

            reportes.filter {
                it.categoriaId == categoriaFiltroId
            }
        }
    }

    Scaffold(

        // ========================================================
        // BARRA SUPERIOR
        // ========================================================

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text = "Mapa de Alertas Ciudadanas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "${reportesVisibles.size} puntos activos en tiempo real",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },

                actions = {

                    // Botón para activar la creación de un reporte
                    FilledTonalIconButton(

                        onClick = {
                            modoCrearPin = !modoCrearPin
                        },

                        colors = IconButtonDefaults
                            .filledTonalIconButtonColors(

                                containerColor =
                                    if (modoCrearPin)
                                        Color(0xFFEA580C)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,

                                contentColor =
                                    if (modoCrearPin)
                                        Color.White
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                            )
                    ) {

                        Icon(
                            imageVector = Icons.Default.AddLocation,
                            contentDescription = "Reportar aquí"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ====================================================
            // GOOGLE MAPS REAL
            // ====================================================

            GoogleMap(

                modifier = Modifier.fillMaxSize(),

                cameraPositionState = cameraPositionState,

                properties = MapProperties(),

                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = false
                ),

                // =================================================
                // TOCAR UNA PARTE DEL MAPA
                // =================================================

                onMapClick = { punto ->

                    if (modoCrearPin) {

                        // Abrimos el formulario con
                        // las coordenadas reales seleccionadas.
                        onCrearReporteEnUbicacion(
                            punto.latitude,
                            punto.longitude
                        )

                        modoCrearPin = false

                    } else {

                        // Si toca una zona vacía,
                        // cerramos la información del reporte.
                        onSelectReporte(null)
                    }
                }
            ) {

                // =================================================
                // MARCADORES REALES DE MYSQL
                // =================================================

                reportesVisibles.forEach { reporte ->

                    val posicion = LatLng(
                        reporte.latitud,
                        reporte.longitud
                    )

                    // Color según el estado real del reporte
                    val colorMarcador = when (
                        reporte.estado.lowercase().trim()
                    ) {

                        "pendiente" ->
                            BitmapDescriptorFactory.HUE_RED

                        "en proceso" ->
                            BitmapDescriptorFactory.HUE_ORANGE

                        "solucionado" ->
                            BitmapDescriptorFactory.HUE_GREEN

                        else ->
                            BitmapDescriptorFactory.HUE_AZURE
                    }

                    Marker(

                        state = rememberUpdatedMarkerState(
                            position = posicion
                        ),

                        title = reporte.titulo,

                        snippet =
                            reporte.direccion
                                ?: reporte.descripcion,

                        icon = BitmapDescriptorFactory
                            .defaultMarker(colorMarcador),

                        onClick = {

                            // Al tocar el marcador mostramos
                            // la misma burbuja que ya tenía ComuniMap.
                            onSelectReporte(reporte)

                            true
                        }
                    )
                }
            }

            // ====================================================
            // FILTROS SUPERIORES
            // ====================================================

            LazyRow(

                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)

            ) {

                item {

                    FilterChip(

                        selected =
                            categoriaFiltroId == null,

                        onClick = {
                            categoriaFiltroId = null
                        },

                        label = {
                            Text("Todas (${reportes.size})")
                        },

                        leadingIcon = {

                            Icon(
                                Icons.Default.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                items(categorias) { categoria ->

                    val cantidad = reportes.count {
                        it.categoriaId == categoria.id
                    }

                    FilterChip(

                        selected =
                            categoriaFiltroId == categoria.id,

                        onClick = {

                            categoriaFiltroId =
                                if (
                                    categoriaFiltroId ==
                                    categoria.id
                                ) {
                                    null
                                } else {
                                    categoria.id
                                }
                        },

                        label = {
                            Text(
                                "${categoria.nombre} ($cantidad)"
                            )
                        }
                    )
                }
            }

            // ====================================================
            // BOTONES + / - / CENTRAR
            // ====================================================

            Column(

                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 70.dp,
                        end = 16.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp)

            ) {

                // ACERCAR
                FloatingActionButton(

                    onClick = {

                        scope.launch {

                            cameraPositionState.animate(
                                CameraUpdateFactory.zoomIn()
                            )
                        }
                    },

                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFF59E0B),

                    shape = RoundedCornerShape(14.dp),

                    modifier = Modifier.size(46.dp)

                ) {

                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Acercar"
                    )
                }

                // ALEJAR
                FloatingActionButton(

                    onClick = {

                        scope.launch {

                            cameraPositionState.animate(
                                CameraUpdateFactory.zoomOut()
                            )
                        }
                    },

                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFF59E0B),

                    shape = RoundedCornerShape(14.dp),

                    modifier = Modifier.size(46.dp)

                ) {

                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Alejar"
                    )
                }

                // CENTRAR EN COCHABAMBA
                FloatingActionButton(

                    onClick = {

                        scope.launch {

                            cameraPositionState.animate(

                                CameraUpdateFactory
                                    .newLatLngZoom(
                                        cochabamba,
                                        14f
                                    )
                            )
                        }
                    },

                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFF59E0B),

                    shape = RoundedCornerShape(14.dp),

                    modifier = Modifier.size(46.dp)

                ) {

                    Icon(
                        Icons.Default.GpsFixed,
                        contentDescription = "Centrar mapa"
                    )
                }
            }

            // ====================================================
            // MENSAJE DE MODO CREAR REPORTE
            // ====================================================

            if (modoCrearPin) {

                Surface(

                    shape = RoundedCornerShape(12.dp),

                    color = Color(0xFFEA580C),

                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            bottom = 120.dp,
                            start = 20.dp,
                            end = 20.dp
                        )
                ) {

                    Row(

                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        ),

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Icon(
                            Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )

                        Text(
                            text = "Toca el mapa donde está el problema",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ====================================================
            // INFORMACIÓN DEL MARCADOR
            // ====================================================

            AnimatedVisibility(

                visible =
                    reporteSeleccionado != null,

                enter =
                    slideInVertically(
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(),

                exit =
                    slideOutVertically(
                        targetOffsetY = { it / 2 }
                    ) + fadeOut(),

                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    )

            ) {

                reporteSeleccionado?.let {

                    BurbujaInformacionReporte(

                        reporte = it,

                        onCerrar = {
                            onSelectReporte(null)
                        }
                    )
                }
            }
        }
    }
}

// ====================================================================================================
// 💬 COMPONENTE: BARRA / BURBUJA INFORMATIVA DEL REPORTE (Diseño Fiel a la Imagen)
// ====================================================================================================
@Composable
fun BurbujaInformacionReporte(
    reporte: Reporte,
    onCerrar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF070E1E) // Fondo azul oscuro carbón profundo
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("burbuja_info_reporte")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // ----------------------------------------------------------------------------------------
            // FILA SUPERIOR: BADGE DE CATEGORÍA + BADGE DE ESTADO
            // ----------------------------------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etiqueta de Categoría (Pill Naranja / Terracota)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFC2410C) // Naranja quemado vivo
                ) {
                    Text(
                        text = reporte.categoriaNombre?.ifBlank { "Alerta Ciudadana" } ?: "Alerta",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Etiqueta de Estado Real: Pendiente, En Proceso, Solucionado
                    val esSolucionado = reporte.estado.equals("Solucionado", ignoreCase = true) || reporte.estado.equals("Resuelto", ignoreCase = true)
                    val esEnProceso = reporte.estado.equals("En Proceso", ignoreCase = true)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            esSolucionado -> Color(0xFF14532D).copy(alpha = 0.6f)
                            esEnProceso -> Color(0xFF1E3A8A).copy(alpha = 0.6f)
                            else -> Color(0xFF291F0A) // Tono oscuro cálido para "Pendiente"
                        }
                    ) {
                        Text(
                            text = if (esSolucionado) "Solucionado" else reporte.estado,
                            color = when {
                                esSolucionado -> Color(0xFF4ADE80)
                                esEnProceso -> Color(0xFF60A5FA)
                                else -> Color(0xFFF59E0B) // Ámbar dorado
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Botón discreto para cerrar la burbuja
                    IconButton(
                        onClick = onCerrar,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar info",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ----------------------------------------------------------------------------------------
            // TÍTULO PRINCIPAL (Texto Blanco en Negrita)
            // ----------------------------------------------------------------------------------------
            Text(
                text = reporte.titulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ----------------------------------------------------------------------------------------
            // DESCRIPCIÓN (Texto Gris Suave / Azulado Muted)
            // ----------------------------------------------------------------------------------------
            Text(
                text = reporte.descripcion,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ----------------------------------------------------------------------------------------
            // FILA INFERIOR: DIRECCIÓN / COORDENADAS
            // ----------------------------------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Ubicación",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reporte.direccion ?: "Lat: ${String.format("%.4f", reporte.latitud)}, Lng: ${String.format("%.4f", reporte.longitud)}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
