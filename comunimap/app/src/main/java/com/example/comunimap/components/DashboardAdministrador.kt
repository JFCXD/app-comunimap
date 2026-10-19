package com.example.comunimap.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.models.Reporte
import com.example.comunimap.models.Usuario

// Colores del tema visual Navy de ComuniMap
private val DarkCardBg = Color(0xFF162238)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentBlueLight = Color(0xFF60A5FA)
private val TextMuted = Color(0xFF94A3B8)
private val BorderColor = Color(0xFF334155)

// Colores semánticos de estado
private val ColorPendiente = Color(0xFFF59E0B)       // Ámbar
private val ColorPendienteBg = Color(0xFF78350F)
private val ColorEnProceso = Color(0xFF0284C7)        // Celeste / Azul
private val ColorEnProcesoBg = Color(0xFF0C4A6E)
private val ColorSolucionado = Color(0xFF10B981)      // Verde esmeralda
private val ColorSolucionadoBg = Color(0xFF064E3B)

/**
 * Dashboard analítico y estadístico exclusivo para el rol de ADMINISTRADOR en ComuniMap.
 *
 * Características:
 * - Tarjetas estadísticas 2x2 para distribución de estados (Total, Pendientes, En Proceso, Solucionados).
 * - Bloque destacado de Usuarios pendientes de validación con acceso directo a UsuariosScreen.
 * - Gráfico horizontal proporcional de "Reportes por estado".
 * - Gráfico horizontal ordenado de "Reportes por categoría" con datos reales.
 * - Lista de "Actividad reciente" con los últimos reportes recibidos.
 * - Accesos rápidos a módulos de gestión (Reportes, Mapa, Usuarios).
 * - Manejo robusto de estados vacíos y división segura entre cero.
 */
@Composable
fun DashboardAdministrador(
    todosLosReportes: List<Reporte>,
    todosLosUsuarios: List<Usuario>,
    onNavigateToReportes: () -> Unit,
    onNavigateToMapa: () -> Unit,
    onNavigateToUsuarios: () -> Unit
) {
    // -------------------------------------------------------------
    // CÁLCULOS ESTADÍSTICOS EN BASE A DATOS REALES
    // -------------------------------------------------------------
    val totalReportes = todosLosReportes.size
    val pendientesCount = remember(todosLosReportes) {
        todosLosReportes.count { it.estado.equals("Pendiente", ignoreCase = true) }
    }
    val enProcesoCount = remember(todosLosReportes) {
        todosLosReportes.count { it.estado.equals("En Proceso", ignoreCase = true) }
    }
    val solucionadosCount = remember(todosLosReportes) {
        todosLosReportes.count { it.estado.equals("Solucionado", ignoreCase = true) }
    }

    // Un usuario se considera pendiente si su estado no es "Activo"
    val usuariosPendientesCount = remember(todosLosUsuarios) {
        todosLosUsuarios.count { !it.estado.equals("Activo", ignoreCase = true) }
    }
    val totalUsuarios = todosLosUsuarios.size

    // Tasa de resolución porcentual segura (evita división entre cero)
    val tasaResolucion = if (totalReportes > 0) {
        ((solucionadosCount.toFloat() / totalReportes.toFloat()) * 100f).toInt()
    } else {
        0
    }

    // Agrupación real de categorías ordenada de mayor a menor
    val categoriasAgrupadas = remember(todosLosReportes) {
        todosLosReportes
            .groupBy { it.categoria?.trim()?.ifBlank { "General" } ?: "General" }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    // Últimos 5 reportes para actividad reciente
    val actividadReciente = remember(todosLosReportes) {
        todosLosReportes.takeLast(5).reversed()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_administrador_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
    ) {
        // =========================================================
        // 1. ENCABEZADO Y TARJETA HERO ANALÍTICA
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_dashboard_admin"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F265C), // Azul navy profundo
                                    Color(0xFF1E40AF), // Azul vibrante
                                    Color(0xFF0284C7)  // Celeste cian
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ComuniMap",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "Panel de Administración",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Resumen general del sistema y métricas comunitarias",
                                    color = Color(0xDDFFFFFF),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
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
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Barra compuesta visual de proporción de estados
                        if (totalReportes > 0) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tasa de resolución",
                                        color = Color(0xDDFFFFFF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$tasaResolucion% resuelto",
                                        color = Color(0xFF67E8F9),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Barra multi-segmento proporcional
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x33000000))
                                ) {
                                    if (pendientesCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(pendientesCount.toFloat())
                                                .fillMaxHeight()
                                                .background(ColorPendiente)
                                        )
                                    }
                                    if (enProcesoCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(enProcesoCount.toFloat())
                                                .fillMaxHeight()
                                                .background(ColorEnProceso)
                                        )
                                    }
                                    if (solucionadosCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .weight(solucionadosCount.toFloat())
                                                .fillMaxHeight()
                                                .background(ColorSolucionado)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Leyenda compacta de la barra
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LeyendaItem(color = ColorPendiente, texto = "Pend: $pendientesCount")
                                    LeyendaItem(color = ColorEnProceso, texto = "Proc: $enProcesoCount")
                                    LeyendaItem(color = ColorSolucionado, texto = "Sol: $solucionadosCount")
                                }
                            }
                        } else {
                            Text(
                                text = "Aún no hay reportes registrados en la comunidad.",
                                color = Color(0xCCFFFFFF),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // =========================================================
        // 2. SECCIÓN: ESTADO DEL SISTEMA (TARJETAS COMPACTAS 2x2)
        // =========================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Estado del Sistema",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Fila 1: Total y Pendientes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaEstadisticaItem(
                        titulo = "Total reportes",
                        cantidad = totalReportes,
                        icono = Icons.Default.FolderOpen,
                        colorAcento = AccentBlueLight,
                        colorFondo = Color(0xFF1E293B),
                        testTag = "card_stat_total",
                        modifier = Modifier.weight(1f)
                    )

                    TarjetaEstadisticaItem(
                        titulo = "Pendientes",
                        cantidad = pendientesCount,
                        icono = Icons.Default.Schedule,
                        colorAcento = ColorPendiente,
                        colorFondo = Color(0xFF2B2113),
                        testTag = "card_stat_pendientes",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fila 2: En Proceso y Solucionados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaEstadisticaItem(
                        titulo = "En Proceso",
                        cantidad = enProcesoCount,
                        icono = Icons.Default.Autorenew,
                        colorAcento = ColorEnProceso,
                        colorFondo = Color(0xFF13283E),
                        testTag = "card_stat_en_proceso",
                        modifier = Modifier.weight(1f)
                    )

                    TarjetaEstadisticaItem(
                        titulo = "Solucionados",
                        cantidad = solucionadosCount,
                        icono = Icons.Default.CheckCircle,
                        colorAcento = ColorSolucionado,
                        colorFondo = Color(0xFF112E23),
                        testTag = "card_stat_solucionados",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // =========================================================
        // 3. TARJETA / BLOQUE: USUARIOS PENDIENTES DE VALIDACIÓN
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_usuarios_pendientes_admin"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (usuariosPendientesCount > 0) Color(0xFF78350F).copy(alpha = 0.4f) else DarkCardBg
                ),
                border = BorderStroke(
                    1.dp,
                    if (usuariosPendientesCount > 0) Color(0xFFF59E0B) else BorderColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (usuariosPendientesCount > 0) Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    else Color(0xFF1E3A8A).copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (usuariosPendientesCount > 0) Icons.Default.WarningAmber else Icons.Default.People,
                                contentDescription = null,
                                tint = if (usuariosPendientesCount > 0) Color(0xFFFBBF24) else AccentBlueLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Validación de Usuarios",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (usuariosPendientesCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF59E0B)
                                    ) {
                                        Text(
                                            text = "$usuariosPendientesCount",
                                            color = Color(0xFF1E1B4B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (usuariosPendientesCount > 0) {
                                    "$usuariosPendientesCount cuenta(s) esperando aprobación"
                                } else {
                                    "No hay usuarios pendientes de validación"
                                },
                                color = if (usuariosPendientesCount > 0) Color(0xFFFDE68A) else TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToUsuarios,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (usuariosPendientesCount > 0) Color(0xFFD97706) else Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_revisar_usuarios_admin")
                    ) {
                        Text(
                            text = if (usuariosPendientesCount > 0) "Revisar" else "Ver",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // =========================================================
        // 4. GRÁFICO: REPORTES POR ESTADO (BARRAS HORIZONTALES)
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_grafico_estados"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Reportes por estado",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Distribución y porcentaje del total de incidencias",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (totalReportes == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay reportes registrados para graficar.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Barra: Pendiente
                        BarraHorizontalMetrica(
                            etiqueta = "Pendiente",
                            cantidad = pendientesCount,
                            total = totalReportes,
                            colorBarra = ColorPendiente,
                            colorEtiqueta = Color(0xFFFEF3C7)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra: En Proceso
                        BarraHorizontalMetrica(
                            etiqueta = "En Proceso",
                            cantidad = enProcesoCount,
                            total = totalReportes,
                            colorBarra = ColorEnProceso,
                            colorEtiqueta = Color(0xFFBAE6FD)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Barra: Solucionado
                        BarraHorizontalMetrica(
                            etiqueta = "Solucionado",
                            cantidad = solucionadosCount,
                            total = totalReportes,
                            colorBarra = ColorSolucionado,
                            colorEtiqueta = Color(0xFFBBF7D0)
                        )
                    }
                }
            }
        }

        // =========================================================
        // 5. GRÁFICO: REPORTES POR CATEGORÍA (DATOS REALES)
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_grafico_categorias"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                text = "Reportes por categoría",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Clasificación real ordenada por frecuencia",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = "${categoriasAgrupadas.size} tipos",
                                color = AccentBlueLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (categoriasAgrupadas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay datos de categorías registrados.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        val maxCount = categoriasAgrupadas.maxOfOrNull { it.second } ?: 1
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            categoriasAgrupadas.forEach { (categoria, cantidad) ->
                                ItemCategoriaGrafico(
                                    categoria = categoria,
                                    cantidad = cantidad,
                                    maxCantidad = maxCount,
                                    totalGeneral = totalReportes
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================
        // 6. ACTIVIDAD RECIENTE (ÚLTIMOS REPORTES DISPONIBLES)
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_actividad_reciente"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                text = "Actividad reciente",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Últimas incidencias registradas en la app",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        TextButton(
                            onClick = onNavigateToReportes,
                            modifier = Modifier.testTag("btn_ver_mas_actividad_admin")
                        ) {
                            Text(
                                text = "Ver todas",
                                color = AccentBlueLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (actividadReciente.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay reportes registrados.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            actividadReciente.forEach { reporte ->
                                ItemReporteReciente(reporte = reporte)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================
        // 7. ACCESOS RÁPIDOS A MÓDULOS DE GESTIÓN
        // =========================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gestión del Sistema",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccesoModuloCard(
                        titulo = "Reportes",
                        subtitulo = "$totalReportes registrados",
                        icono = Icons.Default.Assignment,
                        colorAcento = AccentBlueLight,
                        onClick = onNavigateToReportes,
                        modifier = Modifier.weight(1f)
                    )

                    AccesoModuloCard(
                        titulo = "Mapa Comunitario",
                        subtitulo = "Todos los incidentes",
                        icono = Icons.Default.Map,
                        colorAcento = Color(0xFF34D399),
                        onClick = onNavigateToMapa,
                        modifier = Modifier.weight(1f)
                    )

                    AccesoModuloCard(
                        titulo = "Usuarios",
                        subtitulo = "$totalUsuarios vecinos",
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

// -------------------------------------------------------------
// COMPONENTES AUXILIARES DEL DASHBOARD
// -------------------------------------------------------------

@Composable
private fun LeyendaItem(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            color = Color(0xEEFFFFFF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Tarjeta individual de estadística compacta con número grande e icono claro.
 */
@Composable
private fun TarjetaEstadisticaItem(
    titulo: String,
    cantidad: Int,
    icono: ImageVector,
    colorAcento: Color,
    colorFondo: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorAcento.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = cantidad.toString(),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = titulo,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Barra horizontal para representar métricas de estado con proporción relativa.
 */
@Composable
private fun BarraHorizontalMetrica(
    etiqueta: String,
    cantidad: Int,
    total: Int,
    colorBarra: Color,
    colorEtiqueta: Color
) {
    val porcentaje = if (total > 0) {
        ((cantidad.toFloat() / total.toFloat()) * 100f).toInt()
    } else {
        0
    }

    val fraccionBarra = if (total > 0) {
        (cantidad.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorBarra)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = etiqueta,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "$cantidad  ($porcentaje%)",
                color = colorEtiqueta,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Contenedor de la barra
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F172A))
        ) {
            if (fraccionBarra > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccionBarra)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorBarra)
                )
            }
        }
    }
}

/**
 * Elemento de categoría con barra horizontal relativa al máximo volumen.
 */
@Composable
private fun ItemCategoriaGrafico(
    categoria: String,
    cantidad: Int,
    maxCantidad: Int,
    totalGeneral: Int
) {
    val fraccion = if (maxCantidad > 0) {
        (cantidad.toFloat() / maxCantidad.toFloat()).coerceIn(0.05f, 1f)
    } else {
        0.05f
    }

    val porcentajeTotal = if (totalGeneral > 0) {
        ((cantidad.toFloat() / totalGeneral.toFloat()) * 100f).toInt()
    } else {
        0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoria,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$cantidad",
                    color = AccentBlueLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($porcentajeTotal%)",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F172A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2563EB),
                                Color(0xFF60A5FA)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Tarjeta individual para reportes dentro de "Actividad reciente".
 */
@Composable
private fun ItemReporteReciente(reporte: Reporte) {
    val estadoColor = when (reporte.estado.lowercase()) {
        "pendiente" -> ColorPendiente
        "en proceso" -> ColorEnProceso
        "solucionado" -> ColorSolucionado
        else -> TextMuted
    }

    val estadoBg = when (reporte.estado.lowercase()) {
        "pendiente" -> ColorPendienteBg.copy(alpha = 0.4f)
        "en proceso" -> ColorEnProcesoBg.copy(alpha = 0.4f)
        "solucionado" -> ColorSolucionadoBg.copy(alpha = 0.4f)
        else -> Color(0xFF1E293B)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reporte.titulo,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = estadoBg,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = reporte.estado,
                        color = estadoColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Categoría
                Text(
                    text = reporte.categoria ?: "General",
                    color = AccentBlueLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                val direccion = reporte.direccion?.takeIf { it.isNotBlank() } ?: reporte.ubicacion
                if (!direccion.isNullOrBlank()) {
                    Text(
                        text = " • ",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = direccion,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de acceso rápido a módulos administrativos.
 */
@Composable
private fun AccesoModuloCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorAcento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = titulo,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
