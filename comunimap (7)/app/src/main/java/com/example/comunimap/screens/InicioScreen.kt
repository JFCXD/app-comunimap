package com.example.comunimap.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.api.ApiClient
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA PRINCIPAL: INICIO (DASHBOARD MODERNO)
// =====================================================

// Colores del tema oscuro / navy inspirados en la referencia visual
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkCardBg = Color(0xFF162238)
private val AccentBlue = Color(0xFF38BDF8)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    onNavigateToReportes: () -> Unit,
    onNavigateToMapa: () -> Unit,
    onNavigateToUsuarios: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Métricas reales conectadas a Railway y MySQL
    var totalUsuarios by remember { mutableIntStateOf(0) }
    var totalReportes by remember { mutableIntStateOf(0) }
    var pendientesCount by remember { mutableIntStateOf(0) }
    var enProcesoCount by remember { mutableIntStateOf(0) }
    var solucionadosCount by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    // Consulta de datos reales desde Railway
    fun cargarEstadisticas() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Consulta de usuarios
                val respUsuarios = ApiClient.apiService.obtenerUsuarios()
                Log.d("USUARIOS_HTTP", "HTTP: ${respUsuarios.code()}")
                if (respUsuarios.isSuccessful && respUsuarios.body()?.success == true) {
                    val uList = respUsuarios.body()?.usuarios ?: emptyList()
                    totalUsuarios = uList.size
                    Log.d("USUARIOS_RESPONSE", "Total usuarios reales: $totalUsuarios")
                }

                // Consulta de reportes
                val respReportes = ApiClient.apiService.obtenerReportes()
                Log.d("REPORTES_HTTP", "HTTP: ${respReportes.code()}")
                if (respReportes.isSuccessful && respReportes.body()?.success == true) {
                    val rList = respReportes.body()?.reportes ?: emptyList()
                    totalReportes = rList.size
                    pendientesCount = rList.count { it.estado.equals("Pendiente", ignoreCase = true) }
                    enProcesoCount = rList.count { it.estado.equals("En Proceso", ignoreCase = true) }
                    solucionadosCount = rList.count { it.estado.equals("Solucionado", ignoreCase = true) }
                    Log.d("REPORTES_RESPONSE", "Total reportes: $totalReportes (P:$pendientesCount, EP:$enProcesoCount, S:$solucionadosCount)")
                } else if (!respReportes.isSuccessful) {
                    errorMessage = "Error del servidor: HTTP ${respReportes.code()}"
                }
            } catch (ex: Exception) {
                Log.e("REPORTES_ERROR", "Error cargando métricas", ex)
                errorMessage = "No se pudo conectar con el servidor."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarEstadisticas()
    }

    Scaffold(
        containerColor = DarkNavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Logo circular de ComuniMap
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                                .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Logo ComuniMap",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Título y Subtítulo
                        Column {
                            Text(
                                text = "ComuniMap",
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "MAPA INTERACTIVO • Tu Barrio",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { cargarEstadisticas() },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("inicio_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar estadísticas",
                            tint = Color(0xFF60A5FA)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavyBg,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading && totalReportes == 0 && totalUsuarios == 0) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF60A5FA),
                        modifier = Modifier.testTag("inicio_loading_indicator")
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Cargando métricas de ComuniMap...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (errorMessage != null && totalReportes == 0 && totalUsuarios == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFF87171),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cargarEstadisticas() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.testTag("inicio_retry_button")
                    ) {
                        Text("Reintentar", color = Color.White)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // =====================================================
                    // 1. TARJETA HERO CON DEGRADADO
                    // =====================================================
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp)),
                            shape = RoundedCornerShape(26.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6), // Azul vibrante
                                                Color(0xFF6366F1), // Morado / Índigo
                                                Color(0xFFF97316), // Naranja cálido
                                                Color(0xFFEA580C)  // Naranja quemado
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Fila superior: Saludo y Sol
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "¡Hola, Vecino!",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Reporta incidencias y mejora tu comunidad.",
                                                color = Color(0xEEFFFFFF),
                                                fontSize = 13.sp
                                            )
                                        }

                                        // Badge decorativo con Sol
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0x33FFFFFF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WbSunny,
                                                contentDescription = "Clima Soleado",
                                                tint = Color(0xFFFDE047),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Sub-bloque decorativo de clima (estático según lo solicitado)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0x28FFFFFF))
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Temperatura
                                            Text(
                                                text = "22°C",
                                                color = Color.White,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            // Descripción central
                                            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                                Text(
                                                    text = "Parcialmente\nNublado",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    lineHeight = 16.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Clima Barrial Actual",
                                                    color = Color(0xCCFFFFFF),
                                                    fontSize = 10.sp
                                                )
                                            }

                                            // Indicadores derecha: Humedad y Viento
                                            Column(horizontalAlignment = Alignment.Start) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.WaterDrop,
                                                        contentDescription = null,
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Humedad: 54%",
                                                        color = Color(0xEEFFFFFF),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Air,
                                                        contentDescription = null,
                                                        tint = Color(0xFFBAE6FD),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Viento: 11 km/h",
                                                        color = Color(0xEEFFFFFF),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =====================================================
                    // 2. SECCIÓN: ESTADO DEL SISTEMA (4 TARJETAS)
                    // =====================================================
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Estado del Sistema",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Tarjeta 1: Total
                                ResumenMiniCard(
                                    titulo = "Total",
                                    cantidad = totalReportes,
                                    icono = Icons.Default.FolderOpen,
                                    colorFondo = Color(0xFFF0F4FF),
                                    colorIcono = Color(0xFF3B82F6),
                                    colorNumero = Color(0xFF1E3A8A),
                                    colorTexto = Color(0xFF475569),
                                    modifier = Modifier.weight(1f)
                                )

                                // Tarjeta 2: Pendientes
                                ResumenMiniCard(
                                    titulo = "Pendientes",
                                    cantidad = pendientesCount,
                                    icono = Icons.Default.Schedule,
                                    colorFondo = Color(0xFFFEF3C7),
                                    colorIcono = Color(0xFFD97706),
                                    colorNumero = Color(0xFF92400E),
                                    colorTexto = Color(0xFF92400E),
                                    modifier = Modifier.weight(1f)
                                )

                                // Tarjeta 3: En Proceso
                                ResumenMiniCard(
                                    titulo = "En Proceso",
                                    cantidad = enProcesoCount,
                                    icono = Icons.Default.Autorenew,
                                    colorFondo = Color(0xFFE0F2FE),
                                    colorIcono = Color(0xFF0284C7),
                                    colorNumero = Color(0xFF0369A1),
                                    colorTexto = Color(0xFF0369A1),
                                    modifier = Modifier.weight(1f)
                                )

                                // Tarjeta 4: Solucionados
                                ResumenMiniCard(
                                    titulo = "Solucionados",
                                    cantidad = solucionadosCount,
                                    icono = Icons.Default.CheckCircle,
                                    colorFondo = Color(0xFFDCFCE7),
                                    colorIcono = Color(0xFF16A34A),
                                    colorNumero = Color(0xFF15803D),
                                    colorTexto = Color(0xFF15803D),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // =====================================================
                    // 3. ACCESOS RÁPIDOS Y CATEGORÍAS
                    // =====================================================
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Categorías y Accesos",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AccesoRapidoCard(
                                    titulo = "Reportes",
                                    subtitulo = "$totalReportes registrados",
                                    icono = Icons.Default.Assignment,
                                    colorAcento = Color(0xFF60A5FA),
                                    onClick = onNavigateToReportes,
                                    modifier = Modifier.weight(1f)
                                )

                                AccesoRapidoCard(
                                    titulo = "Mapa",
                                    subtitulo = "Explorar Barrio",
                                    icono = Icons.Default.Map,
                                    colorAcento = Color(0xFF34D399),
                                    onClick = onNavigateToMapa,
                                    modifier = Modifier.weight(1f)
                                )

                                AccesoRapidoCard(
                                    titulo = "Usuarios",
                                    subtitulo = "$totalUsuarios activos",
                                    icono = Icons.Default.People,
                                    colorAcento = Color(0xFFA78BFA),
                                    onClick = onNavigateToUsuarios,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable para cada una de las 4 tarjetas pequeñas de resumen
@Composable
private fun ResumenMiniCard(
    titulo: String,
    cantidad: Int,
    icono: ImageVector,
    colorFondo: Color,
    colorIcono: Color,
    colorNumero: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = cantidad.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorNumero
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = titulo,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorTexto,
                maxLines = 1
            )
        }
    }
}

// Composable para accesos rápidos inferiores con estilo navy
@Composable
private fun AccesoRapidoCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorAcento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorAcento.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorAcento,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = titulo,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = subtitulo,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
