package com.example.comunimap.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.models.Reporte
import com.example.comunimap.models.Usuario

// Paleta analítica Navy / Slate de ComuniMap
private val PanelBgDark = Color(0xFF0E1A30)
private val DarkCardBg = Color(0xFF132038)
private val InnerTileBg = Color(0xFF0B1426)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentBlueLight = Color(0xFF60A5FA)
private val AccentCyan = Color(0xFF38BDF8)
private val TextMuted = Color(0xFF94A3B8)
private val BorderColor = Color(0xFF233554)

// Colores semánticos de estado
private val ColorPendiente = Color(0xFFF59E0B)       // Ámbar
private val ColorPendienteBg = Color(0xFF78350F)
private val ColorEnProceso = Color(0xFF38BDF8)       // Celeste
private val ColorEnProcesoBg = Color(0xFF0C4A6E)
private val ColorSolucionado = Color(0xFF10B981)     // Verde esmeralda
private val ColorSolucionadoBg = Color(0xFF064E3B)

/**
 * Dashboard analítico y centro de control para el rol ADMINISTRADOR en ComuniMap.
 * Diseñado con estructura modular tipo consola estadística (anillo radial, métricas 2x2,
 * telemetría por estado, ranking por categoría, actividad reciente y accesos tácticos).
 */
@Composable
fun DashboardAdministrador(
    todosLosReportes: List<Reporte>,
    todosLosUsuarios: List<Usuario>,
    onNavigateToReportes: () -> Unit,
    onNavigateToMapa: () -> Unit,
    onNavigateToUsuarios: () -> Unit,
    onNavigateToComunidad: () -> Unit = {}
) {
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

    val usuariosPendientesCount = remember(todosLosUsuarios) {
        todosLosUsuarios.count { !it.estado.equals("Activo", ignoreCase = true) }
    }
    val totalUsuarios = todosLosUsuarios.size
    val usuariosActivosCount = (totalUsuarios - usuariosPendientesCount).coerceAtLeast(0)

    val tasaResolucion = if (totalReportes > 0) {
        ((solucionadosCount.toFloat() / totalReportes.toFloat()) * 100f).toInt()
    } else {
        0
    }

    val categoriasAgrupadas = remember(todosLosReportes) {
        todosLosReportes
            .groupBy { it.categoria?.trim()?.ifBlank { "General" } ?: "General" }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    val actividadReciente = remember(todosLosReportes) {
        todosLosReportes.takeLast(5).reversed()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_administrador_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // =========================================================
        // 1. PANEL PRINCIPAL ANALÍTICO (DONUT RADIAL + TELEMETRÍA)
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_dashboard_admin"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.35f),
                            AccentBlue.copy(alpha = 0.18f)
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF101D36),
                                    Color(0xFF142544)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    // Cabecera compacta del panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ColorSolucionado)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MONITOR COMUNITARIO",
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "Panel de Administración",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = InnerTileBg,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = AccentBlueLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$totalReportes reg.",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Zona central analítica: Indicador circular Donut + Telemetría lateral
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutResumenGauge(
                            tasaResolucion = tasaResolucion,
                            pendientes = pendientesCount,
                            enProceso = enProcesoCount,
                            solucionados = solucionadosCount,
                            total = totalReportes,
                            modifier = Modifier.size(116.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            MiniEstadoTelemetryRow(
                                label = "Solucionados",
                                count = solucionadosCount,
                                total = totalReportes,
                                color = ColorSolucionado
                            )
                            MiniEstadoTelemetryRow(
                                label = "En Proceso",
                                count = enProcesoCount,
                                total = totalReportes,
                                color = ColorEnProceso
                            )
                            MiniEstadoTelemetryRow(
                                label = "Pendientes",
                                count = pendientesCount,
                                total = totalReportes,
                                color = ColorPendiente
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.7f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Franja inferior de resumen del sistema (solo datos reales)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MicroResumenPill(
                            etiqueta = "Usuarios",
                            valor = "$totalUsuarios",
                            colorAcento = AccentBlueLight,
                            modifier = Modifier.weight(1f)
                        )
                        MicroResumenPill(
                            etiqueta = "Activos",
                            valor = "$usuariosActivosCount",
                            colorAcento = ColorSolucionado,
                            modifier = Modifier.weight(1f)
                        )
                        MicroResumenPill(
                            etiqueta = "Categorías",
                            valor = "${categoriasAgrupadas.size}",
                            colorAcento = AccentCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // =========================================================
        // 2. ACCESOS RÁPIDOS: COMUNIDAD | VALIDACIÓN DE USUARIOS
        // =========================================================
        item {
            val hayPendientes = usuariosPendientesCount > 0
            val accentAlert = if (hayPendientes) Color(0xFFF59E0B) else AccentBlueLight

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .testTag("row_accesos_rapidos_admin"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccesoRapidoDashboardCard(
                    titulo = "Comunidad",
                    subtitulo = "Avisos y encuestas",
                    icono = Icons.Default.Groups,
                    colorAcento = AccentCyan,
                    colorFondo = DarkCardBg,
                    colorBorde = BorderColor,
                    badgeTexto = null,
                    colorSubtitulo = TextMuted,
                    testTag = "btn_acceso_comunidad_admin",
                    onClick = onNavigateToComunidad,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                AccesoRapidoDashboardCard(
                    titulo = "Validación de Usuarios",
                    subtitulo = if (hayPendientes) {
                        "$usuariosPendientesCount por aprobar"
                    } else {
                        "$totalUsuarios cuentas activas"
                    },
                    icono = if (hayPendientes) Icons.Default.WarningAmber else Icons.Default.People,
                    colorAcento = accentAlert,
                    colorFondo = if (hayPendientes) Color(0xFF221A12) else DarkCardBg,
                    colorBorde = if (hayPendientes) Color(0xFFF59E0B).copy(alpha = 0.6f) else BorderColor,
                    badgeTexto = if (hayPendientes) "$usuariosPendientesCount" else null,
                    colorSubtitulo = if (hayPendientes) Color(0xFFFDE68A) else TextMuted,
                    testTag = "btn_revisar_usuarios_admin",
                    onClick = onNavigateToUsuarios,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // =========================================================
        // 3. SECCIÓN: ESTADO DEL SISTEMA (CUADRÍCULA KPI 2x2)
        // =========================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado del Sistema",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tiempo real",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaEstadisticaItem(
                        titulo = "TOTAL REPORTES",
                        cantidad = totalReportes,
                        totalReferencia = totalReportes.coerceAtLeast(1),
                        subtitulo = "Historial completo",
                        icono = Icons.Default.FolderOpen,
                        colorAcento = AccentBlueLight,
                        colorFondo = Color(0xFF111E36),
                        testTag = "card_stat_total",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    TarjetaEstadisticaItem(
                        titulo = "PENDIENTES",
                        cantidad = pendientesCount,
                        totalReferencia = totalReportes,
                        subtitulo = "${porcentajeSeguro(pendientesCount, totalReportes)}% del total",
                        icono = Icons.Default.Schedule,
                        colorAcento = Color(0xFFFBBF24),
                        colorFondo = Color(0xFF1D1A18),
                        testTag = "card_stat_pendientes",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaEstadisticaItem(
                        titulo = "EN PROCESO",
                        cantidad = enProcesoCount,
                        totalReferencia = totalReportes,
                        subtitulo = "${porcentajeSeguro(enProcesoCount, totalReportes)}% del total",
                        icono = Icons.Default.Autorenew,
                        colorAcento = AccentCyan,
                        colorFondo = Color(0xFF0F2036),
                        testTag = "card_stat_en_proceso",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    TarjetaEstadisticaItem(
                        titulo = "SOLUCIONADOS",
                        cantidad = solucionadosCount,
                        totalReferencia = totalReportes,
                        subtitulo = "${porcentajeSeguro(solucionadosCount, totalReportes)}% del total",
                        icono = Icons.Default.CheckCircle,
                        colorAcento = Color(0xFF34D399),
                        colorFondo = Color(0xFF0E2321),
                        testTag = "card_stat_solucionados",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        // =========================================================
        // 4. WIDGET ANALÍTICO: DISTRIBUCIÓN POR ESTADO
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_grafico_estados"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reportes por estado",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Balance operativo de incidencias",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }

                        if (totalReportes > 0) {
                            // Mini indicador de columnas verticales inspirado en consola analítica
                            MiniColumnEqualizer(
                                pendientes = pendientesCount,
                                enProceso = enProcesoCount,
                                solucionados = solucionadosCount,
                                total = totalReportes
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (totalReportes == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay reportes registrados para graficar.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Barra segmentada continua de distribución global
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(InnerTileBg)
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

                        Spacer(modifier = Modifier.height(14.dp))

                        BarraHorizontalMetrica(
                            etiqueta = "Pendiente",
                            cantidad = pendientesCount,
                            total = totalReportes,
                            colorBarra = ColorPendiente,
                            colorEtiqueta = Color(0xFFFEF3C7)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        BarraHorizontalMetrica(
                            etiqueta = "En Proceso",
                            cantidad = enProcesoCount,
                            total = totalReportes,
                            colorBarra = ColorEnProceso,
                            colorEtiqueta = Color(0xFFBAE6FD)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

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
        // 5. WIDGET: REPORTES POR CATEGORÍA (DATOS REALES)
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_grafico_categorias"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                        Column {
                            Text(
                                text = "Reportes por categoría",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clasificación real ordenada por frecuencia",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = InnerTileBg,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text(
                                text = "${categoriasAgrupadas.size} tipos",
                                color = AccentBlueLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoriasAgrupadas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
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
                            categoriasAgrupadas.forEachIndexed { idx, (categoria, cantidad) ->
                                ItemCategoriaGrafico(
                                    indice = idx + 1,
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
        // 6. ACTIVIDAD RECIENTE (ESTILO BITÁCORA COMPACTA)
        // =========================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_actividad_reciente"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                        Column {
                            Text(
                                text = "Actividad reciente",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Últimas incidencias registradas",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }

                        TextButton(
                            onClick = onNavigateToReportes,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("btn_ver_mas_actividad_admin")
                        ) {
                            Text(
                                text = "Ver todas",
                                color = AccentBlueLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (actividadReciente.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay reportes registrados.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            actividadReciente.forEach { reporte ->
                                ItemReporteReciente(reporte = reporte)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================
        // 7. GESTIÓN DEL SISTEMA (ACCESOS TÁCTICOS)
        // =========================================================
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Gestión del Sistema",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccesoModuloCard(
                        titulo = "Reportes",
                        subtitulo = "$totalReportes registrados",
                        icono = Icons.Default.Assignment,
                        colorAcento = AccentBlueLight,
                        onClick = onNavigateToReportes,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    AccesoModuloCard(
                        titulo = "Mapa",
                        subtitulo = "Vista territorial",
                        icono = Icons.Default.Map,
                        colorAcento = Color(0xFF34D399),
                        onClick = onNavigateToMapa,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    AccesoModuloCard(
                        titulo = "Usuarios",
                        subtitulo = "$totalUsuarios vecinos",
                        icono = Icons.Default.People,
                        colorAcento = Color(0xFFA78BFA),
                        onClick = onNavigateToUsuarios,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENTES ANALÍTICOS DEL CENTRO DE CONTROL
// -------------------------------------------------------------

private fun porcentajeSeguro(cantidad: Int, total: Int): Int {
    return if (total > 0) ((cantidad.toFloat() / total.toFloat()) * 100f).toInt() else 0
}

@Composable
private fun DonutResumenGauge(
    tasaResolucion: Int,
    pendientes: Int,
    enProceso: Int,
    solucionados: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 11.dp.toPx()
            val inset = strokePx / 2f + 2.dp.toPx()
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)

            // Pista base del anillo exterior
            drawArc(
                color = Color(0xFF1E2E4E),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Anillo interior decorativo fino estilo consola técnica
            val innerInset = inset + strokePx * 0.9f
            if (size.width > innerInset * 2f) {
                drawArc(
                    color = Color(0xFF233860).copy(alpha = 0.65f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(innerInset, innerInset),
                    size = Size(size.width - innerInset * 2f, size.height - innerInset * 2f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            if (total > 0) {
                val segments = listOf(
                    solucionados to ColorSolucionado,
                    enProceso to ColorEnProceso,
                    pendientes to ColorPendiente
                ).filter { it.first > 0 }

                val gapAngle = if (segments.size > 1) 6f else 0f
                val totalGap = gapAngle * segments.size
                val availableAngle = (360f - totalGap).coerceAtLeast(0f)

                var startAngle = -90f
                segments.forEach { (count, color) ->
                    val sweep = (count.toFloat() / total.toFloat()) * availableAngle
                    if (sweep > 0.5f) {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweep + gapAngle
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$tasaResolucion%",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 25.sp
            )
            Text(
                text = "RESUELTO",
                color = AccentCyan,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.9.sp
            )
        }
    }
}

@Composable
private fun MiniEstadoTelemetryRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) (count.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val pct = porcentajeSeguro(count, total)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$count",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($pct%)",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFF0A1222))
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceAtLeast(0.04f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun MicroResumenPill(
    etiqueta: String,
    valor: String,
    colorAcento: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(InnerTileBg)
            .border(1.dp, BorderColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            color = TextMuted,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = valor,
            color = colorAcento,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MiniColumnEqualizer(
    pendientes: Int,
    enProceso: Int,
    solucionados: Int,
    total: Int
) {
    val maxVal = maxOf(pendientes, enProceso, solucionados, 1).toFloat()
    val bars = listOf(
        (pendientes / maxVal).coerceIn(0.18f, 1f) to ColorPendiente,
        (enProceso / maxVal).coerceIn(0.18f, 1f) to ColorEnProceso,
        (solucionados / maxVal).coerceIn(0.18f, 1f) to ColorSolucionado,
        ((solucionados + enProceso).toFloat() / maxOf(total, 1).toFloat()).coerceIn(0.18f, 1f) to AccentBlueLight
    )

    Canvas(
        modifier = Modifier
            .width(46.dp)
            .height(26.dp)
    ) {
        val barWidth = 6.dp.toPx()
        val gap = (size.width - barWidth * bars.size) / (bars.size - 1).coerceAtLeast(1)
        bars.forEachIndexed { idx, (ratio, color) ->
            val x = idx * (barWidth + gap)
            // Fondo de columna
            drawRoundRect(
                color = Color(0xFF0A1222),
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            // Valor proporcional
            val h = size.height * ratio
            drawRoundRect(
                color = color,
                topLeft = Offset(x, size.height - h),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    }
}

/**
 * Tarjeta KPI compacta con estilo de módulo analítico, número prominente y barra de proporción inferior.
 */
@Composable
private fun TarjetaEstadisticaItem(
    titulo: String,
    cantidad: Int,
    totalReferencia: Int,
    subtitulo: String,
    icono: ImageVector,
    colorAcento: Color,
    colorFondo: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val fraction = if (totalReferencia > 0) {
        (cantidad.toFloat() / totalReferencia.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorAcento.copy(alpha = 0.16f))
                        .border(1.dp, colorAcento.copy(alpha = 0.28f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = cantidad.toString(),
                color = Color.White,
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = subtitulo,
                    color = TextMuted,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceAtLeast(0.05f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(colorAcento)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraHorizontalMetrica(
    etiqueta: String,
    cantidad: Int,
    total: Int,
    colorBarra: Color,
    colorEtiqueta: Color
) {
    val porcentaje = porcentajeSeguro(cantidad, total)
    val fraccionBarra = if (total > 0) (cantidad.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colorBarra)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = etiqueta,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$cantidad",
                    color = colorEtiqueta,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colorBarra.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "$porcentaje%",
                        color = colorEtiqueta,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(InnerTileBg)
        ) {
            if (fraccionBarra > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccionBarra)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(colorBarra)
                )
            }
        }
    }
}

@Composable
private fun ItemCategoriaGrafico(
    indice: Int,
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
    val porcentajeTotal = porcentajeSeguro(cantidad, totalGeneral)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = InnerTileBg,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text(
                        text = String.format("%02d", indice),
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoria,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$cantidad",
                    color = AccentBlueLight,
                    fontSize = 12.5.sp,
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

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(InnerTileBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2563EB),
                                Color(0xFF38BDF8)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ItemReporteReciente(reporte: Reporte) {
    val estadoColor = when (reporte.estado.lowercase()) {
        "pendiente" -> ColorPendiente
        "en proceso" -> ColorEnProceso
        "solucionado" -> ColorSolucionado
        else -> TextMuted
    }

    val estadoBg = when (reporte.estado.lowercase()) {
        "pendiente" -> ColorPendienteBg.copy(alpha = 0.42f)
        "en proceso" -> ColorEnProcesoBg.copy(alpha = 0.42f)
        "solucionado" -> ColorSolucionadoBg.copy(alpha = 0.42f)
        else -> Color(0xFF1E293B)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = InnerTileBg,
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 11.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(estadoColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reporte.titulo,
                        color = Color.White,
                        fontSize = 13.5.sp,
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
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reporte.categoria ?: "General",
                        color = AccentBlueLight,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val direccion = reporte.direccion?.takeIf { it.isNotBlank() } ?: reporte.ubicacion
                    if (!direccion.isNullOrBlank()) {
                        Text(
                            text = " • ",
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = direccion,
                            color = TextMuted,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
    }
}

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
            .clip(RoundedCornerShape(15.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colorAcento.copy(alpha = 0.16f))
                        .border(1.dp, colorAcento.copy(alpha = 0.26f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitulo,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AccesoRapidoDashboardCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorAcento: Color,
    colorFondo: Color,
    colorBorde: Color,
    badgeTexto: String?,
    colorSubtitulo: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        border = BorderStroke(1.dp, colorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorAcento.copy(alpha = 0.16f))
                        .border(1.dp, colorAcento.copy(alpha = 0.28f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!badgeTexto.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colorAcento
                        ) {
                            Text(
                                text = badgeTexto,
                                color = Color(0xFF1E1B4B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Abrir $titulo",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitulo,
                    color = colorSubtitulo,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

