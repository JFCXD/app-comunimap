package com.example.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.Categoria
import com.example.models.CrearReporteRequest
import com.example.models.Reporte
import com.example.utils.Constantes

// =====================================================
// PANTALLA: REPORTES (Gestión y Listado de Alertas)
// =====================================================
// Permite buscar, filtrar por categoría o estado real
// (Pendiente, En Proceso, Solucionado), ver detalles y
// registrar nuevos reportes con crear_reporte.php.
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    reportes: List<Reporte>,
    categorias: List<Categoria>,
    reporteSeleccionado: Reporte?,
    estaCargando: Boolean = false,
    onRefrescar: () -> Unit = {},
    onSelectReporte: (Reporte?) -> Unit,
    onCrearReporte: (CrearReporteRequest) -> Unit,
    mostrarDialogoNuevo: Boolean,
    onDismissDialogoNuevo: () -> Unit,
    onAbrirDialogoNuevo: () -> Unit,
    usuarioActualId: Int = 1
) {
    var busqueda by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf("Todos") }
    var categoriaSeleccionadaId by remember { mutableStateOf<Int?>(null) }
    var mostrarBusqueda by remember { mutableStateOf(false) }

    val reportesFiltrados = remember(reportes, busqueda, estadoSeleccionado, categoriaSeleccionadaId) {
        reportes.filter { rep ->
            val coincideTexto = busqueda.isBlank() ||
                    rep.titulo.contains(busqueda, ignoreCase = true) ||
                    rep.descripcion.contains(busqueda, ignoreCase = true) ||
                    (rep.direccion?.contains(busqueda, ignoreCase = true) == true) ||
                    (rep.categoriaNombre?.contains(busqueda, ignoreCase = true) == true)

            val coincideEstado = when (estadoSeleccionado) {
                "Todos" -> true
                "Pendientes" -> rep.estado.equals("Pendiente", ignoreCase = true)
                "En Proceso" -> rep.estado.equals("En Proceso", ignoreCase = true)
                "Solucionados" -> rep.estado.equals("Solucionado", ignoreCase = true) || rep.estado.equals("Resuelto", ignoreCase = true)
                else -> true
            }

            val coincideCategoria = categoriaSeleccionadaId == null || rep.categoriaId == categoriaSeleccionadaId

            coincideTexto && coincideEstado && coincideCategoria
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (mostrarBusqueda) {
                        OutlinedTextField(
                            value = busqueda,
                            onValueChange = { busqueda = it },
                            placeholder = { Text("Buscar reporte, calle, etc...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Column {
                            Text(
                                text = "Lista de Reportes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${reportesFiltrados.size} incidentes registrados",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Botón para sincronizar con la base de datos MySQL (`reportes.php`)
                    IconButton(
                        onClick = onRefrescar,
                        enabled = !estaCargando
                    ) {
                        if (estaCargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sincronizar con MySQL",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Botón para alternar la barra de búsqueda rápida
                    IconButton(onClick = {
                        mostrarBusqueda = !mostrarBusqueda
                        if (!mostrarBusqueda) busqueda = ""
                    }) {
                        Icon(
                            imageVector = if (mostrarBusqueda) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAbrirDialogoNuevo,
                containerColor = Color(0xFFEA580C),
                contentColor = Color.White,
                modifier = Modifier.testTag("nuevo_reporte_boton")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Reporte")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filtros por Categoría (Horizontal Chips)
            if (categorias.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = categoriaSeleccionadaId == null,
                            onClick = { categoriaSeleccionadaId = null },
                            label = { Text("Todas las categorías", fontSize = 12.sp) }
                        )
                    }
                    items(categorias, key = { it.idCategoria }) { cat ->
                        FilterChip(
                            selected = categoriaSeleccionadaId == cat.idCategoria,
                            onClick = {
                                categoriaSeleccionadaId = if (categoriaSeleccionadaId == cat.idCategoria) null else cat.idCategoria
                            },
                            label = { Text(cat.nombre, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Filtros por Estado Real: Todos, Pendientes, En Proceso, Solucionados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Todos", "Pendientes", "En Proceso", "Solucionados").forEach { estado ->
                    FilterChip(
                        selected = estadoSeleccionado == estado,
                        onClick = { estadoSeleccionado = estado },
                        label = { Text(estado, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lista de Reportes
            if (reportesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (estaCargando) "Cargando reportes desde MySQL..." else "No hay reportes que coincidan con el filtro",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!estaCargando) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onRefrescar,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Refrescar datos")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportesFiltrados, key = { it.idReporte }) { reporte ->
                        ItemReporteCard(
                            reporte = reporte,
                            onClick = { onSelectReporte(reporte) }
                        )
                    }
                }
            }
        }
    }

    // Modal de Detalle de Reporte (Consulta / Visualización)
    if (reporteSeleccionado != null) {
        DialogoDetalleReporte(
            reporte = reporteSeleccionado,
            onDismiss = { onSelectReporte(null) }
        )
    }

    // Modal para registrar un nuevo reporte
    if (mostrarDialogoNuevo) {
        DialogoCrearReporte(
            categorias = categorias,
            usuarioId = usuarioActualId,
            onDismiss = onDismissDialogoNuevo,
            onGuardar = { request ->
                onCrearReporte(request)
                onDismissDialogoNuevo()
            }
        )
    }
}

// Tarjeta de elemento de reporte en el listado
@Composable
fun ItemReporteCard(
    reporte: Reporte,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etiqueta de Categoría
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = reporte.categoriaNombre ?: "Alerta",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Etiqueta de Estado Real
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Constantes.obtenerColorEstado(reporte.estado).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = reporte.estado,
                        color = Constantes.obtenerColorEstado(reporte.estado),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = reporte.titulo,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reporte.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reporte.direccion ?: "GPS: ${reporte.ubicacion}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (!reporte.usuarioNombre.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Por: ${reporte.usuarioNombre}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Modal para ver el detalle de un reporte (solo consulta)
@Composable
fun DialogoDetalleReporte(
    reporte: Reporte,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Constantes.obtenerColorEstado(reporte.estado).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Estado: ${reporte.estado}",
                            color = Constantes.obtenerColorEstado(reporte.estado),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = reporte.titulo,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reporte.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fila de Metadatos
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Categoría: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(reporte.categoriaNombre ?: "General", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dirección: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(reporte.direccion ?: "No especificada", fontSize = 12.sp, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ubicación GPS: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(reporte.ubicacion, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reportado por: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(reporte.usuarioNombre ?: "Vecino", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Detalle")
                }
            }
        }
    }
}

// Modal para registrar un nuevo reporte
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearReporte(
    categorias: List<Categoria>,
    usuarioId: Int = 1,
    onDismiss: () -> Unit,
    onGuardar: (CrearReporteRequest) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("Av. Heroínas #450, Cochabamba") }
    var categoriaId by remember { mutableStateOf(categorias.firstOrNull()?.idCategoria ?: 1) }
    var latitud by remember { mutableStateOf("-17.3935") }
    var longitud by remember { mutableStateOf("-66.1570") }
    var expandedCat by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nuevo Incidente",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título de la alerta") },
                        placeholder = { Text("Ej. Basura acumulada en la esquina") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción del problema") },
                        placeholder = { Text("Describe los detalles para la atención...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Selector de Categoría
                item {
                    if (categorias.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            val catSeleccionada =
                                categorias.find {
                                    it.idCategoria == categoriaId
                                }?.nombre ?: "Seleccionar"

                            OutlinedTextField(
                                value = catSeleccionada,
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text("Categoría")
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector =
                                            if (expandedCat)
                                                Icons.Default.KeyboardArrowUp
                                            else
                                                Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Seleccionar categoría"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedCat = true
                                    },
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor =
                                        MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor =
                                        MaterialTheme.colorScheme.outline,
                                    disabledLabelColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledTrailingIconColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            DropdownMenu(
                                expanded = expandedCat,
                                onDismissRequest = {
                                    expandedCat = false
                                },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {

                                categorias.forEach { cat ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(cat.nombre)
                                        },
                                        onClick = {
                                            categoriaId = cat.idCategoria
                                            expandedCat = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = "Categoría General (ID: $categoriaId)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección o Referencia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = latitud,
                            onValueChange = { latitud = it },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = longitud,
                            onValueChange = { longitud = it },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    Button(
                        onClick = {
                            if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                                val latVal = latitud.toDoubleOrNull() ?: -17.3935
                                val lngVal = longitud.toDoubleOrNull() ?: -66.1570
                                onGuardar(
                                    CrearReporteRequest(
                                        titulo = titulo,
                                        descripcion = descripcion,
                                        ubicacion = "$latVal,$lngVal",
                                        direccion = direccion.ifBlank { null },
                                        idUsuario = usuarioId,
                                        idCategoria = categoriaId
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
