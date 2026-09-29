package com.example.comunimap.screens

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.ActualizarEstadoReporteRequest
import com.example.comunimap.models.ActualizarReporteRequest
import com.example.comunimap.models.Categoria
import com.example.comunimap.models.CrearReporteRequest
import com.example.comunimap.models.Reporte
import com.example.comunimap.models.Usuario
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA: GESTIÓN DE REPORTES
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }

    // Filtros y búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var selectedEstadoFilter by remember { mutableStateOf("Todos") }
    var selectedCategoriaFilterId by remember { mutableStateOf<String?>(null) }

    // Diálogos modales
    var reporteDetalle by remember { mutableStateOf<Reporte?>(null) }
    var reporteAEditar by remember { mutableStateOf<Reporte?>(null) }
    var reporteACambiarEstado by remember { mutableStateOf<Reporte?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Consulta de reportes, categorías y usuarios reales
    fun cargarDatos() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. Cargar reportes
                val respReportes = ApiClient.apiService.obtenerReportes()
                Log.d("REPORTES_HTTP", "HTTP: ${respReportes.code()}")
                if (respReportes.isSuccessful) {
                    val body = respReportes.body()
                    if (body != null && body.success) {
                        reportes = body.reportes
                        Log.d("REPORTES_RESPONSE", "Reportes recibidos: ${body.reportes.size}")
                    } else {
                        errorMessage = "La API respondió con un error."
                    }
                } else {
                    errorMessage = "Error del servidor: HTTP ${respReportes.code()}"
                }

                // 2. Cargar categorías para selección
                val respCategorias = ApiClient.apiService.obtenerCategorias()
                Log.d("CATEGORIAS_HTTP", "HTTP: ${respCategorias.code()}")
                if (respCategorias.isSuccessful && respCategorias.body()?.success == true) {
                    categorias = respCategorias.body()?.categorias ?: emptyList()
                    Log.d("CATEGORIAS_RESPONSE", "Categorías recibidas: ${categorias.size}")
                }

                // 3. Cargar usuarios para asociar autor si se requiere
                val respUsuarios = ApiClient.apiService.obtenerUsuarios()
                if (respUsuarios.isSuccessful && respUsuarios.body()?.success == true) {
                    usuarios = respUsuarios.body()?.usuarios ?: emptyList()
                }
            } catch (ex: Exception) {
                Log.e("REPORTES_ERROR", "Error consultando reportes", ex)
                errorMessage = "No se pudo conectar con el servidor."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    // Filtrado de reportes
    val reportesFiltrados = reportes.filter { r ->
        val matchesSearch = searchQuery.isBlank() ||
                r.titulo.contains(searchQuery, ignoreCase = true) ||
                (r.direccion?.contains(searchQuery, ignoreCase = true) == true)
        val matchesEstado = selectedEstadoFilter == "Todos" ||
                r.estado.equals(selectedEstadoFilter, ignoreCase = true)
        val matchesCategoria = selectedCategoriaFilterId == null ||
                r.idCategoria == selectedCategoriaFilterId
        matchesSearch && matchesEstado && matchesCategoria
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reportes", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { cargarDatos() },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("reportes_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar reportes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por título o dirección") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_reportes_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtro por Estado
            Text(
                text = "Estado:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val estados = listOf("Todos", "Pendiente", "En Proceso", "Solucionado")
                items(estados) { estado ->
                    FilterChip(
                        selected = selectedEstadoFilter == estado,
                        onClick = { selectedEstadoFilter = estado },
                        label = { Text(estado) }
                    )
                }
            }

            // Filtro por Categoría si hay categorías disponibles
            if (categorias.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoriaFilterId == null,
                            onClick = { selectedCategoriaFilterId = null },
                            label = { Text("Todas Categorías") }
                        )
                    }
                    items(categorias) { cat ->
                        FilterChip(
                            selected = selectedCategoriaFilterId == cat.idCategoria,
                            onClick = { selectedCategoriaFilterId = cat.idCategoria },
                            label = { Text(cat.nombre) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido principal
            if (isLoading && reportes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.testTag("reportes_loading_indicator"))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando reportes...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else if (errorMessage != null && reportes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cargarDatos() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (reportesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No se encontraron reportes con los filtros seleccionados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("reportes_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(reportesFiltrados) { reporte ->
                        ReporteCard(
                            reporte = reporte,
                            onVerDetalle = { reporteDetalle = reporte },
                            onEditar = { reporteAEditar = reporte },
                            onCambiarEstado = { reporteACambiarEstado = reporte }
                        )
                    }
                }
            }
        }
    }

    // Diálogo: Editar Reporte
    reporteAEditar?.let { reporte ->
        EditarReporteDialog(
            reporte = reporte,
            categorias = categorias,
            onDismiss = { reporteAEditar = null },
            onReporteEditado = {
                reporteAEditar = null
                cargarDatos()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Reporte actualizado con éxito")
                }
            }
        )
    }

    // Diálogo: Cambiar Estado
    reporteACambiarEstado?.let { reporte ->
        CambiarEstadoReporteDialog(
            reporte = reporte,
            onDismiss = { reporteACambiarEstado = null },
            onEstadoCambiado = {
                reporteACambiarEstado = null
                cargarDatos()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Estado cambiado con éxito")
                }
            }
        )
    }

    // Diálogo: Ver Detalle
    reporteDetalle?.let { reporte ->
        DetalleReporteDialog(
            reporte = reporte,
            onDismiss = { reporteDetalle = null }
        )
    }
}

// Tarjeta individual para mostrar reporte en la lista
@Composable
private fun ReporteCard(
    reporte: Reporte,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    val colorEstado = when (reporte.estado.trim()) {
        "Pendiente" -> Color(0xFFE65100)
        "En Proceso" -> Color(0xFF0277BD)
        "Solucionado" -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle() }
            .testTag("reporte_card_${reporte.idReporte ?: "item"}"),
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
                Text(
                    text = reporte.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = reporte.estado,
                    fontWeight = FontWeight.Bold,
                    color = colorEstado,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reporte.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reporte.direccion ?: "Sin dirección especificada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!reporte.categoria.isNullOrBlank()) {
                Text(
                    text = "Categoría: ${reporte.categoria}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Acciones rápidas: Editar y Cambiar Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCambiarEstado) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Estado", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 12.sp)
                }
            }
        }
    }
}

// Diálogo para editar reporte existente
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarReporteDialog(
    reporte: Reporte,
    categorias: List<Categoria>,
    onDismiss: () -> Unit,
    onReporteEditado: () -> Unit
) {
    var titulo by remember { mutableStateOf(reporte.titulo) }
    var descripcion by remember { mutableStateOf(reporte.descripcion) }
    var ubicacion by remember { mutableStateOf(reporte.ubicacion ?: "") }
    var direccion by remember { mutableStateOf(reporte.direccion ?: "") }
    var selectedCategoriaId by remember { mutableStateOf(reporte.idCategoria ?: (categorias.firstOrNull()?.idCategoria ?: "1")) }
    var selectedEstado by remember { mutableStateOf(reporte.estado) }

    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Editar Reporte") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (latitud,longitud)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de categoría
                if (categorias.isNotEmpty()) {
                    Text("Categoría:", style = MaterialTheme.typography.labelMedium)
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

                // Selector de estado
                Text("Estado:", style = MaterialTheme.typography.labelMedium)
                var estadoExpanded by remember { mutableStateOf(false) }
                val estadosPermitidos = listOf("Pendiente", "En Proceso", "Solucionado")
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { estadoExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedEstado,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = estadoExpanded,
                        onDismissRequest = { estadoExpanded = false }
                    ) {
                        estadosPermitidos.forEach { est ->
                            DropdownMenuItem(
                                text = { Text(est) },
                                onClick = {
                                    selectedEstado = est
                                    estadoExpanded = false
                                }
                            )
                        }
                    }
                }

                if (formError != null) {
                    Text(
                        text = formError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idReporte = reporte.idReporte
                    if (idReporte == null) {
                        formError = "ID de reporte inválido."
                        return@Button
                    }
                    coroutineScope.launch {
                        isSubmitting = true
                        formError = null
                        try {
                            val req = ActualizarReporteRequest(
                                idReporte = idReporte,
                                titulo = titulo.trim(),
                                descripcion = descripcion.trim(),
                                ubicacion = ubicacion.trim(),
                                direccion = direccion.trim(),
                                idCategoria = selectedCategoriaId,
                                estado = selectedEstado
                            )
                            Log.d("API_MUTATION", "POST actualizar_reporte.php id: $idReporte")
                            val resp = ApiClient.apiService.actualizarReporte(req)
                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onReporteEditado()
                            } else {
                                formError = resp.body()?.message ?: "Error al actualizar: HTTP ${resp.code()}"
                            }
                        } catch (ex: Exception) {
                            Log.e("API_ERROR", "Error actualizando reporte", ex)
                            formError = "No se pudo conectar con el servidor."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), color = Color.White)
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}

// Diálogo rápido para cambiar el estado del reporte
@Composable
private fun CambiarEstadoReporteDialog(
    reporte: Reporte,
    onDismiss: () -> Unit,
    onEstadoCambiado: () -> Unit
) {
    var selectedEstado by remember { mutableStateOf(reporte.estado) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val estadosPermitidos = listOf("Pendiente", "En Proceso", "Solucionado")

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Cambiar Estado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Selecciona el nuevo estado para el reporte:")
                Text(
                    text = reporte.titulo,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                estadosPermitidos.forEach { est ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedEstado = est }
                            .padding(vertical = 4.dp)
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedEstado == est,
                            onClick = { selectedEstado = est }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(est)
                    }
                }

                if (errorMsg != null) {
                    Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idReporte = reporte.idReporte ?: return@Button
                    coroutineScope.launch {
                        isSubmitting = true
                        errorMsg = null
                        try {
                            val req = ActualizarEstadoReporteRequest(
                                idReporte = idReporte,
                                estado = selectedEstado
                            )
                            Log.d("API_MUTATION", "POST actualizar_estado.php id: $idReporte a $selectedEstado")
                            val resp = ApiClient.apiService.actualizarEstadoReporte(req)
                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onEstadoCambiado()
                            } else {
                                errorMsg = resp.body()?.message ?: "Error al actualizar estado: HTTP ${resp.code()}"
                            }
                        } catch (ex: Exception) {
                            Log.e("API_ERROR", "Error cambiando estado", ex)
                            errorMsg = "No se pudo conectar con el servidor."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), color = Color.White)
                } else {
                    Text("Actualizar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}

// Diálogo para ver detalle completo de un reporte
@Composable
private fun DetalleReporteDialog(
    reporte: Reporte,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(reporte.titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Estado: ${reporte.estado}",
                    fontWeight = FontWeight.Bold,
                    color = when (reporte.estado) {
                        "Pendiente" -> Color(0xFFE65100)
                        "En Proceso" -> Color(0xFF0277BD)
                        "Solucionado" -> Color(0xFF2E7D32)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(text = "Descripción:\n${reporte.descripcion}")
                Text(text = "Dirección: ${reporte.direccion ?: "N/A"}")
                Text(text = "Coordenadas: ${reporte.ubicacion ?: "N/A"}")
                Text(text = "Categoría: ${reporte.categoria ?: "N/A"}")
                Text(text = "Usuario reportador: ${reporte.usuario ?: (reporte.idUsuario ?: "N/A")}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
