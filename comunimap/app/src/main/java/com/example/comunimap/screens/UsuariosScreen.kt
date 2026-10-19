package com.example.comunimap.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.ActualizarUsuarioRequest
import com.example.comunimap.models.CambiarEstadoUsuarioRequest
import com.example.comunimap.models.CrearUsuarioRequest
import com.example.comunimap.models.Usuario
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA: GESTIÓN DE USUARIOS (ADMINISTRADOR)
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    usuarioActual: Usuario? = null,
    esAdministrador: Boolean = true
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }

    // Filtros y búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var selectedRolFilter by remember { mutableStateOf("Todos") }
    var selectedEstadoFilter by remember { mutableStateOf("Todos") } // "Todos", "Pendientes", "Activos"

    // Diálogos modales
    var showCrearDialog by remember { mutableStateOf(false) }
    var usuarioAEditar by remember { mutableStateOf<Usuario?>(null) }
    var usuarioACambiarEstado by remember { mutableStateOf<Usuario?>(null) }
    var usuarioAAprobarDirecto by remember { mutableStateOf<Usuario?>(null) }

    var idEnProcesoActivacion by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Consulta de la lista de usuarios reales desde Railway
    suspend fun consultarUsuarios() {
        isLoading = true
        errorMessage = null
        try {
            val response = ApiClient.apiService.obtenerUsuarios()
            Log.d("USUARIOS_HTTP", "HTTP: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Log.d("USUARIOS_RESPONSE", "Usuarios recibidos: ${body.usuarios.size}")
                    usuarios = body.usuarios
                } else {
                    errorMessage = "La API respondió con un error."
                }
            } else {
                errorMessage = "Error del servidor: HTTP ${response.code()}"
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (exception: Exception) {
            Log.e("USUARIOS_ERROR", "Error consultando usuarios", exception)
            errorMessage = "No se pudo conectar con el servidor."
        } finally {
            isLoading = false
        }
    }

    // Activar usuario reutilizando cambiar_estado_usuario.php
    fun activarUsuario(usuario: Usuario) {
        val id = usuario.idUsuario ?: return
        coroutineScope.launch {
            idEnProcesoActivacion = id
            try {
                val req = CambiarEstadoUsuarioRequest(
                    idUsuario = id,
                    estado = "Activo"
                )
                Log.d("USUARIOS_ACTIVACION", "POST cambiar_estado_usuario.php para usuario $id a Activo")
                val resp = ApiClient.apiService.cambiarEstadoUsuario(req)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    consultarUsuarios()
                    snackbarHostState.showSnackbar("Usuario activado correctamente")
                } else {
                    val msg = resp.body()?.message ?: "Error al activar usuario: HTTP ${resp.code()}"
                    snackbarHostState.showSnackbar(msg)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (ex: Exception) {
                Log.e("USUARIOS_ACTIVACION", "Error al activar usuario", ex)
                snackbarHostState.showSnackbar("No se pudo conectar con el servidor.")
            } finally {
                idEnProcesoActivacion = null
            }
        }
    }

    LaunchedEffect(Unit) {
        consultarUsuarios()
    }

    // Lista de usuarios pendientes de validación (estado = "Inactivo")
    val usuariosPendientes = remember(usuarios) {
        usuarios.filter { it.estado.equals("Inactivo", ignoreCase = true) }
    }

    // Filtrado de usuarios en memoria (sin alterar los datos de la fuente)
    val usuariosFiltrados = usuarios.filter { u ->
        val matchesSearch = searchQuery.isBlank() ||
                u.nombre.contains(searchQuery, ignoreCase = true) ||
                u.email.contains(searchQuery, ignoreCase = true) ||
                (u.telefono ?: "").contains(searchQuery, ignoreCase = true)

        val matchesRol = selectedRolFilter == "Todos" ||
                u.rol.equals(selectedRolFilter, ignoreCase = true)

        val matchesEstado = when (selectedEstadoFilter) {
            "Pendientes" -> u.estado.equals("Inactivo", ignoreCase = true)
            "Activos" -> u.estado.equals("Activo", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesRol && matchesEstado
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Usuarios", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { coroutineScope.launch { consultarUsuarios() } },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("usuarios_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar usuarios")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCrearDialog = true },
                modifier = Modifier.testTag("crear_usuario_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear nuevo usuario")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Campo de búsqueda por nombre, email o teléfono
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre, correo o teléfono") },
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
                    .testTag("search_usuarios_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // =====================================================
            // SECCIÓN DESTACADA: USUARIOS PENDIENTES DE VALIDACIÓN
            // =====================================================
            if (usuariosPendientes.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seccion_usuarios_pendientes"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF78350F).copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(16.dp)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pendientes de validación",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF3C7),
                                    fontSize = 15.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "${usuariosPendientes.size} pendiente${if (usuariosPendientes.size > 1) "s" else ""}",
                                    color = Color(0xFF1E1B4B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hay vecinos registrados esperando tu aprobación para acceder a la aplicación.",
                            color = Color(0xFFFDE68A),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    selectedEstadoFilter = if (selectedEstadoFilter == "Pendientes") "Todos" else "Pendientes"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedEstadoFilter == "Pendientes") Color(0xFFB45309) else Color(0xFFD97706)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_filtrar_pendientes_banner")
                            ) {
                                Icon(
                                    imageVector = if (selectedEstadoFilter == "Pendientes") Icons.Default.Clear else Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedEstadoFilter == "Pendientes") "Mostrar todos los usuarios" else "Ver pendientes (${usuariosPendientes.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // =====================================================
            // FILTROS: ESTADO Y ROL
            // =====================================================
            Text(
                text = "Filtrar por estado:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedEstadoFilter == "Todos",
                        onClick = { selectedEstadoFilter = "Todos" },
                        label = { Text("Todos (${usuarios.size})") },
                        modifier = Modifier.testTag("filtro_estado_todos")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedEstadoFilter == "Pendientes",
                        onClick = { selectedEstadoFilter = "Pendientes" },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (usuariosPendientes.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFF59E0B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("Pendientes (${usuariosPendientes.size})")
                            }
                        },
                        colors = if (usuariosPendientes.isNotEmpty()) {
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                selectedLabelColor = Color(0xFFF59E0B)
                            )
                        } else FilterChipDefaults.filterChipColors(),
                        modifier = Modifier.testTag("filtro_estado_pendientes")
                    )
                }
                item {
                    val activosCount = usuarios.count { it.estado.equals("Activo", ignoreCase = true) }
                    FilterChip(
                        selected = selectedEstadoFilter == "Activos",
                        onClick = { selectedEstadoFilter = "Activos" },
                        label = { Text("Activos ($activosCount)") },
                        modifier = Modifier.testTag("filtro_estado_activos")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Filtrar por rol:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val roles = listOf("Todos", "Administrador", "Usuario")
                items(roles) { rol ->
                    FilterChip(
                        selected = selectedRolFilter == rol,
                        onClick = { selectedRolFilter = rol },
                        label = { Text(rol) },
                        modifier = Modifier.testTag("filtro_rol_$rol")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // =====================================================
            // ESTADOS DE VISUALIZACIÓN / LISTA
            // =====================================================
            if (isLoading && usuarios.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando usuarios...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else if (errorMessage != null && usuarios.isEmpty()) {
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
                        Button(onClick = { coroutineScope.launch { consultarUsuarios() } }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (usuariosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (selectedEstadoFilter == "Pendientes") {
                                "No hay usuarios pendientes de validación."
                            } else {
                                "No se encontraron usuarios con los criterios indicados."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedEstadoFilter != "Todos" || selectedRolFilter != "Todos" || searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = {
                                selectedEstadoFilter = "Todos"
                                selectedRolFilter = "Todos"
                                searchQuery = ""
                            }) {
                                Text("Restablecer filtros")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("usuarios_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(usuariosFiltrados, key = { it.idUsuario ?: it.email }) { usuario ->
                        UsuarioItemCard(
                            usuario = usuario,
                            isApproving = idEnProcesoActivacion == usuario.idUsuario,
                            onAprobar = { usuarioAAprobarDirecto = usuario },
                            onEditar = { usuarioAEditar = usuario },
                            onCambiarEstado = { usuarioACambiarEstado = usuario }
                        )
                    }
                }
            }
        }
    }

    // Diálogo: Confirmar Aprobación / Activación directa
    usuarioAAprobarDirecto?.let { usuario ->
        AlertDialog(
            onDismissRequest = { if (idEnProcesoActivacion == null) usuarioAAprobarDirecto = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Aprobar Cuenta", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Deseas activar y autorizar el acceso para este usuario?")
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = usuario.email, fontSize = 13.sp)
                            if (!usuario.telefono.isNullOrBlank()) {
                                Text(text = "Teléfono: ${usuario.telefono}", fontSize = 12.sp)
                            }
                            Text(
                                text = "Rol: ${usuario.rol}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = "El vecino podrá iniciar sesión inmediatamente después de ser activado.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val usuarioParaActivar = usuario
                        usuarioAAprobarDirecto = null
                        activarUsuario(usuarioParaActivar)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    enabled = idEnProcesoActivacion == null,
                    modifier = Modifier.testTag("btn_confirmar_aprobacion")
                ) {
                    Text("Aprobar y Activar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { usuarioAAprobarDirecto = null },
                    enabled = idEnProcesoActivacion == null,
                    modifier = Modifier.testTag("btn_cancelar_aprobacion")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo: Crear Usuario
    if (showCrearDialog) {
        CrearUsuarioDialog(
            onDismiss = { showCrearDialog = false },
            onUsuarioCreado = {
                showCrearDialog = false
                coroutineScope.launch {
                    consultarUsuarios()
                    snackbarHostState.showSnackbar("Usuario creado con éxito")
                }
            }
        )
    }

    // Diálogo: Editar Usuario
    usuarioAEditar?.let { usuario ->
        EditarUsuarioDialog(
            usuario = usuario,
            onDismiss = { usuarioAEditar = null },
            onUsuarioEditado = {
                usuarioAEditar = null
                coroutineScope.launch {
                    consultarUsuarios()
                    snackbarHostState.showSnackbar("Usuario actualizado con éxito")
                }
            }
        )
    }

    // Diálogo: Cambiar Estado
    usuarioACambiarEstado?.let { usuario ->
        CambiarEstadoUsuarioDialog(
            usuario = usuario,
            onDismiss = { usuarioACambiarEstado = null },
            onEstadoCambiado = {
                usuarioACambiarEstado = null
                coroutineScope.launch {
                    consultarUsuarios()
                    snackbarHostState.showSnackbar("Estado de usuario actualizado")
                }
            }
        )
    }
}

// Tarjeta individual para usuario con sección destacada de aprobación
@Composable
private fun UsuarioItemCard(
    usuario: Usuario,
    isApproving: Boolean = false,
    onAprobar: () -> Unit,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    val esActivo = usuario.estado.equals("Activo", ignoreCase = true)
    val esPendiente = !esActivo

    val colorEstado = if (esActivo) Color(0xFF2E7D32) else Color(0xFFD97706)
    val borderStroke = if (esPendiente) BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)) else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("usuario_card_${usuario.idUsuario ?: "item"}"),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esPendiente) 3.dp else 1.5.dp),
        border = borderStroke,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Cabecera de la tarjeta: Nombre y Chip de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (esActivo) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (esPendiente) "Pendiente" else usuario.estado,
                        fontWeight = FontWeight.Bold,
                        color = colorEstado,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Teléfono: ${usuario.telefono ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rol: ${usuario.rol}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción según el estado del usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (esPendiente) {
                    // Botón principal de Aprobación para cuentas pendientes
                    Button(
                        onClick = onAprobar,
                        enabled = !isApproving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_aprobar_usuario_${usuario.idUsuario ?: "item"}")
                    ) {
                        if (isApproving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Activando...", fontSize = 12.sp, color = Color.White)
                        } else {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Aprobar cuenta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Botón para desactivar usuario activo
                    TextButton(onClick = onCambiarEstado) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Desactivar", fontSize = 12.sp)
                    }
                }

                // Acciones secundarias (Editar y Cambiar Estado)
                Row {
                    if (esPendiente) {
                        TextButton(onClick = onCambiarEstado) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Estado", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    TextButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// Diálogo para crear nuevo usuario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearUsuarioDialog(
    onDismiss: () -> Unit,
    onUsuarioCreado: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("Usuario") }

    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Crear Usuario") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña *") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Rol (Administrador o Usuario)
                Text("Rol:", style = MaterialTheme.typography.labelMedium)
                var rolExpanded by remember { mutableStateOf(false) }
                val rolesPermitidos = listOf("Usuario", "Administrador")
                ExposedDropdownMenuBox(
                    expanded = rolExpanded,
                    onExpandedChange = { rolExpanded = it }
                ) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = rolExpanded,
                        onDismissRequest = { rolExpanded = false }
                    ) {
                        rolesPermitidos.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    rol = r
                                    rolExpanded = false
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
                    if (nombre.isBlank() || email.isBlank() || password.isBlank() || telefono.isBlank()) {
                        formError = "Por favor completa todos los campos requeridos."
                        return@Button
                    }
                    coroutineScope.launch {
                        isSubmitting = true
                        formError = null
                        try {
                            val req = CrearUsuarioRequest(
                                nombre = nombre.trim(),
                                email = email.trim(),
                                password = password.trim(),
                                telefono = telefono.trim(),
                                rol = rol
                            )
                            Log.d("API_MUTATION", "POST crear_usuario.php para email: ${req.email}")
                            val resp = ApiClient.apiService.crearUsuario(req)
                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onUsuarioCreado()
                            } else {
                                formError = resp.body()?.message ?: "Error al crear usuario: HTTP ${resp.code()}"
                            }
                        } catch (ex: Exception) {
                            Log.e("API_ERROR", "Error creando usuario", ex)
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
                    Text("Crear")
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

// Diálogo para editar usuario existente
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarUsuarioDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onUsuarioEditado: () -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var email by remember { mutableStateOf(usuario.email) }
    var telefono by remember { mutableStateOf(usuario.telefono ?: "") }
    var rol by remember { mutableStateOf(usuario.rol) }
    var estado by remember { mutableStateOf(usuario.estado) }

    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Editar Usuario") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Rol
                Text("Rol:", style = MaterialTheme.typography.labelMedium)
                var rolExpanded by remember { mutableStateOf(false) }
                val rolesPermitidos = listOf("Usuario", "Administrador")
                ExposedDropdownMenuBox(
                    expanded = rolExpanded,
                    onExpandedChange = { rolExpanded = it }
                ) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = rolExpanded,
                        onDismissRequest = { rolExpanded = false }
                    ) {
                        rolesPermitidos.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    rol = r
                                    rolExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selector de Estado
                Text("Estado:", style = MaterialTheme.typography.labelMedium)
                var estadoExpanded by remember { mutableStateOf(false) }
                val estadosPermitidos = listOf("Activo", "Inactivo")
                ExposedDropdownMenuBox(
                    expanded = estadoExpanded,
                    onExpandedChange = { estadoExpanded = it }
                ) {
                    OutlinedTextField(
                        value = estado,
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
                                    estado = est
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
                    val idUsuario = usuario.idUsuario
                    if (idUsuario == null) {
                        formError = "ID de usuario inválido."
                        return@Button
                    }
                    coroutineScope.launch {
                        isSubmitting = true
                        formError = null
                        try {
                            val req = ActualizarUsuarioRequest(
                                idUsuario = idUsuario,
                                nombre = nombre.trim(),
                                email = email.trim(),
                                telefono = telefono.trim(),
                                rol = rol,
                                estado = estado
                            )
                            Log.d("API_MUTATION", "POST actualizar_usuario.php id: $idUsuario")
                            val resp = ApiClient.apiService.actualizarUsuario(req)
                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onUsuarioEditado()
                            } else {
                                formError = resp.body()?.message ?: "Error al actualizar: HTTP ${resp.code()}"
                            }
                        } catch (ex: Exception) {
                            Log.e("API_ERROR", "Error actualizando usuario", ex)
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

// Diálogo para cambiar el estado de un usuario (Activo / Inactivo)
@Composable
private fun CambiarEstadoUsuarioDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onEstadoCambiado: () -> Unit
) {
    val nuevoEstado = if (usuario.estado.equals("Activo", ignoreCase = true)) "Inactivo" else "Activo"
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Cambiar Estado de Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Deseas cambiar el estado del usuario?")
                Text(
                    text = "${usuario.nombre} (${usuario.email})",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Estado actual: ${usuario.estado} ➔ Nuevo estado: $nuevoEstado",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (errorMsg != null) {
                    Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idUsuario = usuario.idUsuario ?: return@Button
                    coroutineScope.launch {
                        isSubmitting = true
                        errorMsg = null
                        try {
                            val req = CambiarEstadoUsuarioRequest(
                                idUsuario = idUsuario,
                                estado = nuevoEstado
                            )
                            Log.d("API_MUTATION", "POST cambiar_estado_usuario.php id: $idUsuario a $nuevoEstado")
                            val resp = ApiClient.apiService.cambiarEstadoUsuario(req)
                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onEstadoCambiado()
                            } else {
                                errorMsg = resp.body()?.message ?: "Error al cambiar estado: HTTP ${resp.code()}"
                            }
                        } catch (ex: Exception) {
                            Log.e("API_ERROR", "Error cambiando estado usuario", ex)
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
                    Text("Confirmar")
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
