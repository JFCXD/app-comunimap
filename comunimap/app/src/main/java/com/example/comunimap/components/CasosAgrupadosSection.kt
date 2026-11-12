package com.example.comunimap.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.models.CasoAgrupado

// =====================================================
// COLORES SEMÁNTICOS DE PRIORIDAD (ADAPTABLES A TEMA)
// =====================================================

// Prioridad Alta (Rojo)
val ColorPrioridadAlta = Color(0xFFEF4444)
val ColorPrioridadAltaContainer = Color(0xFF7F1D1D).copy(alpha = 0.25f)

// Prioridad Media (Ámbar / Naranja)
val ColorPrioridadMedia = Color(0xFFF59E0B)
val ColorPrioridadMediaContainer = Color(0xFF78350F).copy(alpha = 0.25f)

// Prioridad Baja (Azul)
val ColorPrioridadBaja = Color(0xFF3B82F6)
val ColorPrioridadBajaContainer = Color(0xFF1E3A8A).copy(alpha = 0.25f)

/**
 * Encabezado de sección para casos comunitarios agrupados por prioridad.
 */
@Composable
fun SeccionCasosAgrupadosHeader(
    titulo: String,
    cantidad: Int,
    subtitulo: String,
    colorAcento: Color,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorAcento.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorAcento,
                    letterSpacing = 0.6.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorAcento.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "$cantidad caso${if (cantidad != 1) "s" else ""}",
                    color = colorAcento,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

/**
 * Tarjeta individual para mostrar un caso agrupado según prioridad comunitaria.
 * Incluye selector de estado y botón de guardado exclusivo para Administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasoAgrupadoCard(
    caso: CasoAgrupado,
    esAdministrador: Boolean = false,
    isProcesando: Boolean = false,
    onCambiarEstado: (nuevoEstado: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val prioridadNormalizada = caso.prioridad.trim().lowercase()

    val (colorAcento, colorFondoBadge, iconoPrioridad) = when (prioridadNormalizada) {
        "alta" -> Triple(ColorPrioridadAlta, ColorPrioridadAltaContainer, Icons.Default.Warning)
        "media" -> Triple(ColorPrioridadMedia, ColorPrioridadMediaContainer, Icons.Default.WarningAmber)
        else -> Triple(ColorPrioridadBaja, ColorPrioridadBajaContainer, Icons.Default.Info)
    }

    var estadoSeleccionado by remember(caso.idReporteFormateado) {
        mutableStateOf("En Proceso")
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("caso_agrupado_card_${caso.idReporteFormateado}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Fila 1: Título del problema y Badge de Prioridad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caso.titulo.ifBlank { "Incidencia Comunitaria" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorFondoBadge,
                    border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = iconoPrioridad,
                            contentDescription = null,
                            tint = colorAcento,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Prioridad: ${caso.prioridad.ifBlank { "Normal" }}",
                            fontWeight = FontWeight.Bold,
                            color = colorAcento,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 2: Categoría
            if (caso.categoria.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Categoría: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = caso.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Fila 3: Dirección / Ubicación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ubicación: ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (!caso.direccion.isNullOrBlank()) caso.direccion else "Sin dirección especificada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Fila 4: Bloques métricos (Vecinos y Reportes agrupados)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Métrica: Vecinos involucrados
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = colorAcento,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${caso.cantidadVecinos} vecino${if (caso.cantidadVecinos != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Métrica: Reportes agrupados (radio 5m)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${caso.cantidadReportes} reporte${if (caso.cantidadReportes != 1) "s" else ""} agrupado${if (caso.cantidadReportes != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Fila 5: Selector de Estado y Botón Guardar (Exclusivo Administrador)
            if (esAdministrador) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.8.dp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Cambiar estado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector Desplegable de Estados (Pendiente, En Proceso, Solucionado)
                    Box(modifier = Modifier.weight(1.15f)) {
                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown && !isProcesando,
                            onExpandedChange = { if (!isProcesando) expandedDropdown = !expandedDropdown }
                        ) {
                            OutlinedTextField(
                                value = estadoSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                enabled = !isProcesando,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                                },
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                singleLine = true,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_estado_caso_${caso.idReporteFormateado}")
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDropdown && !isProcesando,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                val estadosDisponibles = listOf("Pendiente", "En Proceso", "Solucionado")
                                estadosDisponibles.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = opcion,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (opcion == estadoSeleccionado) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            estadoSeleccionado = opcion
                                            expandedDropdown = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }

                    // Botón Guardar estado
                    Button(
                        onClick = { onCambiarEstado(estadoSeleccionado) },
                        enabled = !isProcesando,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorAcento,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(0.95f)
                            .testTag("btn_guardar_estado_caso_${caso.idReporteFormateado}")
                    ) {
                        if (isProcesando) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Guardando...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardar estado",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
