package com.example.comunimap.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.TextButton
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
import com.example.comunimap.models.Reporte
import com.example.comunimap.models.Usuario
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA PRINCIPAL: INICIO (CON ADAPTACIÓN POR ROL)
// =====================================================

// Colores del tema oscuro / navy
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkCardBg = Color(0xFF162238)
private val AccentBlue = Color(0xFF3B82F6)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    usuarioActual: Usuario? = null,
    esAdministrador: Boolean = false,
    onNavigateToReportes: () -> Unit,
    onNavigateToMapa: () -> Unit,
    onNavigateToUsuarios: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Datos reales obtenidos desde Railway
    var todosLosReportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var totalUsuarios by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    // Consulta de datos reales desde Railway
    suspend fun cargarEstadisticas() {
        isLoading = true
        errorMessage = null
        try {
            // 1. Consulta de reportes (necesaria para ambos roles)
            val respReportes = ApiClient.apiService.obtenerReportes()
            Log.d("REPORTES_HTTP", "HTTP: ${respReportes.code()}")
            if (respReportes.isSuccessful && respReportes.body()?.success == true) {
                todosLosReportes = respReportes.body()?.reportes ?: emptyList()
                Log.d("REPORTES_RESPONSE", "Total reportes en servidor: ${todosLosReportes.size}")
            } else if (!respReportes.isSuccessful) {
                errorMessage = "Error del servidor: HTTP ${respReportes.code()}"
            }

            // 2. Consulta de usuarios (solo relevante para métricas de Administrador)
            if (esAdministrador) {
                val respUsuarios = ApiClient.apiService.obtenerUsuarios()
                Log.d("USUARIOS_HTTP", "HTTP: ${respUsuarios.code()}")
                if (respUsuarios.isSuccessful && respUsuarios.body()?.success == true) {
                    val uList = respUsuarios.body()?.usuarios ?: emptyList()
                    totalUsuarios = uList.size
                    Log.d("USUARIOS_RESPONSE", "Total usuarios reales: $totalUsuarios")
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (ex: Exception) {
            Log.e("REPORTES_ERROR", "Error cargando métricas", ex)
            errorMessage = "No se pudo conectar con el servidor."
        } finally {
            isLoading = false
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ComuniMap",
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = if (esAdministrador) "PANEL DE ADMINISTRACIÓN" else "MAPA INTERACTIVO • Tu Barrio",
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
                        onClick = { coroutineScope.launch { cargarEstadisticas() } },
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
            if (isLoading && todosLosReportes.isEmpty()) {
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
                        text = "Cargando ComuniMap...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (errorMessage != null && todosLosReportes.isEmpty()) {
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
                        onClick = { coroutineScope.launch { cargarEstadisticas() } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.testTag("inicio_retry_button")
                    ) {
                        Text("Reintentar", color = Color.White)
                    }
                }
            } else {
                if (esAdministrador) {
                    // =====================================================
                    // 1. INICIO DE ADMINISTRADOR (DASHBOARD COMPLETO)
                    // =====================================================
                    InicioAdminContent(
                        totalReportes = todosLosReportes.size,
                        pendientesCount = todosLosReportes.count { it.estado.equals("Pendiente", ignoreCase = true) },
                        enProcesoCount = todosLosReportes.count { it.estado.equals("En Proceso", ignoreCase = true) },
                        solucionadosCount = todosLosReportes.count { it.estado.equals("Solucionado", ignoreCase = true) },
                        totalUsuarios = totalUsuarios,
                        onNavigateToReportes = onNavigateToReportes,
                        onNavigateToMapa = onNavigateToMapa,
                        onNavigateToUsuarios = onNavigateToUsuarios
                    )
                } else {
                    // =====================================================
                    // 2. NUEVO INICIO DE USUARIO (AMIGABLE, SENCILLO, ACCESIBLE)
                    // =====================================================
                    InicioUsuarioContent(
                        usuarioActual = usuarioActual,
                        todosLosReportes = todosLosReportes,
                        onNavigateToMapa = onNavigateToMapa,
                        onNavigateToReportes = onNavigateToReportes
                    )
                }
            }
        }
    }
}

// =====================================================
// COMPONENTE: CONTENIDO INICIO PARA USUARIO NORMAL
// =====================================================

@Composable
private fun InicioUsuarioContent(
    usuarioActual: Usuario?,
    todosLosReportes: List<Reporte>,
    onNavigateToMapa: () -> Unit,
    onNavigateToReportes: () -> Unit
) {
    // Filtrar estrictamente solo los reportes creados por el usuario en sesión
    val misReportes = remember(todosLosReportes, usuarioActual) {
        val uid = usuarioActual?.idUsuario?.toString() ?: ""
        todosLosReportes.filter { it.idUsuario?.toString() == uid }
    }

    val misTotal = misReportes.size
    val misPendientes = misReportes.count { it.estado.equals("Pendiente", ignoreCase = true) }
    val misEnProceso = misReportes.count { it.estado.equals("En Proceso", ignoreCase = true) }
    val misSolucionados = misReportes.count { it.estado.equals("Solucionado", ignoreCase = true) }

    // Estado para desplegar la sección de ayuda
    var ayudaExpandida by remember { mutableStateOf(false) }

    val nombreUsuario = usuarioActual?.nombre ?: "Vecino"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // =====================================================
        // 1. ENCABEZADO DEL USUARIO
        // =====================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hola, $nombreUsuario 👋",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bienvenido a ComuniMap",
                    color = Color(0xFF60A5FA),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ayuda a mejorar tu barrio reportando incidencias.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        // =====================================================
        // 2. TARJETA VISUAL SUPERIOR (SIN DATOS DE CLIMA FALSOS)
        // =====================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A), // Azul marino elegante
                                    Color(0xFF2563EB), // Azul vibrante
                                    Color(0xFF0284C7)  // Celeste profundo
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tu comunidad • Cochabamba",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Participación Vecinal Activa",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Un espacio para construir juntos un barrio más limpio, seguro y ordenado.",
                                color = Color(0xEEFFFFFF),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // =====================================================
        // 3. ACCIÓN PRINCIPAL DEL USUARIO: REPORTAR UN PROBLEMA
        // =====================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_reportar_problema"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Reportar un problema",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Selecciona en el mapa dónde ocurrió",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Botón grande "Ir al mapa" (cambia a la pestaña Mapa)
                    Button(
                        onClick = onNavigateToMapa,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_ir_al_mapa")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ir al mapa",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =====================================================
        // 4. RESUMEN PERSONAL COMPACTO ("MIS REPORTES")
        // =====================================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Mis reportes",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Estado de tus incidencias enviadas",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        TextButton(
                            onClick = onNavigateToReportes,
                            modifier = Modifier.testTag("btn_ver_todos_reportes")
                        ) {
                            Text(
                                text = "Ver todos",
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 métricas compactas personales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResumenMiniCard(
                            titulo = "Total",
                            cantidad = misTotal,
                            icono = Icons.Default.FolderOpen,
                            colorFondo = Color(0xFF1E293B),
                            colorIcono = Color(0xFF60A5FA),
                            colorNumero = Color.White,
                            colorTexto = Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f)
                        )
                        ResumenMiniCard(
                            titulo = "Pendiente",
                            cantidad = misPendientes,
                            icono = Icons.Default.Schedule,
                            colorFondo = Color(0xFF2A2415),
                            colorIcono = Color(0xFFFBBF24),
                            colorNumero = Color(0xFFFDE68A),
                            colorTexto = Color(0xFFD97706),
                            modifier = Modifier.weight(1f)
                        )
                        ResumenMiniCard(
                            titulo = "En Proceso",
                            cantidad = misEnProceso,
                            icono = Icons.Default.Autorenew,
                            colorFondo = Color(0xFF14243A),
                            colorIcono = Color(0xFF38BDF8),
                            colorNumero = Color(0xFFBAE6FD),
                            colorTexto = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                        ResumenMiniCard(
                            titulo = "Resuelto",
                            cantidad = misSolucionados,
                            icono = Icons.Default.CheckCircle,
                            colorFondo = Color(0xFF132B20),
                            colorIcono = Color(0xFF4ADE80),
                            colorNumero = Color(0xFFBBF7D0),
                            colorTexto = Color(0xFF16A34A),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (misTotal == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no tienes reportes enviados. Toca 'Ir al mapa' para crear el primero.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // =====================================================
        // 5. AYUDA PARA PERSONAS CON POCA EXPERIENCIA / MAYORES
        // =====================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { ayudaExpandida = !ayudaExpandida },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "¿Necesitas ayuda?",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ver cómo crear un reporte paso a paso",
                                    color = Color(0xFF60A5FA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Icon(
                            imageVector = if (ayudaExpandida) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (ayudaExpandida) "Ocultar ayuda" else "Mostrar ayuda",
                            tint = Color(0xFF94A3B8)
                        )
                    }

                    // Pasos desplegables explicados de forma muy clara
                    AnimatedVisibility(
                        visible = ayudaExpandida,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PasoAyudaItem(numero = "1", texto = "Pulsa el botón 'Ir al mapa'.")
                            PasoAyudaItem(numero = "2", texto = "Pulsa el botón 'Crear reporte'.")
                            PasoAyudaItem(numero = "3", texto = "Marca en el mapa el lugar exacto del problema.")
                            PasoAyudaItem(numero = "4", texto = "Presiona 'Continuar'.")
                            PasoAyudaItem(numero = "5", texto = "Completa los datos del incidente y pulsa 'Crear'.")
                        }
                    }
                }
            }
        }
    }
}

// Subcomponente de paso de ayuda numerado
@Composable
private fun PasoAyudaItem(numero: String, texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFF2563EB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = texto,
            color = Color(0xFFE2E8F0),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

// =====================================================
// COMPONENTE: CONTENIDO INICIO PARA ADMINISTRADOR
// =====================================================

@Composable
private fun InicioAdminContent(
    totalReportes: Int,
    pendientesCount: Int,
    enProcesoCount: Int,
    solucionadosCount: Int,
    totalUsuarios: Int,
    onNavigateToReportes: () -> Unit,
    onNavigateToMapa: () -> Unit,
    onNavigateToUsuarios: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // =====================================================
        // 1. TARJETA HERO ADMINISTRATIVA
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
                                    Color(0xFF1E3A8A), // Azul marino
                                    Color(0xFF3B82F6), // Azul vibrante
                                    Color(0xFF0284C7)  // Celeste profundo
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Panel de Control",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gestión centralizada de incidencias y usuarios de ComuniMap.",
                                    color = Color(0xEEFFFFFF),
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x33FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sub-bloque informativo neutral de la comunidad (sin datos falsos de clima)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x28FFFFFF))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Cochabamba, Bolivia",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Base de datos en tiempo real",
                                        color = Color(0xCCFFFFFF),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "$totalReportes reportes",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================
        // 2. SECCIÓN: ESTADO DEL SISTEMA (TODOS LOS REPORTES)
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
        // 3. ACCESOS RÁPIDOS ADMINISTRATIVOS
        // =====================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gestión del Sistema",
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
                        subtitulo = "$totalReportes totales",
                        icono = Icons.Default.Assignment,
                        colorAcento = Color(0xFF60A5FA),
                        onClick = onNavigateToReportes,
                        modifier = Modifier.weight(1f)
                    )

                    AccesoRapidoCard(
                        titulo = "Mapa",
                        subtitulo = "Ver todos",
                        icono = Icons.Default.Map,
                        colorAcento = Color(0xFF34D399),
                        onClick = onNavigateToMapa,
                        modifier = Modifier.weight(1f)
                    )

                    AccesoRapidoCard(
                        titulo = "Usuarios",
                        subtitulo = "$totalUsuarios registrados",
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

// =====================================================
// COMPONENTES AUXILIARES DE TARJETAS
// =====================================================

@Composable
fun ResumenMiniCard(
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
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cantidad.toString(),
                color = colorNumero,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = titulo,
                color = colorTexto,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AccesoRapidoCard(
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
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorAcento.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorAcento,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = titulo,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
