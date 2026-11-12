package com.example.comunimap.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.api.ApiClient
import com.example.comunimap.components.CasoAgrupadoCard
import com.example.comunimap.components.ColorPrioridadAlta
import com.example.comunimap.components.ColorPrioridadBaja
import com.example.comunimap.components.ColorPrioridadMedia
import com.example.comunimap.components.SeccionCasosAgrupadosHeader
import com.example.comunimap.components.SeccionEvidenciaFotografica
import com.example.comunimap.models.ActualizarEstadoGrupoRequest
import com.example.comunimap.models.ActualizarEstadoReporteRequest
import com.example.comunimap.models.ActualizarReporteRequest
import com.example.comunimap.models.CasoAgrupado
import com.example.comunimap.models.CasosAgrupadosResponse
import com.example.comunimap.models.Categoria
import com.example.comunimap.models.CrearReporteRequest
import com.example.comunimap.models.Reporte
import com.example.comunimap.models.Usuario
import com.example.comunimap.utils.PreferenciasManager
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA: GESTIÓN DE REPORTES
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    usuarioActual: Usuario? = null,
    esAdministrador: Boolean = false,
    tokenApi: String? = null,
    onVerReporteEnMapa: ((Reporte) -> Unit)? = null
) {
    val context = LocalContext.current
    // Detección robusta del rol Administrador
    val esAdmin = esAdministrador || (usuarioActual?.rol?.trim()?.equals("Administrador", ignoreCase = true) == true)

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reportes by remember { mutableStateOf<List<Reporte>>(emptyList()) }
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }

    // Casos comunitarios agrupados por prioridad (Solo Administrador)
    var casosAgrupadosResponse by remember { mutableStateOf<CasosAgrupadosResponse?>(null) }
    var isLoadingCasosAgrupados by remember(esAdmin) { mutableStateOf(esAdmin) }
    var errorCasosAgrupados by remember { mutableStateOf<String?>(null) }

    // Estado para confirmación y procesamiento de cambio de estado grupal
    var confirmacionCambioGrupo by remember {
        mutableStateOf<Pair<CasoAgrupado, String>?>(null)
    }
    var casoEnProcesoId by remember { mutableStateOf<String?>(null) }

    // Filtros y búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var selectedEstadoFilter by remember { mutableStateOf("Todos") }
    var selectedCategoriaFilterId by remember { mutableStateOf<String?>(null) }
    var filtrosDesplegados by remember { mutableStateOf(false) }

    // Diálogos modales
    var reporteDetalle by remember { mutableStateOf<Reporte?>(null) }
    var reporteAEditar by remember { mutableStateOf<Reporte?>(null) }
    var reporteACambiarEstado by remember { mutableStateOf<Reporte?>(null) }
    var mostrarContactoCategoria by remember { mutableStateOf(false) }
    var mostrarDirectorioCompleto by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Manejador para abrir el marcador telefónico del celular (exclusivo para Administrador)
    val manejarAccionTelefono: (String) -> Unit = { numero ->
        if (numero.isNotBlank()) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$numero")
                }
                context.startActivity(dialIntent)
            } catch (e: Exception) {
                Log.e("MARCADOR_TELEFONO", "Error al abrir la aplicación de llamadas para $numero", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("No se encontró una aplicación de teléfono en este dispositivo")
                }
            }
        }
    }

    // Consulta aislada para recargar únicamente los casos agrupados (ej. botón reintentar)
    suspend fun recargarCasosAgrupados() {
        if (!esAdmin) return
        isLoadingCasosAgrupados = true
        errorCasosAgrupados = null
        try {
            val respCasos = ApiClient.apiService.obtenerCasosAgrupados()
            if (respCasos.isSuccessful && respCasos.body()?.success == true) {
                casosAgrupadosResponse = respCasos.body()
                errorCasosAgrupados = null
                Log.d("CASOS_RESPONSE", "Casos agrupados cargados: total=${casosAgrupadosResponse?.total}")
            } else {
                errorCasosAgrupados = "No se pudieron cargar los casos agrupados."
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (ex: Exception) {
            Log.e("CASOS_ERROR", "Error consultando casos agrupados", ex)
            errorCasosAgrupados = "No se pudieron cargar los casos agrupados."
        } finally {
            isLoadingCasosAgrupados = false
        }
    }

    // Consulta concurrente y sincronizada de reportes, categorías, usuarios y casos prioritarios
    suspend fun cargarDatos() {
        isLoading = true
        errorMessage = null
        if (esAdmin) {
            isLoadingCasosAgrupados = true
            errorCasosAgrupados = null
        }
        try {
            coroutineScope {
                // 1. Iniciar consulta de casos agrupados por prioridad en paralelo si es Administrador
                val deferredCasos = if (esAdmin) {
                    async {
                        try {
                            ApiClient.apiService.obtenerCasosAgrupados()
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (ex: Exception) {
                            Log.e("CASOS_ERROR", "Error al consultar casos agrupados", ex)
                            null
                        }
                    }
                } else null

                // 2. Iniciar consulta de reportes en paralelo
                val deferredReportes = async {
                    try {
                        ApiClient.apiService.obtenerReportes()
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (ex: Exception) {
                        Log.e("REPORTES_ERROR", "Error al consultar reportes", ex)
                        null
                    }
                }

                // 3. Iniciar consulta de categorías en paralelo
                val deferredCategorias = async {
                    try {
                        ApiClient.apiService.obtenerCategorias()
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (ex: Exception) {
                        Log.e("CATEGORIAS_ERROR", "Error al consultar categorías", ex)
                        null
                    }
                }

                // 4. Iniciar consulta de usuarios en paralelo si es Administrador
                val deferredUsuarios = if (esAdmin) {
                    async {
                        try {
                            ApiClient.apiService.obtenerUsuarios()
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (ex: Exception) {
                            Log.e("USUARIOS_ERROR", "Error al consultar usuarios", ex)
                            null
                        }
                    }
                } else null

                // Procesar respuesta de casos prioritarios (para que estén disponibles desde el inicio)
                if (deferredCasos != null) {
                    val respCasos = deferredCasos.await()
                    if (respCasos != null && respCasos.isSuccessful && respCasos.body()?.success == true) {
                        casosAgrupadosResponse = respCasos.body()
                        errorCasosAgrupados = null
                        Log.d("CASOS_RESPONSE", "Casos prioritarios recibidos en primera carga: total=${casosAgrupadosResponse?.total}")
                    } else {
                        errorCasosAgrupados = "No se pudieron cargar los casos agrupados."
                    }
                    isLoadingCasosAgrupados = false
                }

                // Procesar respuesta de reportes
                val respReportes = deferredReportes.await()
                if (respReportes != null) {
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
                } else {
                    errorMessage = "No se pudo conectar con el servidor."
                }

                // Procesar respuesta de categorías
                val respCategorias = deferredCategorias.await()
                if (respCategorias != null && respCategorias.isSuccessful && respCategorias.body()?.success == true) {
                    categorias = respCategorias.body()?.categorias ?: emptyList()
                }

                // Procesar respuesta de usuarios
                if (deferredUsuarios != null) {
                    val respUsuarios = deferredUsuarios.await()
                    if (respUsuarios != null && respUsuarios.isSuccessful && respUsuarios.body()?.success == true) {
                        usuarios = respUsuarios.body()?.usuarios ?: emptyList()
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (ex: Exception) {
            Log.e("REPORTES_ERROR", "Error general consultando datos", ex)
            errorMessage = "No se pudo conectar con el servidor."
        } finally {
            isLoading = false
            isLoadingCasosAgrupados = false
        }
    }

    // Función para cambiar el estado de todos los reportes de un caso agrupado
    fun ejecutarCambioEstadoGrupo(caso: CasoAgrupado, idReporteInt: Int, nuevoEstado: String) {
        if (!esAdmin) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("No tienes permisos para cambiar el estado de este caso.")
            }
            return
        }

        val token = tokenApi ?: PreferenciasManager.obtenerTokenApi(context)
        if (token.isNullOrBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Tu sesión administrativa ha caducado. Inicia sesión nuevamente.")
            }
            return
        }

        if (casoEnProcesoId != null) return // Evitar doble pulsación concurrente

        coroutineScope.launch {
            casoEnProcesoId = caso.idReporteFormateado
            try {
                val authHeader = "Bearer $token"
                val req = ActualizarEstadoGrupoRequest(
                    idReporte = idReporteInt,
                    estado = nuevoEstado
                )
                val response = ApiClient.apiService.actualizarEstadoGrupo(authHeader, req)

                if (response.isSuccessful && response.body()?.success == true) {
                    snackbarHostState.showSnackbar("Estado de los reportes actualizado correctamente.")
                    cargarDatos()
                } else {
                    when (response.code()) {
                        400 -> snackbarHostState.showSnackbar("Los datos del cambio de estado no son válidos.")
                        401 -> snackbarHostState.showSnackbar("Tu sesión administrativa ha caducado. Inicia sesión nuevamente.")
                        403 -> snackbarHostState.showSnackbar("No tienes permisos para cambiar el estado de este caso.")
                        409 -> {
                            snackbarHostState.showSnackbar("Este caso cambió. Actualiza la lista e inténtalo nuevamente.")
                            cargarDatos()
                        }
                        else -> snackbarHostState.showSnackbar("No se pudo actualizar el estado. Inténtalo nuevamente.")
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("No se pudo actualizar el estado. Inténtalo nuevamente.")
            } finally {
                casoEnProcesoId = null
            }
        }
    }

    LaunchedEffect(esAdmin, usuarioActual?.idUsuario) {
        cargarDatos()
    }

    // Listas derivadas de casos agrupados
    val casosAlta = remember(casosAgrupadosResponse) { casosAgrupadosResponse?.casos?.alta ?: emptyList() }
    val casosMedia = remember(casosAgrupadosResponse) { casosAgrupadosResponse?.casos?.media ?: emptyList() }
    val casosBaja = remember(casosAgrupadosResponse) { casosAgrupadosResponse?.casos?.baja ?: emptyList() }
    val tieneCasosEspeciales = esAdmin && (casosAlta.isNotEmpty() || casosMedia.isNotEmpty() || casosBaja.isNotEmpty())

    // Filtrado de reportes por rol (Administrador ve todos, Usuario solo los suyos)
    val reportesVisibles = remember(reportes, esAdmin, usuarioActual) {
        if (esAdmin) {
            reportes
        } else {
            val uid = usuarioActual?.idUsuario?.toString() ?: ""
            reportes.filter { it.idUsuario?.toString() == uid }
        }
    }

    // Filtrado de reportes según búsqueda y filtros seleccionados
    val reportesFiltrados = reportesVisibles.filter { r ->
        val matchesSearch = searchQuery.isBlank() ||
                r.titulo.contains(searchQuery, ignoreCase = true) ||
                (r.direccion?.contains(searchQuery, ignoreCase = true) == true)
        val matchesEstado = selectedEstadoFilter == "Todos" ||
                r.estado.equals(selectedEstadoFilter, ignoreCase = true)
        val matchesCategoria = selectedCategoriaFilterId == null ||
                r.idCategoria == selectedCategoriaFilterId
        matchesSearch && matchesEstado && matchesCategoria
    }

    fun esReporteSolucionado(estado: String?): Boolean {
        val norm = estado?.trim()?.lowercase(Locale.ROOT)
        return norm == "solucionado" || norm == "resuelto"
    }

    // Separación de reportes individuales: Activos vs Solucionados
    val reportesActivos = remember(reportesFiltrados) {
        reportesFiltrados.filter { !esReporteSolucionado(it.estado) }
    }

    val reportesSolucionados = remember(reportesFiltrados) {
        reportesFiltrados.filter { esReporteSolucionado(it.estado) }
    }

    val nombreCategoriaFiltro = remember(selectedCategoriaFilterId, categorias) {
        if (selectedCategoriaFilterId == null) "Categorías"
        else categorias.find { it.idCategoria == selectedCategoriaFilterId }?.nombre ?: "Categorías"
    }
    val resumenFiltroReportes = "$selectedEstadoFilter · $nombreCategoriaFiltro"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(
                    onClick = { mostrarDirectorioCompleto = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 8.dp, end = 4.dp)
                        .testTag("reportes_directorio_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Directorio de servicios de la OTB",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
        ) {
            // Cabecera compacta: Título y Recargar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (esAdmin) "Reportes" else "Mis Reportes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = { coroutineScope.launch { cargarDatos() } },
                    enabled = !isLoading,
                    modifier = Modifier.testTag("reportes_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar reportes",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Campo de búsqueda compacto
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Buscar reportes...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .testTag("search_reportes_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Barra colapsable de filtros
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { filtrosDesplegados = !filtrosDesplegados },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (selectedEstadoFilter != "Todos" || selectedCategoriaFilterId != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = resumenFiltroReportes,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedEstadoFilter != "Todos" || selectedCategoriaFilterId != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (filtrosDesplegados) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (filtrosDesplegados) "Ocultar filtros" else "Mostrar filtros",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Panel de chips desplegable con animación suave
            AnimatedVisibility(visible = filtrosDesplegados) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Estado:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val estados = listOf("Todos", "Pendiente", "En Proceso", "Solucionado")
                        items(estados) { estado ->
                            FilterChip(
                                selected = selectedEstadoFilter == estado,
                                onClick = { selectedEstadoFilter = estado },
                                label = { Text(estado, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    if (categorias.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Categoría:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoriaFilterId == null,
                                    onClick = { selectedCategoriaFilterId = null },
                                    label = { Text("Todas Categorías", style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                            items(categorias) { cat ->
                                FilterChip(
                                    selected = selectedCategoriaFilterId == cat.idCategoria,
                                    onClick = { selectedCategoriaFilterId = cat.idCategoria },
                                    label = { Text(cat.nombre, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido principal
            val estaCargandoTodo = (isLoading && reportes.isEmpty()) || (esAdmin && isLoadingCasosAgrupados && reportes.isEmpty() && casosAgrupadosResponse == null)
            if (estaCargandoTodo) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.testTag("reportes_loading_indicator"))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (esAdmin) "Cargando reportes y casos prioritarios..." else "Cargando reportes...",
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                        Button(onClick = { coroutineScope.launch { cargarDatos() } }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("reportes_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // SECCIÓN EXCLUSIVA PARA ADMINISTRADOR: CASOS COMUNITARIOS AGRUPADOS POR PRIORIDAD
                    if (esAdmin) {
                        // 1. Indicador discreto si la API de casos agrupados está cargando
                        if (isLoadingCasosAgrupados && casosAgrupadosResponse == null) {
                            item(key = "casos_loading_indicator") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cargando casos comunitarios...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // 2. Aviso amigable y discreto si no se pudieron cargar los casos agrupados
                        if (errorCasosAgrupados != null && !isLoadingCasosAgrupados && !tieneCasosEspeciales) {
                            item(key = "casos_error_banner") {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = errorCasosAgrupados ?: "No se pudieron cargar los casos agrupados.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    recargarCasosAgrupados()
                                                }
                                            }
                                        ) {
                                            Text("Reintentar", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }

                        // 3. SECCIÓN 1: CASOS DE ATENCIÓN URGENTE (si alta tiene elementos)
                        if (casosAlta.isNotEmpty()) {
                            item(key = "header_casos_alta") {
                                SeccionCasosAgrupadosHeader(
                                    titulo = "CASOS DE ATENCIÓN URGENTE",
                                    cantidad = casosAlta.size,
                                    subtitulo = "Reportes críticos reiterados por vecinos en la misma área (5m).",
                                    colorAcento = ColorPrioridadAlta,
                                    icono = Icons.Default.Warning
                                )
                            }
                            itemsIndexed(casosAlta, key = { index, caso -> "alta_${caso.idReporteFormateado}_$index" }) { _, caso ->
                                CasoAgrupadoCard(
                                    caso = caso,
                                    esAdministrador = esAdmin,
                                    isProcesando = casoEnProcesoId == caso.idReporteFormateado,
                                    onCambiarEstado = { nuevoEstado ->
                                        confirmacionCambioGrupo = Pair(caso, nuevoEstado)
                                    }
                                )
                            }
                        }

                        // 4. SECCIÓN 2: CASOS QUE REQUIEREN ATENCIÓN (si media tiene elementos)
                        if (casosMedia.isNotEmpty()) {
                            item(key = "header_casos_media") {
                                SeccionCasosAgrupadosHeader(
                                    titulo = "CASOS QUE REQUIEREN ATENCIÓN",
                                    cantidad = casosMedia.size,
                                    subtitulo = "Reportes de prioridad media que requieren revisión o intervención.",
                                    colorAcento = ColorPrioridadMedia,
                                    icono = Icons.Default.WarningAmber
                                )
                            }
                            itemsIndexed(casosMedia, key = { index, caso -> "media_${caso.idReporteFormateado}_$index" }) { _, caso ->
                                CasoAgrupadoCard(
                                    caso = caso,
                                    esAdministrador = esAdmin,
                                    isProcesando = casoEnProcesoId == caso.idReporteFormateado,
                                    onCambiarEstado = { nuevoEstado ->
                                        confirmacionCambioGrupo = Pair(caso, nuevoEstado)
                                    }
                                )
                            }
                        }

                        // 5. SECCIÓN 3: CASOS EN SEGUIMIENTO (si baja tiene elementos)
                        if (casosBaja.isNotEmpty()) {
                            item(key = "header_casos_baja") {
                                SeccionCasosAgrupadosHeader(
                                    titulo = "CASOS EN SEGUIMIENTO",
                                    cantidad = casosBaja.size,
                                    subtitulo = "Reportes de baja prioridad acumulados en el sector.",
                                    colorAcento = ColorPrioridadBaja,
                                    icono = Icons.Default.Info
                                )
                            }
                            itemsIndexed(casosBaja, key = { index, caso -> "baja_${caso.idReporteFormateado}_$index" }) { _, caso ->
                                CasoAgrupadoCard(
                                    caso = caso,
                                    esAdministrador = esAdmin,
                                    isProcesando = casoEnProcesoId == caso.idReporteFormateado,
                                    onCambiarEstado = { nuevoEstado ->
                                        confirmacionCambioGrupo = Pair(caso, nuevoEstado)
                                    }
                                )
                            }
                        }

                    }

                    // =====================================================
                    // SECCIÓN 1: REPORTES INDIVIDUALES (ACTIVOS)
                    // =====================================================
                    item(key = "header_reportes_activos") {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "REPORTES INDIVIDUALES",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "(${reportesActivos.size} activos)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (reportesActivos.isEmpty()) {
                        item(key = "reportes_activos_vacios") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (esAdmin) {
                                            "No se encontraron reportes activos con los filtros seleccionados."
                                        } else {
                                            "No tienes reportes activos pendientes o en proceso."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(reportesActivos, key = { index, it -> "activo_${it.idReporte ?: it.titulo}_$index" }) { _, reporte ->
                            ReporteCard(
                                reporte = reporte,
                                esAdministrador = esAdmin,
                                onVerDetalle = { reporteDetalle = reporte },
                                onEditar = { reporteAEditar = reporte },
                                onCambiarEstado = { reporteACambiarEstado = reporte }
                            )
                        }
                    }

                    // =====================================================
                    // SECCIÓN 2: REPORTES SOLUCIONADOS
                    // =====================================================
                    item(key = "header_reportes_solucionados") {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Reportes solucionados",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "(${reportesSolucionados.size} resueltos)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Historial de problemas atendidos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (reportesSolucionados.isEmpty()) {
                        item(key = "reportes_solucionados_vacios") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay reportes solucionados que coincidan con los filtros.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(reportesSolucionados, key = { index, it -> "solucionado_${it.idReporte ?: it.titulo}_$index" }) { _, reporte ->
                            ReporteCard(
                                reporte = reporte,
                                esAdministrador = esAdmin,
                                onVerDetalle = { reporteDetalle = reporte },
                                onEditar = { reporteAEditar = reporte },
                                onCambiarEstado = { reporteACambiarEstado = reporte }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo: Confirmar cambio de estado de caso comunitario agrupado
    confirmacionCambioGrupo?.let { (caso, nuevoEstado) ->
        AlertDialog(
            onDismissRequest = { confirmacionCambioGrupo = null },
            title = {
                Text(
                    text = "Confirmar cambio de estado",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas cambiar a \"$nuevoEstado\" los ${caso.cantidadReportes} reportes de este caso?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idRepInt = caso.idReporteInt ?: caso.idReporteFormateado.toIntOrNull()
                        confirmacionCambioGrupo = null
                        if (idRepInt != null) {
                            ejecutarCambioEstadoGrupo(caso, idRepInt, nuevoEstado)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Los datos del cambio de estado no son válidos.")
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_confirmar_cambio_grupo")
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmacionCambioGrupo = null },
                    modifier = Modifier.testTag("btn_cancelar_cambio_grupo")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo: Editar Reporte
    reporteAEditar?.let { reporte ->
        EditarReporteDialog(
            reporte = reporte,
            categorias = categorias,
            onDismiss = { reporteAEditar = null },
            onReporteEditado = {
                reporteAEditar = null
                coroutineScope.launch {
                    cargarDatos()
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
                coroutineScope.launch {
                    cargarDatos()
                    snackbarHostState.showSnackbar("Estado cambiado con éxito")
                }
            }
        )
    }

    // Diálogo: Ver Detalle
    reporteDetalle?.let { reporte ->
        DetalleReporteDialog(
            reporte = reporte,
            esAdministrador = esAdmin,
            onContactarServicio = {
                mostrarContactoCategoria = true
            },
            onMostrarUbicacionEnMapa = {
                val ubicacion = reporte.ubicacion
                var coordsValidas = false
                if (!ubicacion.isNullOrBlank()) {
                    val partes = ubicacion.split(",")
                    if (partes.size >= 2) {
                        val lat = partes[0].trim().toDoubleOrNull()
                        val lng = partes[1].trim().toDoubleOrNull()
                        if (lat != null && lng != null) {
                            coordsValidas = true
                        }
                    }
                }
                if (coordsValidas) {
                    reporteDetalle = null
                    mostrarContactoCategoria = false
                    onVerReporteEnMapa?.invoke(reporte)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Este reporte no cuenta con coordenadas válidas para mostrarse en el mapa.")
                    }
                }
            },
            onDismiss = {
                reporteDetalle = null
                mostrarContactoCategoria = false
            }
        )
    }

    // Diálogo secundario: Contacto de servicio según categoría del reporte (Solo Administrador)
    if (mostrarContactoCategoria && reporteDetalle != null && esAdmin) {
        ContactoCategoriaDialog(
            reporte = reporteDetalle!!,
            categorias = categorias,
            onAccionTelefono = manejarAccionTelefono,
            onDismiss = {
                // Al cerrar, regresar al detalle del mismo reporte conservando reporteDetalle
                mostrarContactoCategoria = false
            }
        )
    }

    // Diálogo: Directorio completo de servicios de la OTB (Botón flotante, Solo Administrador)
    if (mostrarDirectorioCompleto && esAdmin) {
        DirectorioServiciosDialog(
            onAccionTelefono = manejarAccionTelefono,
            onDismiss = {
                mostrarDirectorioCompleto = false
            }
        )
    }
}

// Tarjeta individual para mostrar reporte en la lista
@Composable
private fun ReporteCard(
    reporte: Reporte,
    esAdministrador: Boolean,
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

            // Acciones: Administrador tiene Estado y Editar; Usuario tiene Ver Detalle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (esAdministrador) {
                    TextButton(onClick = onVerDetalle) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detalle", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
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
                } else {
                    TextButton(onClick = onVerDetalle) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Detalle", fontSize = 12.sp)
                    }
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
    var selectedCategoriaId by remember { mutableStateOf<String?>(reporte.idCategoria ?: categorias.firstOrNull()?.idCategoria) }
    var selectedEstado by remember { mutableStateOf(reporte.estado) }

    // Actualizar categoría si las categorías terminan de cargar posteriormente
    LaunchedEffect(categorias) {
        if (selectedCategoriaId == null && categorias.isNotEmpty()) {
            selectedCategoriaId = categorias.firstOrNull()?.idCategoria
        }
    }

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
                                        selectedCategoriaId = cat.idCategoria
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
                    val idCategoriaActualizada = selectedCategoriaId
                    if (idCategoriaActualizada.isNullOrBlank()) {
                        formError = if (categorias.isEmpty()) {
                            "No se pudieron cargar las categorías."
                        } else {
                            "Selecciona una categoría válida."
                        }
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
                                idCategoria = idCategoriaActualizada,
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
    esAdministrador: Boolean = false,
    onContactarServicio: () -> Unit = {},
    onMostrarUbicacionEnMapa: () -> Unit = {},
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
                    color = when (reporte.estado.trim().lowercase(Locale.ROOT)) {
                        "pendiente" -> Color(0xFFE65100)
                        "en proceso" -> Color(0xFF0277BD)
                        "solucionado", "resuelto" -> Color(0xFF2E7D32)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(text = "Descripción:\n${reporte.descripcion}")
                Text(text = "Dirección: ${reporte.direccion ?: "N/A"}")
                Text(text = "Coordenadas: ${reporte.ubicacion ?: "N/A"}")
                Text(text = "Categoría: ${reporte.categoria ?: "N/A"}")
                Text(text = "Usuario reportador: ${reporte.usuario ?: (reporte.idUsuario ?: "N/A")}")

                // Evidencia fotográfica (se omite automáticamente si no existe imagen)
                SeccionEvidenciaFotografica(
                    rutaImagen = reporte.imagen,
                    altura = 180.dp,
                    tituloColor = MaterialTheme.colorScheme.onSurface
                )

                // ACCIÓN COMÚN: MOSTRAR UBICACIÓN EN EL MAPA (Vecinos y Administradores, Activos y Solucionados)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onMostrarUbicacionEnMapa,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_ver_ubicacion_mapa"),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF38BDF8)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mostrar ubicación en el mapa",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // SECCIÓN EXCLUSIVA DE ADMINISTRADOR: CONTACTAR SERVICIO SEGÚN CATEGORÍA
                if (esAdministrador) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "¿Necesitas comunicar este problema?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Contacta con la institución correspondiente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = onContactarServicio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_contactar_servicio"),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contactar servicio",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// =====================================================
// DIÁLOGO: CONTACTO ASOCIADO A LA CATEGORÍA DEL REPORTE
// =====================================================

@Composable
private fun ContactoCategoriaDialog(
    reporte: Reporte,
    categorias: List<Categoria>,
    onAccionTelefono: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val contacto = remember(reporte, categorias) {
        buscarContactoServicioPorReporte(reporte, categorias)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Contacto institucional",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (contacto != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Categoría: ${contacto.categoriaNombre}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = contacto.nombreServicio,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAccionTelefono(contacto.telefonoFicticio) }
                                    .testTag("contacto_categoria_telefono_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Teléfono",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = contacto.telefonoFicticio,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Número de prueba",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFF59E0B),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No hay contactos registrados para esta categoría.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_volver_detalle_reporte")
            ) {
                Text("Volver")
            }
        }
    )
}

// =====================================================
// DIÁLOGO: DIRECTORIO COMPLETO DE SERVICIOS DE LA OTB
// =====================================================

@Composable
private fun DirectorioServiciosDialog(
    onAccionTelefono: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Directorio de servicios de la OTB",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Contactos disponibles para gestionar los problemas del barrio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DIRECTORIO_SERVICIOS_OTB, key = { it.categoriaNombre }) { contacto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccionTelefono(contacto.telefonoFicticio) }
                            .testTag("directorio_item_${contacto.categoriaNombre.lowercase().replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = contacto.nombreServicio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Categoría: ${contacto.categoriaNombre}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = contacto.telefonoFicticio,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "· Número de prueba",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onAccionTelefono(contacto.telefonoFicticio) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Contactar",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cerrar_directorio")
            ) {
                Text("Cerrar")
            }
        }
    )
}

// =====================================================
// MODELO Y DATOS DEL DIRECTORIO DE SERVICIOS PÚBLICOS
// =====================================================

data class ContactoServicio(
    val categoriaNombre: String,
    val nombreServicio: String,
    val telefonoFicticio: String,
    val etiquetasAlias: List<String> = emptyList()
)

val DIRECTORIO_SERVICIOS_OTB = listOf(
    ContactoServicio(
        categoriaNombre = "Basura",
        nombreServicio = "Servicio de limpieza municipal",
        telefonoFicticio = "0000000000",
        etiquetasAlias = listOf("basura", "limpieza", "residuos")
    ),
    ContactoServicio(
        categoriaNombre = "Alumbrado",
        nombreServicio = "Servicio de alumbrado público",
        telefonoFicticio = "0000000001",
        etiquetasAlias = listOf("alumbrado", "luz", "iluminacion", "electricidad")
    ),
    ContactoServicio(
        categoriaNombre = "Calles",
        nombreServicio = "Obras públicas municipales",
        telefonoFicticio = "0000000002",
        etiquetasAlias = listOf("calles", "calle", "bache", "baches", "pavimento", "obras")
    ),
    ContactoServicio(
        categoriaNombre = "Áreas Verdes",
        nombreServicio = "Parques y jardines",
        telefonoFicticio = "0000000003",
        etiquetasAlias = listOf("areas verdes", "areas", "parques", "jardines", "arboles")
    ),
    ContactoServicio(
        categoriaNombre = "Seguridad",
        nombreServicio = "Seguridad ciudadana",
        telefonoFicticio = "0000000004",
        etiquetasAlias = listOf("seguridad", "policia", "vigilancia")
    ),
    ContactoServicio(
        categoriaNombre = "Agua",
        nombreServicio = "Servicio de agua potable",
        telefonoFicticio = "0000000005",
        etiquetasAlias = listOf("agua", "potable", "fuga", "semapa")
    ),
    ContactoServicio(
        categoriaNombre = "Alcantarillado",
        nombreServicio = "Servicio de saneamiento",
        telefonoFicticio = "0000000006",
        etiquetasAlias = listOf("alcantarillado", "saneamiento", "desague", "drenaje")
    ),
    ContactoServicio(
        categoriaNombre = "Tránsito",
        nombreServicio = "Unidad de tránsito",
        telefonoFicticio = "0000000007",
        etiquetasAlias = listOf("transito", "vialidad", "semaforo", "semaforos")
    ),
    ContactoServicio(
        categoriaNombre = "Animales",
        nombreServicio = "Protección animal",
        telefonoFicticio = "0000000008",
        etiquetasAlias = listOf("animales", "zoonosis", "mascotas", "perros")
    ),
    ContactoServicio(
        categoriaNombre = "Infraestructura",
        nombreServicio = "Infraestructura municipal",
        telefonoFicticio = "0000000009",
        etiquetasAlias = listOf("infraestructura", "edificaciones", "construccion")
    ),
    ContactoServicio(
        categoriaNombre = "Otros",
        nombreServicio = "Administración de la OTB",
        telefonoFicticio = "0000000010",
        etiquetasAlias = listOf("otros", "administracion", "otb", "general")
    )
)

private fun normalizarTextoServicio(texto: String): String {
    return java.text.Normalizer.normalize(texto.trim().lowercase(), java.text.Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}

private fun buscarContactoServicioPorReporte(
    reporte: Reporte,
    categorias: List<Categoria>
): ContactoServicio? {
    // 1. Buscar primero por el campo textual reporte.categoria
    val catDirecta = reporte.categoria?.trim() ?: ""
    if (catDirecta.isNotBlank()) {
        val normDirecta = normalizarTextoServicio(catDirecta)
        val matchDirecto = DIRECTORIO_SERVICIOS_OTB.find { contacto ->
            val normContacto = normalizarTextoServicio(contacto.categoriaNombre)
            normDirecta == normContacto ||
                normDirecta.contains(normContacto) ||
                normContacto.contains(normDirecta) ||
                contacto.etiquetasAlias.any { alias -> normDirecta.contains(alias) || alias.contains(normDirecta) }
        }
        if (matchDirecto != null) return matchDirecto
    }

    // 2. Buscar por el idCategoria asociado en la lista de categorias
    val idCat = reporte.idCategoria?.trim() ?: ""
    if (idCat.isNotBlank()) {
        val catEncontrada = categorias.find { it.idCategoria?.trim() == idCat }
        if (catEncontrada != null && catEncontrada.nombre.isNotBlank()) {
            val normCat = normalizarTextoServicio(catEncontrada.nombre)
            val matchId = DIRECTORIO_SERVICIOS_OTB.find { contacto ->
                val normContacto = normalizarTextoServicio(contacto.categoriaNombre)
                normCat == normContacto ||
                    normCat.contains(normContacto) ||
                    normContacto.contains(normCat) ||
                    contacto.etiquetasAlias.any { alias -> normCat.contains(alias) || alias.contains(normCat) }
            }
            if (matchId != null) return matchId
        }
    }

    return null
}
