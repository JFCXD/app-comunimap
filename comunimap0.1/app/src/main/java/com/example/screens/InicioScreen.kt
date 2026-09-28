package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.models.Categoria
import com.example.models.Reporte
import com.example.utils.Constantes

// ====================================================================================================
// 📊 PANTALLA: INICIO / DASHBOARD PRINCIPAL (ComuniMap)
// ====================================================================================================
// Esta pantalla muestra el resumen ejecutivo de la plataforma ciudadana:
// 1. Tarjeta superior de bienvenida con el Widget de Clima Barrial.
// 2. Métricas del sistema en tiempo real (Total, Pendientes, En Proceso, Resueltos).
// 3. Catálogo horizontal de Categorías de servicios públicos.
// 4. Lista de incidentes prioritarios y recientes con acceso a su detalle.
//
// 📌 ¿CÓMO CONECTAR EL CLIMA CON UNA API EXTERNA REAL (OPENWEATHER / OPEN-METEO)?
// ----------------------------------------------------------------------------------------------------
// 1. Con Open-Meteo (Gratuita sin API key):
//    `https://api.open-meteo.com/v1/forecast?latitude=19.4326&longitude=-99.1332&current_weather=true`
// 2. Con OpenWeatherMap (Requiere API Key):
//    `https://api.openweathermap.org/data/2.5/weather?lat=19.4326&lon=-99.1332&units=metric&appid=TU_KEY`
// 3. Creas una interfaz Retrofit para deserializar la temperatura (`current_weather.temperature`),
//    la humedad y el estado del cielo, pasando dichos valores a la sección del clima más abajo.
// ====================================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    reportes: List<Reporte>,
    categorias: List<Categoria>,
    estaConectadoApi: Boolean?,
    estaCargando: Boolean = false,
    onRefrescar: () -> Unit = {},
    onNavigateToMapa: () -> Unit,
    onNavigateToReportes: () -> Unit,
    onSelectReporte: (Reporte) -> Unit,
    onCrearReporteClick: () -> Unit
) {
    // ------------------------------------------------------------------------------------------------
    // 📈 CÁLCULO DE MÉTRICAS A PARTIR DEL LISTADO DE REPORTES
    // ------------------------------------------------------------------------------------------------
    val totalReportes = reportes.size
    val pendientes = reportes.count { it.estado.equals("Pendiente", ignoreCase = true) }
    val enProceso = reportes.count { it.estado.equals("En Proceso", ignoreCase = true) }
    val solucionados = reportes.count { it.estado.equals("Solucionado", ignoreCase = true) || it.estado.equals("Resuelto", ignoreCase = true) }
    val urgentes = reportes.filter { it.prioridad.equals("Urgente", ignoreCase = true) || it.prioridad.equals("Alta", ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo oficial de ComuniMap en la barra superior
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(4.dp, shape = CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.comunimap_logo),
                                contentDescription = "Logo ComuniMap",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ComuniMap",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "MAPA INTERACTIVO • Tu Barrio",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Botón de sincronización manual con el servidor
                    IconButton(
                        onClick = onRefrescar,
                        modifier = Modifier.testTag("btn_refrescar_inicio")
                    ) {
                        if (estaCargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refrescar datos",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // Botón flotante para levantar un nuevo reporte ciudadano
            ExtendedFloatingActionButton(
                onClick = onCrearReporteClick,
                icon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) },
                text = { Text("Nuevo Reporte", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_nuevo_reporte_inicio")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ----------------------------------------------------------------------------------------
            // 🌤️ SECCIÓN 1: BANNER DE BIENVENIDA Y WIDGET DE CLIMA BARRIAL
            // ----------------------------------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "¡Hola, Vecino!",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Reporta incidencias y mejora tu comunidad.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = "Clima",
                                        tint = Color(0xFFFDE047),
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ☀️ WIDGET DEL CLIMA BARRIAL
                            // Modifica estos valores o vincula las variables desde tu ViewModel / API de Clima
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Temperatura y Condición del Cielo
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "22°C",
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Parcialmente Nublado",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Clima Barrial Actual",
                                                    color = Color.White.copy(alpha = 0.75f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Indicadores Meteorológicos: Humedad y Viento
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.WaterDrop,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Humedad: 54%",
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 11.sp
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Air,
                                                contentDescription = null,
                                                tint = Color(0xFF93C5FD),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Viento: 11 km/h",
                                                color = Color.White.copy(alpha = 0.9f),
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

            // ----------------------------------------------------------------------------------------
            // 📊 SECCIÓN 2: MÉTRICAS DEL ESTADO DEL SISTEMA (TARJETAS NUMÉRICAS)
            // ----------------------------------------------------------------------------------------
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Estado del Sistema",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tarjeta Total
                        MetricaCard(
                            titulo = "Total",
                            valor = totalReportes.toString(),
                            colorFondo = Color(0xFFEEF2FF),
                            colorTexto = Color(0xFF3730A3),
                            icono = Icons.Default.FolderOpen,
                            modifier = Modifier.weight(1f)
                        )

                        // Tarjeta Pendientes
                        MetricaCard(
                            titulo = "Pendientes",
                            valor = pendientes.toString(),
                            colorFondo = Color(0xFFFEF3C7),
                            colorTexto = Color(0xFF92400E),
                            icono = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )

                        // Tarjeta En Proceso
                        MetricaCard(
                            titulo = "En Proceso",
                            valor = enProceso.toString(),
                            colorFondo = Color(0xFFDBEAFE),
                            colorTexto = Color(0xFF1E40AF),
                            icono = Icons.Default.Autorenew,
                            modifier = Modifier.weight(1f)
                        )

                        // Tarjeta Solucionados
                        MetricaCard(
                            titulo = "Solucionados",
                            valor = solucionados.toString(),
                            colorFondo = Color(0xFFDCFCE7),
                            colorTexto = Color(0xFF166534),
                            icono = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ----------------------------------------------------------------------------------------
            // 🏷️ SECCIÓN 3: CATEGORÍAS DE INCIDENCIAS
            // ----------------------------------------------------------------------------------------
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Categorías de Incidencias",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(categorias) { categoria ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                runCatching { Color(android.graphics.Color.parseColor(categoria.colorHex)) }
                                                    .getOrDefault(Color(0xFF2563EB)).copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (categoria.icono) {
                                                "car" -> Icons.Default.DirectionsCar
                                                "light" -> Icons.Default.Lightbulb
                                                "water" -> Icons.Default.WaterDrop
                                                "trash" -> Icons.Default.DeleteOutline
                                                "shield" -> Icons.Default.Security
                                                "park" -> Icons.Default.Park
                                                else -> Icons.Default.Warning
                                            },
                                            contentDescription = null,
                                            tint = runCatching { Color(android.graphics.Color.parseColor(categoria.colorHex)) }
                                                .getOrDefault(Color(0xFF2563EB)),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = categoria.nombre,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    val count = reportes.count { it.categoriaId == categoria.id }
                                    Text(
                                        text = "$count reportes",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------------------------------------------
            // 🚨 SECCIÓN 4: ALERTAS RECIENTES Y PRIORITARIAS
            // ----------------------------------------------------------------------------------------
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alertas Recientes & Prioritarias",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToReportes) {
                        Text("Ver todas (${reportes.size})", fontSize = 12.sp)
                    }
                }
            }

            items(reportes.take(4)) { reporte ->
                TarjetaReporteResumen(
                    reporte = reporte,
                    onClick = { onSelectReporte(reporte) }
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------------------------------
// 🧩 COMPONENTE REUTILIZABLE: TARJETA DE MÉTRICA INDIVIDUAL
// ----------------------------------------------------------------------------------------------------
@Composable
fun MetricaCard(
    titulo: String,
    valor: String,
    colorFondo: Color,
    colorTexto: Color,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colorFondo,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colorTexto)
            Text(text = titulo, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = colorTexto.copy(alpha = 0.8f))
        }
    }
}

// ----------------------------------------------------------------------------------------------------
// 🧩 COMPONENTE REUTILIZABLE: FILA RESUMEN DE REPORTE
// ----------------------------------------------------------------------------------------------------
@Composable
fun TarjetaReporteResumen(
    reporte: Reporte,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Constantes.obtenerColorEstado(reporte.estado).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (reporte.categoriaId) {
                        1 -> Icons.Default.DirectionsCar
                        2 -> Icons.Default.Lightbulb
                        3 -> Icons.Default.WaterDrop
                        4 -> Icons.Default.DeleteOutline
                        5 -> Icons.Default.Security
                        else -> Icons.Default.Place
                    },
                    contentDescription = null,
                    tint = Constantes.obtenerColorEstado(reporte.estado),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reporte.titulo,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reporte.direccion ?: "GPS: ${reporte.ubicacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge de Estado
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Constantes.obtenerColorEstado(reporte.estado).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = reporte.estado,
                            color = Constantes.obtenerColorEstado(reporte.estado),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Badge de Prioridad
                    if (reporte.prioridad.equals("Urgente", ignoreCase = true) || reporte.prioridad.equals("Alta", ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Constantes.obtenerColorPrioridad(reporte.prioridad).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Prioridad ${reporte.prioridad}",
                                color = Constantes.obtenerColorPrioridad(reporte.prioridad),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = reporte.creadoEn,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
