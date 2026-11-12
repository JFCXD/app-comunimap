package com.example.comunimap.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.ComunidadAdminRequest
import com.example.comunimap.models.ComunidadAdminResponse
import com.example.comunimap.models.ComunidadSubirImagenResponse
import com.example.comunimap.models.OpcionEncuesta
import com.example.comunimap.models.PublicacionComunidad
import com.example.comunimap.models.Usuario
import com.example.comunimap.models.VotarComunidadRequest
import com.example.comunimap.utils.PreferenciasManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// Paleta visual de ComuniMap
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkCardBg = Color(0xFF162238)
private val DarkCardBorder = Color(0xFF1E293B)
private val AccentBlue = Color(0xFF2563EB)
private val AccentCyan = Color(0xFF38BDF8)
private val ColorComunicado = Color(0xFF3B82F6) // Azul
private val ColorActividad = Color(0xFFF59E0B)  // Ámbar
private val ColorEncuesta = Color(0xFF10B981)   // Verde

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunidadScreen(
    usuarioActual: Usuario? = null,
    esAdministrador: Boolean = false,
    tokenApi: String? = null,
    idPublicacionObjetivo: Int? = null,
    onPublicacionObjetivoProcesada: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Rol y token
    val esAdmin = esAdministrador || (usuarioActual?.rol?.trim()?.equals("Administrador", ignoreCase = true) == true)
    val token = tokenApi ?: PreferenciasManager.obtenerTokenApi(context)

    // Estados de datos y paginación
    var publicaciones by remember { mutableStateOf<List<PublicacionComunidad>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estado del LazyList para auto-scroll hacia la publicación notificada
    val listState = rememberLazyListState()
    var idPublicacionResaltada by remember { mutableStateOf<Int?>(null) }

    // Estados para diálogos administrativos
    var showCrearDialog by remember { mutableStateOf(false) }
    var publicacionAEditar by remember { mutableStateOf<PublicacionComunidad?>(null) }
    var publicacionACambiarEstado by remember { mutableStateOf<PublicacionComunidad?>(null) }

    // Función para consultar publicaciones desde la API real
    fun cargarPublicaciones(page: Int, append: Boolean = false) {
        if (token.isNullOrBlank()) {
            errorMessage = "Sesión no válida o caducada. Inicia sesión nuevamente."
            return
        }

        coroutineScope.launch {
            if (append) isLoadingMore = true else isLoading = true
            errorMessage = null
            try {
                val authHeader = "Bearer $token"
                val administrarParam = if (esAdmin) 1 else null
                Log.d("COMUNIDAD_API", "GET comunidad.php?page=$page&administrar=$administrarParam")

                val resp = ApiClient.apiService.obtenerPublicacionesComunidad(
                    authHeader = authHeader,
                    page = page,
                    administrar = administrarParam
                )

                if (resp.isSuccessful && resp.body()?.success == true) {
                    val body = resp.body()!!
                    currentPage = body.page
                    hasMore = body.hasMore

                    val nuevasPubs = body.publicaciones
                    publicaciones = if (append) {
                        // Evitar duplicados por idPublicacion
                        val idsExistentes = publicaciones.mapNotNull { it.idPublicacionInt }.toSet()
                        publicaciones + nuevasPubs.filter { it.idPublicacionInt !in idsExistentes }
                    } else {
                        nuevasPubs
                    }
                    Log.d("COMUNIDAD_API", "Cargadas ${publicaciones.size} publicaciones. hasMore=$hasMore")
                } else {
                    when (resp.code()) {
                        401 -> errorMessage = "Tu sesión ha caducado. Inicia sesión nuevamente."
                        403 -> errorMessage = "No tienes permiso para ver estas publicaciones."
                        else -> errorMessage = "No se pudieron cargar las publicaciones comunitarias."
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (ex: Exception) {
                Log.e("COMUNIDAD_API", "Error al consultar publicaciones", ex)
                errorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a Internet."
            } finally {
                isLoading = false
                isLoadingMore = false
            }
        }
    }

    // Carga inicial
    LaunchedEffect(esAdmin, token) {
        cargarPublicaciones(page = 1, append = false)
    }

    // Desplazamiento y resaltado cuando se abre una publicación desde una notificación
    LaunchedEffect(idPublicacionObjetivo, isLoading) {
        val targetId = idPublicacionObjetivo ?: return@LaunchedEffect
        if (isLoading) return@LaunchedEffect

        // 1. Buscar en la lista ya cargada
        val index = publicaciones.indexOfFirst { it.idPublicacionInt == targetId }
        if (index != -1) {
            idPublicacionResaltada = targetId
            listState.animateScrollToItem(index)
            onPublicacionObjetivoProcesada()
        } else if (hasMore && !token.isNullOrBlank()) {
            // 2. Si no está en la página inicial, buscar en las siguientes páginas (máx 5 páginas)
            var encontrada = false
            var intentos = 0
            val maxIntentos = 5
            var paginaActual = currentPage

            while (!encontrada && hasMore && intentos < maxIntentos) {
                intentos++
                val siguientePagina = paginaActual + 1
                try {
                    val authHeader = "Bearer $token"
                    val administrarParam = if (esAdmin) 1 else null
                    val resp = ApiClient.apiService.obtenerPublicacionesComunidad(
                        authHeader = authHeader,
                        page = siguientePagina,
                        administrar = administrarParam
                    )
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        val body = resp.body()!!
                        currentPage = body.page
                        hasMore = body.hasMore
                        paginaActual = body.page
                        val nuevasPubs = body.publicaciones
                        val idsExistentes = publicaciones.mapNotNull { it.idPublicacionInt }.toSet()
                        publicaciones = publicaciones + nuevasPubs.filter { it.idPublicacionInt !in idsExistentes }

                        val nuevoIndex = publicaciones.indexOfFirst { it.idPublicacionInt == targetId }
                        if (nuevoIndex != -1) {
                            encontrada = true
                            idPublicacionResaltada = targetId
                            listState.animateScrollToItem(nuevoIndex)
                            onPublicacionObjetivoProcesada()
                            break
                        }
                    } else {
                        break
                    }
                } catch (e: Exception) {
                    break
                }
            }

            if (!encontrada) {
                snackbarHostState.showSnackbar("La publicación solicitada ya no se encuentra disponible.")
                onPublicacionObjetivoProcesada()
            }
        } else {
            snackbarHostState.showSnackbar("La publicación solicitada ya no se encuentra disponible.")
            onPublicacionObjetivoProcesada()
        }
    }

    // Función para registrar voto en una encuesta
    fun registrarVoto(idPublicacion: Int, idOpcion: Int, onCompletado: (Boolean) -> Unit) {
        if (token.isNullOrBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Tu sesión ha caducado. Inicia sesión nuevamente.")
            }
            onCompletado(false)
            return
        }

        coroutineScope.launch {
            try {
                val authHeader = "Bearer $token"
                val req = VotarComunidadRequest(idPublicacion = idPublicacion, idOpcion = idOpcion)
                Log.d("COMUNIDAD_VOTO", "POST votar_comunidad.php pub=$idPublicacion op=$idOpcion")

                val resp = ApiClient.apiService.votarComunidad(authHeader, req)

                if (resp.isSuccessful && resp.body()?.success == true) {
                    snackbarHostState.showSnackbar("¡Voto registrado con éxito!")
                    // Recargar publicaciones para mostrar resultados reales devueltos por MySQL
                    cargarPublicaciones(page = 1, append = false)
                    onCompletado(true)
                } else {
                    val errorMsg = extraerMensajeErrorApi(resp, "No se pudo registrar tu voto")
                    snackbarHostState.showSnackbar(errorMsg)
                    // Si ya votó o finalizó, recargar para sincronizar estado real
                    if (resp.code() == 409) {
                        cargarPublicaciones(page = 1, append = false)
                    }
                    onCompletado(false)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (ex: Exception) {
                Log.e("COMUNIDAD_VOTO", "Error al registrar voto", ex)
                snackbarHostState.showSnackbar("No se pudo conectar con el servidor.")
                onCompletado(false)
            }
        }
    }

    Scaffold(
        containerColor = DarkNavyBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavyBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (onNavigateBack != null) 4.dp else 16.dp,
                            end = 6.dp,
                            top = 5.dp,
                            bottom = 5.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("comunidad_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Comunidad",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "Comunicados, actividades y encuestas de nuestra OTB.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = { cargarPublicaciones(page = 1, append = false) },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("comunidad_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar publicaciones",
                            tint = Color(0xFF60A5FA)
                        )
                    }
                }
                HorizontalDivider(
                    color = DarkCardBorder.copy(alpha = 0.7f),
                    thickness = 1.dp
                )
            }
        },
        floatingActionButton = {
            // Botón exclusivo de Administrador para crear publicaciones
            if (esAdmin) {
                FloatingActionButton(
                    onClick = { showCrearDialog = true },
                    containerColor = AccentBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("comunidad_fab_crear")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear publicación",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Estado de carga inicial
                isLoading && publicaciones.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Cargando publicaciones de la comunidad...",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }

                // Error de conexión con opción de reintento
                errorMessage != null && publicaciones.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "No se pudieron cargar las publicaciones.",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cargarPublicaciones(page = 1, append = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }

                // Muro vacío
                publicaciones.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Todavía no hay publicaciones de la OTB.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pronto se compartirán avisos, actividades vecinales y encuestas.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { cargarPublicaciones(page = 1, append = false) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Text("Actualizar muro", color = Color(0xFF38BDF8))
                        }
                    }
                }

                // Lista de publicaciones con paginación
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .testTag("comunidad_list"),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = publicaciones,
                            key = { pub -> "pub_${pub.idPublicacionInt ?: pub.idPublicacion ?: pub.titulo}_${pub.tipo}" }
                        ) { pub ->
                            PublicacionCard(
                                publicacion = pub,
                                esAdmin = esAdmin,
                                estaResaltada = pub.idPublicacionInt != null && pub.idPublicacionInt == idPublicacionResaltada,
                                onVotar = { idPub, idOp, callback ->
                                    registrarVoto(idPub, idOp, callback)
                                },
                                onEditar = {
                                    publicacionAEditar = pub
                                },
                                onCambiarEstado = {
                                    publicacionACambiarEstado = pub
                                }
                            )
                        }

                        // Elemento de paginación
                        if (hasMore) {
                            item(key = "comunidad_has_more_item") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoadingMore) {
                                        CircularProgressIndicator(
                                            color = AccentCyan,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                cargarPublicaciones(page = currentPage + 1, append = true)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                            modifier = Modifier.testTag("comunidad_cargar_mas_btn")
                                        ) {
                                            Text(
                                                text = "Cargar más publicaciones",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =====================================================
    // DIÁLOGOS ADMINISTRATIVOS
    // =====================================================

    // Diálogo: Crear Publicación (Comunicado, Actividad o Encuesta)
    if (showCrearDialog && esAdmin && !token.isNullOrBlank()) {
        CrearPublicacionDialog(
            token = token,
            onDismiss = { showCrearDialog = false },
            onCreadoExitosamente = {
                showCrearDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Publicación creada exitosamente")
                    cargarPublicaciones(page = 1, append = false)
                }
            }
        )
    }

    // Diálogo: Editar Publicación
    publicacionAEditar?.let { pub ->
        EditarPublicacionDialog(
            publicacion = pub,
            token = token ?: "",
            onDismiss = { publicacionAEditar = null },
            onEditadoExitosamente = {
                publicacionAEditar = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Publicación actualizada correctamente")
                    cargarPublicaciones(page = 1, append = false)
                }
            }
        )
    }

    // Diálogo: Cambiar Estado
    publicacionACambiarEstado?.let { pub ->
        CambiarEstadoPublicacionDialog(
            publicacion = pub,
            token = token ?: "",
            onDismiss = { publicacionACambiarEstado = null },
            onEstadoCambiado = {
                publicacionACambiarEstado = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Estado de la publicación actualizado")
                    cargarPublicaciones(page = 1, append = false)
                }
            }
        )
    }
}

// =====================================================
// TARJETA DE PUBLICACIÓN (COMUNICADO, ACTIVIDAD, ENCUESTA)
// =====================================================

@Composable
private fun PublicacionCard(
    publicacion: PublicacionComunidad,
    esAdmin: Boolean,
    estaResaltada: Boolean = false,
    onVotar: (Int, Int, (Boolean) -> Unit) -> Unit,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    val colorTipo = when (publicacion.tipo) {
        "Actividad" -> ColorActividad
        "Encuesta" -> ColorEncuesta
        else -> ColorComunicado
    }

    val iconoTipo = when (publicacion.tipo) {
        "Actividad" -> Icons.Default.Event
        "Encuesta" -> Icons.Default.Poll
        else -> Icons.Default.Campaign
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("publicacion_card_${publicacion.idPublicacionInt ?: "item"}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(
            width = if (estaResaltada) 2.dp else 1.dp,
            color = if (estaResaltada) AccentCyan else DarkCardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (estaResaltada) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Indicador visual si fue abierta desde una notificación
            if (estaResaltada) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Publicación destacada",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            // Cabecera: Tipo de publicación, fecha y estado administrativo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge del tipo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorTipo.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, colorTipo.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = iconoTipo,
                        contentDescription = null,
                        tint = colorTipo,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = publicacion.tipo.uppercase(),
                        color = colorTipo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge de estado solo para Administrador
                    if (esAdmin && !publicacion.estado.isNullOrBlank()) {
                        val colorEstado = when (publicacion.estado.trim()) {
                            "Publicado" -> Color(0xFF10B981)
                            "Borrador" -> Color(0xFF94A3B8)
                            "Archivado" -> Color(0xFFF97316)
                            else -> Color(0xFF94A3B8)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colorEstado.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = publicacion.estado,
                                color = colorEstado,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Fecha de creación
                    publicacion.fechaCreacion?.let { fecha ->
                        Text(
                            text = formatearFechaLegible(fecha),
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Título
            Text(
                text = publicacion.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Imagen opcional si imagenUrl está disponible
            val urlImg = publicacion.imagenUrl ?: publicacion.imagen
            if (!urlImg.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = urlImg,
                    contentDescription = "Imagen de ${publicacion.titulo}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = AccentCyan,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        // Ocultar limpiamente en caso de error sin romper la tarjeta
                    }
                )
            }

            // Descripción
            Text(
                text = publicacion.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1)
            )

            // =====================================================
            // SECCIÓN ESPECÍFICA SEGÚN TIPO
            // =====================================================

            when (publicacion.tipo) {
                "Actividad" -> {
                    SeccionActividadDetalle(publicacion = publicacion)
                }

                "Encuesta" -> {
                    SeccionEncuestaDetalle(
                        publicacion = publicacion,
                        onVotar = onVotar
                    )
                }
            }

            // Controles de administración exclusivos (Editar / Cambiar estado)
            if (esAdmin) {
                HorizontalDivider(
                    color = DarkCardBorder,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCambiarEstado,
                        modifier = Modifier.testTag("btn_estado_pub_${publicacion.idPublicacionInt ?: "item"}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Estado", fontSize = 12.sp, color = Color(0xFF60A5FA))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextButton(
                        onClick = onEditar,
                        modifier = Modifier.testTag("btn_editar_pub_${publicacion.idPublicacionInt ?: "item"}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 12.sp, color = Color(0xFF38BDF8))
                    }
                }
            }
        }
    }
}

// =====================================================
// COMPONENTE: DETALLE DE ACTIVIDAD (FECHA, HORA, LUGAR)
// =====================================================

@Composable
private fun SeccionActividadDetalle(publicacion: PublicacionComunidad) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fecha
            if (!publicacion.fechaActividad.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = ColorActividad,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha: ${formatearFechaLegible(publicacion.fechaActividad)}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Hora
            if (!publicacion.horaActividad.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = ColorActividad,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hora: ${publicacion.horaActividad}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Lugar
            if (!publicacion.lugarActividad.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ColorActividad,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lugar: ${publicacion.lugarActividad}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// =====================================================
// COMPONENTE: DETALLE DE ENCUESTA Y VOTACIÓN
// =====================================================

@Composable
private fun SeccionEncuestaDetalle(
    publicacion: PublicacionComunidad,
    onVotar: (Int, Int, (Boolean) -> Unit) -> Unit
) {
    val idPubInt = publicacion.idPublicacionInt ?: return
    val opciones = publicacion.opciones ?: emptyList()
    val totalVotos = publicacion.totalVotos ?: 0
    val yaVoto = publicacion.miVoto != null
    val abierta = publicacion.encuestaAbierta == true

    var selectedOpcionId by remember { mutableStateOf<Int?>(null) }
    var isSubmittingVoto by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Estado de la encuesta: Abierta / Finalizada y Cierre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Poll,
                        contentDescription = null,
                        tint = if (abierta) ColorEncuesta else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (abierta) "Encuesta abierta" else "Encuesta finalizada",
                        color = if (abierta) ColorEncuesta else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                if (!publicacion.fechaCierre.isNullOrBlank()) {
                    Text(
                        text = "Cierre: ${formatearFechaLegible(publicacion.fechaCierre)}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // CASO 1: EL USUARIO YA VOTÓ O LA ENCUESTA FINALIZÓ -> MOSTRAR RESULTADOS
            if (yaVoto || !abierta) {
                if (yaVoto) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ya participaste en esta encuesta",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Lista de resultados con barras de progreso y porcentaje
                opciones.forEach { opcion ->
                    val opIdInt = opcion.idOpcionInt
                    val esMiOpcion = opIdInt != null && opIdInt == publicacion.miVotoInt
                    val votosOpcion = opcion.totalVotos ?: 0
                    val porcentaje = if (totalVotos > 0) {
                        (votosOpcion.toFloat() / totalVotos.toFloat()) * 100f
                    } else 0f

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (esMiOpcion) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Tu voto",
                                        tint = ColorEncuesta,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = opcion.texto,
                                    color = if (esMiOpcion) ColorEncuesta else Color.White,
                                    fontWeight = if (esMiOpcion) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f %% (%d)", porcentaje, votosOpcion),
                                color = if (esMiOpcion) ColorEncuesta else Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = { porcentaje / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (esMiOpcion) ColorEncuesta else Color(0xFF0284C7),
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }

                Text(
                    text = "Total de votos: $totalVotos",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                // CASO 2: ENCUESTA ABIERTA Y USUARIO NO HA VOTADO -> FORMULARIO DE VOTACIÓN
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    opciones.forEach { opcion ->
                        val opIdInt = opcion.idOpcionInt ?: return@forEach
                        val isSelected = selectedOpcionId == opIdInt

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color.Transparent)
                                .clickable {
                                    if (!isSubmittingVoto) {
                                        selectedOpcionId = opIdInt
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (!isSubmittingVoto) {
                                        selectedOpcionId = opIdInt
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AccentBlue,
                                    unselectedColor = Color(0xFF94A3B8)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = opcion.texto,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (selectedOpcionId != null && !isSubmittingVoto) {
                                isSubmittingVoto = true
                                onVotar(idPubInt, selectedOpcionId!!) { exito ->
                                    isSubmittingVoto = false
                                }
                            }
                        },
                        enabled = selectedOpcionId != null && !isSubmittingVoto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comunidad_votar_btn_${idPubInt}"),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorEncuesta),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmittingVoto) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Registrando voto...", color = Color.White)
                        } else {
                            Icon(Icons.Default.HowToVote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Registrar mi voto", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// DIÁLOGO: CREAR PUBLICACIÓN (ADMINISTRADOR)
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearPublicacionDialog(
    token: String,
    onDismiss: () -> Unit,
    onCreadoExitosamente: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var tipoSeleccionado by remember { mutableStateOf("Comunicado") } // Comunicado, Actividad, Encuesta
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf("Publicado") } // Publicado, Borrador

    // Campos Actividad (almacenamiento interno y visual)
    var fechaActividadIso by remember { mutableStateOf("") } // ej: "2026-11-14"
    var fechaActividadVisible by remember { mutableStateOf("") } // ej: "14/11/2026"
    var horaActividad by remember { mutableStateOf("") } // ej: "16:00"
    var lugarActividad by remember { mutableStateOf("") }

    // Campos Encuesta (almacenamiento interno y visual)
    var fechaCierreIso by remember { mutableStateOf("") } // ej: "2026-11-15"
    var fechaCierreVisible by remember { mutableStateOf("") } // ej: "15/11/2026"
    var horaCierre by remember { mutableStateOf("") } // ej: "23:59"
    val opcionesEncuesta = remember { mutableStateListOf("", "") } // De 2 a 10 opciones

    // Fotografía opcional (solo para Comunicados y Actividades)
    var imagenSeleccionada by remember { mutableStateOf<Uri?>(null) }
    var uriFotoCamara by remember { mutableStateOf<Uri?>(null) }
    var archivoFotoCamara by remember { mutableStateOf<File?>(null) }

    // Control de flujo multi-paso para reintentos y prevención de duplicados
    var idPublicacionCreada by remember { mutableStateOf<Int?>(null) }
    var fotoSubidaExitosamente by remember { mutableStateOf(false) }

    var formError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    fun limpiarArchivosTemporales() {
        try {
            archivoFotoCamara?.delete()
            archivoFotoCamara = null
            uriFotoCamara = null
        } catch (_: Exception) {}
    }

    // Launcher de cámara mediante FileProvider existente del proyecto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        if (exito && uriFotoCamara != null) {
            imagenSeleccionada = uriFotoCamara
            formError = null
        }
    }

    // Launcher de galería fotográfica con Photo Picker del sistema (cero permisos intrusivos)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionada = uri
            formError = null
        }
    }

    fun abrirCamara() {
        try {
            val archivoFoto = File(context.cacheDir, "camara_comunidad_${System.currentTimeMillis()}.jpg")
            archivoFotoCamara = archivoFoto
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivoFoto
            )
            uriFotoCamara = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("CAMARA_COMUNIDAD", "Error al abrir la cámara", e)
            formError = "No se pudo abrir la cámara en este dispositivo."
        }
    }

    fun abrirGaleria() {
        try {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            Log.e("GALERIA_COMUNIDAD", "Error al abrir la galería", e)
            formError = "No se pudo abrir la galería de imágenes."
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                limpiarArchivosTemporales()
                onDismiss()
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear publicación", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector de Tipo de Publicación
                Text("Tipo de publicación", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Comunicado", "Actividad", "Encuesta").forEach { tipo ->
                        FilterChip(
                            selected = tipoSeleccionado == tipo,
                            onClick = {
                                tipoSeleccionado = tipo
                                formError = null
                            },
                            label = { Text(tipo, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Título (máximo 180 caracteres)
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { if (it.length <= 180) titulo = it },
                    label = { Text("Título *") },
                    supportingText = { Text("${titulo.length}/180", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_crear_pub_titulo"),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Descripción (máximo 10,000 caracteres)
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 10000) descripcion = it },
                    label = { Text("Descripción *") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_crear_pub_desc"),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Estado Inicial
                Text("Estado", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Publicado", "Borrador").forEach { est ->
                        FilterChip(
                            selected = estadoSeleccionado == est,
                            onClick = { estadoSeleccionado = est },
                            label = { Text(est, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (est == "Publicado") Color(0xFF10B981) else Color(0xFF64748B),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Campos dinámicos si es Actividad: Selectores visuales de fecha y hora
                if (tipoSeleccionado == "Actividad") {
                    HorizontalDivider(color = DarkCardBorder)
                    Text("Detalles de la Actividad", fontWeight = FontWeight.Bold, color = ColorActividad, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CampoSelectorVisual(
                            etiqueta = "Fecha *",
                            valorVisible = fechaActividadVisible,
                            placeholder = "Seleccionar fecha",
                            icono = Icons.Default.Event,
                            onClick = {
                                mostrarDatePicker(context, fechaActividadIso) { iso, legible ->
                                    fechaActividadIso = iso
                                    fechaActividadVisible = legible
                                    formError = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        CampoSelectorVisual(
                            etiqueta = "Hora *",
                            valorVisible = horaActividad,
                            placeholder = "Seleccionar hora",
                            icono = Icons.Default.AccessTime,
                            onClick = {
                                mostrarTimePicker(context, horaActividad) { hora ->
                                    horaActividad = hora
                                    formError = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = lugarActividad,
                        onValueChange = { if (it.length <= 255) lugarActividad = it },
                        label = { Text("Lugar *") },
                        placeholder = { Text("Sede de la OTB") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // =====================================================
                // SECCIÓN DE FOTOGRAFÍA (OPCIONAL) - COMUNICADO Y ACTIVIDAD
                // =====================================================
                if (tipoSeleccionado != "Encuesta") {
                    HorizontalDivider(color = DarkCardBorder)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fotografía",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Opcional",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (imagenSeleccionada == null) {
                            // Dos acciones: Tomar foto o Elegir de galería
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { abrirCamara() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("btn_comunidad_tomar_foto"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Tomar foto",
                                        modifier = Modifier.size(18.dp),
                                        tint = AccentCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tomar foto", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = { abrirGaleria() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("btn_comunidad_elegir_galeria"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Elegir de galería",
                                        modifier = Modifier.size(18.dp),
                                        tint = AccentBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Elegir de galería", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else {
                            // Vista previa con AsyncImage de Coil
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = imagenSeleccionada,
                                    contentDescription = "Vista previa de la fotografía",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .testTag("vista_previa_foto_comunidad"),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Botones de acción: Cambiar foto (Cámara / Galería) y Quitar foto
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { abrirCamara() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_comunidad_cambiar_camara"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cámara", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { abrirGaleria() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_comunidad_cambiar_galeria"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Galería", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        imagenSeleccionada = null
                                        uriFotoCamara = null
                                        fotoSubidaExitosamente = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_comunidad_quitar_foto"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFF87171))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quitar", fontSize = 11.sp, color = Color(0xFFF87171))
                                }
                            }
                        }
                    }
                }

                // Campos dinámicos si es Encuesta: Selectores visuales de fecha y hora de cierre
                if (tipoSeleccionado == "Encuesta") {
                    HorizontalDivider(color = DarkCardBorder)
                    Text("Configuración de la Encuesta", fontWeight = FontWeight.Bold, color = ColorEncuesta, fontSize = 13.sp)

                    Text("Fecha y hora de cierre *", color = Color(0xFFCBD5E1), fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CampoSelectorVisual(
                            etiqueta = "Fecha cierre *",
                            valorVisible = fechaCierreVisible,
                            placeholder = "Seleccionar fecha",
                            icono = Icons.Default.Event,
                            onClick = {
                                mostrarDatePicker(context, fechaCierreIso) { iso, legible ->
                                    fechaCierreIso = iso
                                    fechaCierreVisible = legible
                                    formError = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        CampoSelectorVisual(
                            etiqueta = "Hora cierre *",
                            valorVisible = horaCierre,
                            placeholder = "Seleccionar hora",
                            icono = Icons.Default.AccessTime,
                            onClick = {
                                mostrarTimePicker(context, horaCierre) { hora ->
                                    horaCierre = hora
                                    formError = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Opciones de respuesta (de 2 a 10)", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.sp)
                    opcionesEncuesta.forEachIndexed { index, opcion ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = opcion,
                                onValueChange = { opcionesEncuesta[index] = it },
                                label = { Text("Opción ${index + 1} *") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            if (opcionesEncuesta.size > 2) {
                                IconButton(onClick = { opcionesEncuesta.removeAt(index) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }

                    if (opcionesEncuesta.size < 10) {
                        TextButton(
                            onClick = { opcionesEncuesta.add("") },
                            colors = ButtonDefaults.textButtonColors(contentColor = ColorEncuesta)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar otra opción")
                        }
                    }
                }

                formError?.let { err ->
                    Text(text = err, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSubmitting) return@Button

                    if (titulo.trim().isBlank()) {
                        formError = "El título es obligatorio."
                        return@Button
                    }
                    if (descripcion.trim().isBlank()) {
                        formError = "La descripción es obligatoria."
                        return@Button
                    }

                    if (tipoSeleccionado == "Actividad") {
                        if (fechaActividadIso.isBlank()) {
                            formError = "Selecciona la fecha de la actividad."
                            return@Button
                        }
                        if (horaActividad.isBlank()) {
                            formError = "Selecciona la hora de la actividad."
                            return@Button
                        }
                        if (lugarActividad.trim().isBlank()) {
                            formError = "El lugar de la actividad es obligatorio."
                            return@Button
                        }
                    }

                    var fechaCierreFinal: String? = null
                    if (tipoSeleccionado == "Encuesta") {
                        val (valido, errCierre) = validarFechaCierreFutura(fechaCierreIso, horaCierre)
                        if (!valido) {
                            formError = errCierre ?: "La fecha de cierre debe ser futura."
                            return@Button
                        }

                        val opcionesLimpias = opcionesEncuesta.map { it.trim() }
                        if (opcionesLimpias.size < 2) {
                            formError = "Ingresa al menos 2 opciones de respuesta distintas."
                            return@Button
                        }
                        if (opcionesLimpias.size > 10) {
                            formError = "No puedes ingresar más de 10 opciones de respuesta."
                            return@Button
                        }
                        if (opcionesLimpias.any { it.isBlank() }) {
                            formError = "Ninguna opción de respuesta puede estar vacía."
                            return@Button
                        }
                        val repetidas = opcionesLimpias.groupBy { it.lowercase(Locale.ROOT) }.filter { it.value.size > 1 }
                        if (repetidas.isNotEmpty()) {
                            formError = "No puede haber opciones duplicadas en la encuesta."
                            return@Button
                        }

                        fechaCierreFinal = "${fechaCierreIso.trim()} ${horaCierre.trim()}:00"
                    }

                    formError = null
                    isSubmitting = true

                    coroutineScope.launch {
                        var tempArchivoSubida: File? = null
                        try {
                            val authHeader = "Bearer $token"

                            // CASO A: Sin fotografía seleccionada (o tipo Encuesta)
                            if (imagenSeleccionada == null || tipoSeleccionado == "Encuesta") {
                                val req = ComunidadAdminRequest(
                                    accion = "crear",
                                    tipo = tipoSeleccionado,
                                    titulo = titulo.trim(),
                                    descripcion = descripcion.trim(),
                                    estado = estadoSeleccionado,
                                    fechaActividad = if (tipoSeleccionado == "Actividad") fechaActividadIso.trim() else null,
                                    horaActividad = if (tipoSeleccionado == "Actividad") horaActividad.trim() else null,
                                    lugarActividad = if (tipoSeleccionado == "Actividad") lugarActividad.trim() else null,
                                    fechaCierre = fechaCierreFinal,
                                    opciones = if (tipoSeleccionado == "Encuesta") opcionesEncuesta.map { it.trim() } else null
                                )
                                val resp = ApiClient.apiService.comunidadAdmin(authHeader, req)

                                if (resp.isSuccessful && resp.body()?.success == true) {
                                    limpiarArchivosTemporales()
                                    onCreadoExitosamente()
                                } else {
                                    formError = extraerMensajeErrorApi(resp, "Error al crear la publicación")
                                }
                                return@launch
                            }

                            // CASO B: Con fotografía seleccionada (Comunicado o Actividad)
                            // Flujo en 6 pasos según orden estricto de preservación y notificaciones FCM

                            // Paso 1: Validar campos y procesar la fotografía localmente
                            val (partImagen, fileTemp) = try {
                                prepararImagenComunidad(context, imagenSeleccionada!!)
                            } catch (e: Exception) {
                                formError = e.message ?: "Error al procesar la fotografía seleccionada."
                                isSubmitting = false
                                return@launch
                            }
                            tempArchivoSubida = fileTemp

                            // Paso 2 y 3: Crear la publicación en estado temporal 'Borrador' para que FCM no notifique prematuramente
                            if (idPublicacionCreada == null) {
                                val reqBorrador = ComunidadAdminRequest(
                                    accion = "crear",
                                    tipo = tipoSeleccionado,
                                    titulo = titulo.trim(),
                                    descripcion = descripcion.trim(),
                                    estado = "Borrador", // Siempre Borrador temporal
                                    fechaActividad = if (tipoSeleccionado == "Actividad") fechaActividadIso.trim() else null,
                                    horaActividad = if (tipoSeleccionado == "Actividad") horaActividad.trim() else null,
                                    lugarActividad = if (tipoSeleccionado == "Actividad") lugarActividad.trim() else null
                                )
                                val respCrear = ApiClient.apiService.comunidadAdmin(authHeader, reqBorrador)
                                if (!respCrear.isSuccessful || respCrear.body()?.success != true) {
                                    formError = extraerMensajeErrorApi(respCrear, "Error al crear la publicación")
                                    return@launch
                                }
                                val nuevoId = respCrear.body()?.idPublicacionInt
                                if (nuevoId == null || nuevoId <= 0) {
                                    formError = "El servidor no devolvió un identificador de publicación válido."
                                    return@launch
                                }
                                idPublicacionCreada = nuevoId
                            }

                            val idPubReal = idPublicacionCreada!!

                            // Paso 4: Subir fotografía a comunidad_imagen.php con multipart
                            if (!fotoSubidaExitosamente) {
                                val idPart = idPubReal.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val respImg = ApiClient.apiService.subirImagenComunidad(authHeader, idPart, partImagen)
                                if (!respImg.isSuccessful || respImg.body()?.success != true) {
                                    val errMsg = extraerMensajeErrorApi(respImg, "Error al subir la fotografía")
                                    formError = "La publicación se guardó como borrador (ID #$idPubReal), pero no se pudo subir la fotografía: $errMsg. Puedes reintentar."
                                    return@launch
                                }
                                fotoSubidaExitosamente = true
                            }

                            // Paso 5: Si el administrador eligió "Publicado", cambiar el estado ahora que la foto ya está asignada
                            if (estadoSeleccionado == "Publicado") {
                                val reqEstado = ComunidadAdminRequest(
                                    accion = "estado",
                                    idPublicacion = idPubReal,
                                    estado = "Publicado"
                                )
                                val respEstado = ApiClient.apiService.comunidadAdmin(authHeader, reqEstado)
                                if (!respEstado.isSuccessful || respEstado.body()?.success != true) {
                                    val errMsg = extraerMensajeErrorApi(respEstado, "Error al cambiar a Publicado")
                                    formError = "La fotografía se guardó en el borrador (ID #$idPubReal), pero falló la publicación: $errMsg. Puedes reintentar publicar."
                                    return@launch
                                }
                            }

                            // Paso 6: Completado exitosamente
                            limpiarArchivosTemporales()
                            tempArchivoSubida.delete()
                            onCreadoExitosamente()

                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (ex: Exception) {
                            Log.e("COMUNIDAD_ADMIN", "Fallo al procesar publicación con imagen", ex)
                            formError = "Error de conexión con el servidor. Verifica tu conexión a Internet."
                        } finally {
                            isSubmitting = false
                            try {
                                tempArchivoSubida?.delete()
                            } catch (_: Exception) {}
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.testTag("btn_confirmar_crear_pub")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val textoCarga = when {
                        idPublicacionCreada != null && !fotoSubidaExitosamente -> "Subiendo fotografía..."
                        idPublicacionCreada != null && fotoSubidaExitosamente -> "Publicando..."
                        else -> "Guardando..."
                    }
                    Text(textoCarga)
                } else {
                    val textoBoton = when {
                        idPublicacionCreada != null && !fotoSubidaExitosamente -> "Reintentar subida de foto"
                        idPublicacionCreada != null && fotoSubidaExitosamente -> "Reintentar publicar"
                        else -> "Guardar publicación"
                    }
                    Text(textoBoton)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    limpiarArchivosTemporales()
                    onDismiss()
                },
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        }
    )
}

// =====================================================
// DIÁLOGO: EDITAR PUBLICACIÓN (ADMINISTRADOR)
// =====================================================

@Composable
private fun EditarPublicacionDialog(
    publicacion: PublicacionComunidad,
    token: String,
    onDismiss: () -> Unit,
    onEditadoExitosamente: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var titulo by remember { mutableStateOf(publicacion.titulo) }
    var descripcion by remember { mutableStateOf(publicacion.descripcion) }

    // Campos para Actividad con precarga correcta
    var fechaActividadIso by remember { mutableStateOf(normalizarFechaIso(publicacion.fechaActividad)) }
    var fechaActividadVisible by remember { mutableStateOf(isoAFormatoLegible(normalizarFechaIso(publicacion.fechaActividad))) }
    var horaActividad by remember { mutableStateOf(normalizarHoraMinuto(publicacion.horaActividad)) }
    var lugarActividad by remember { mutableStateOf(publicacion.lugarActividad ?: "") }

    // Fecha cierre para Encuesta (solo si no tiene votos)
    val totalVotos = publicacion.totalVotos ?: 0
    val puedeEditarCierre = publicacion.tipo == "Encuesta" && totalVotos == 0
    var fechaCierreIso by remember { mutableStateOf(normalizarFechaIso(publicacion.fechaCierre)) }
    var fechaCierreVisible by remember { mutableStateOf(isoAFormatoLegible(normalizarFechaIso(publicacion.fechaCierre))) }
    var horaCierre by remember { mutableStateOf(extraerHoraDeFechaCompleta(publicacion.fechaCierre)) }

    var formError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar ${publicacion.tipo}", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { if (it.length <= 180) titulo = it },
                    label = { Text("Título *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 10000) descripcion = it },
                    label = { Text("Descripción *") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                if (publicacion.tipo == "Actividad") {
                    HorizontalDivider(color = DarkCardBorder)
                    Text("Detalles de la Actividad", fontWeight = FontWeight.Bold, color = ColorActividad, fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CampoSelectorVisual(
                            etiqueta = "Fecha *",
                            valorVisible = fechaActividadVisible,
                            placeholder = "Seleccionar fecha",
                            icono = Icons.Default.Event,
                            onClick = {
                                mostrarDatePicker(context, fechaActividadIso) { iso, legible ->
                                    fechaActividadIso = iso
                                    fechaActividadVisible = legible
                                    formError = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        CampoSelectorVisual(
                            etiqueta = "Hora *",
                            valorVisible = horaActividad,
                            placeholder = "Seleccionar hora",
                            icono = Icons.Default.AccessTime,
                            onClick = {
                                mostrarTimePicker(context, horaActividad) { hora ->
                                    horaActividad = hora
                                    formError = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = lugarActividad,
                        onValueChange = { if (it.length <= 255) lugarActividad = it },
                        label = { Text("Lugar *") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (publicacion.tipo == "Encuesta") {
                    if (puedeEditarCierre) {
                        HorizontalDivider(color = DarkCardBorder)
                        Text("Fecha y hora de cierre", fontWeight = FontWeight.Bold, color = ColorEncuesta, fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CampoSelectorVisual(
                                etiqueta = "Fecha cierre *",
                                valorVisible = fechaCierreVisible,
                                placeholder = "Seleccionar fecha",
                                icono = Icons.Default.Event,
                                onClick = {
                                    mostrarDatePicker(context, fechaCierreIso) { iso, legible ->
                                        fechaCierreIso = iso
                                        fechaCierreVisible = legible
                                        formError = null
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            CampoSelectorVisual(
                                etiqueta = "Hora cierre *",
                                valorVisible = horaCierre,
                                placeholder = "Seleccionar hora",
                                icono = Icons.Default.AccessTime,
                                onClick = {
                                    mostrarTimePicker(context, horaCierre) { hora ->
                                        horaCierre = hora
                                        formError = null
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Text(
                            text = "Esta encuesta ya tiene votos registrados, por lo que su fecha de cierre y opciones no pueden modificarse.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                formError?.let { err ->
                    Text(text = err, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idPubInt = publicacion.idPublicacionInt
                    if (idPubInt == null) {
                        formError = "Identificador de publicación no válido."
                        return@Button
                    }
                    if (titulo.trim().isBlank()) {
                        formError = "El título es obligatorio."
                        return@Button
                    }
                    if (descripcion.trim().isBlank()) {
                        formError = "La descripción es obligatoria."
                        return@Button
                    }

                    if (publicacion.tipo == "Actividad") {
                        if (fechaActividadIso.isBlank()) {
                            formError = "Selecciona la fecha de la actividad."
                            return@Button
                        }
                        if (horaActividad.isBlank()) {
                            formError = "Selecciona la hora de la actividad."
                            return@Button
                        }
                        if (lugarActividad.trim().isBlank()) {
                            formError = "El lugar de la actividad es obligatorio."
                            return@Button
                        }
                    }

                    var fechaCierreFinal: String? = null
                    if (puedeEditarCierre && (fechaCierreIso.isNotBlank() || horaCierre.isNotBlank())) {
                        val (valido, errCierre) = validarFechaCierreFutura(fechaCierreIso, horaCierre)
                        if (!valido) {
                            formError = errCierre ?: "La fecha de cierre debe ser futura."
                            return@Button
                        }
                        fechaCierreFinal = "${fechaCierreIso.trim()} ${horaCierre.trim()}:00"
                    }

                    formError = null
                    isSubmitting = true

                    coroutineScope.launch {
                        try {
                            val req = ComunidadAdminRequest(
                                accion = "editar",
                                idPublicacion = idPubInt,
                                titulo = titulo.trim(),
                                descripcion = descripcion.trim(),
                                fechaActividad = if (publicacion.tipo == "Actividad") fechaActividadIso.trim() else null,
                                horaActividad = if (publicacion.tipo == "Actividad") horaActividad.trim() else null,
                                lugarActividad = if (publicacion.tipo == "Actividad") lugarActividad.trim() else null,
                                fechaCierre = fechaCierreFinal
                            )
                            val authHeader = "Bearer $token"
                            val resp = ApiClient.apiService.comunidadAdmin(authHeader, req)

                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onEditadoExitosamente()
                            } else {
                                formError = extraerMensajeErrorApi(resp, "Error al actualizar la publicación")
                            }
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (ex: Exception) {
                            Log.e("COMUNIDAD_ADMIN", "Fallo al editar publicación", ex)
                            formError = "No se pudo conectar con el servidor."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizando...")
                } else {
                    Text("Guardar cambios")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        }
    )
}

// =====================================================
// DIÁLOGO: CAMBIAR ESTADO (ADMINISTRADOR)
// =====================================================

@Composable
private fun CambiarEstadoPublicacionDialog(
    publicacion: PublicacionComunidad,
    token: String,
    onDismiss: () -> Unit,
    onEstadoCambiado: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var estadoSeleccionado by remember { mutableStateOf(publicacion.estado ?: "Publicado") }
    var formError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar estado", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Selecciona el nuevo estado para \"${publicacion.titulo}\":",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )

                listOf("Borrador", "Publicado", "Archivado").forEach { est ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { estadoSeleccionado = est }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = estadoSeleccionado == est,
                            onClick = { estadoSeleccionado = est },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = est,
                            color = Color.White,
                            fontWeight = if (estadoSeleccionado == est) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                formError?.let { err ->
                    Text(text = err, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idPubInt = publicacion.idPublicacionInt
                    if (idPubInt == null) {
                        formError = "Identificador de publicación no válido."
                        return@Button
                    }

                    formError = null
                    isSubmitting = true

                    coroutineScope.launch {
                        try {
                            val req = ComunidadAdminRequest(
                                accion = "estado",
                                idPublicacion = idPubInt,
                                estado = estadoSeleccionado
                            )
                            val authHeader = "Bearer $token"
                            val resp = ApiClient.apiService.comunidadAdmin(authHeader, req)

                            if (resp.isSuccessful && resp.body()?.success == true) {
                                onEstadoCambiado()
                            } else {
                                formError = extraerMensajeErrorApi(resp, "Error al cambiar estado")
                            }
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (ex: Exception) {
                            Log.e("COMUNIDAD_ADMIN", "Fallo al cambiar estado", ex)
                            formError = "No se pudo conectar con el servidor."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiando...")
                } else {
                    Text("Actualizar estado")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        }
    )
}

// =====================================================
// COMPONENTE: CAMPO SELECTOR VISUAL
// =====================================================

@Composable
private fun CampoSelectorVisual(
    etiqueta: String,
    valorVisible: String,
    placeholder: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = valorVisible,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta, maxLines = 1) },
            placeholder = { Text(placeholder, maxLines = 1, color = Color(0xFF64748B)) },
            trailingIcon = {
                Icon(
                    imageVector = icono,
                    contentDescription = etiqueta,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Color(0xFF334155),
                focusedLabelColor = AccentCyan,
                unfocusedLabelColor = Color(0xFF94A3B8),
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A)
            )
        )
        // Capa interactiva sobre todo el campo para asegurar apertura inmediata al pulsar
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

// =====================================================
// SELECTORES NATIVOS: DATEPICKER Y TIMEPICKER
// =====================================================

private fun mostrarDatePicker(
    context: Context,
    fechaIsoActual: String,
    onFechaSeleccionada: (iso: String, legible: String) -> Unit
) {
    val tz = TimeZone.getTimeZone("America/La_Paz")
    val cal = Calendar.getInstance(tz)

    if (fechaIsoActual.isNotBlank()) {
        try {
            val partes = fechaIsoActual.trim().split("-")
            if (partes.size == 3) {
                val y = partes[0].toInt()
                val m = partes[1].toInt() - 1
                val d = partes[2].toInt()
                cal.set(y, m, d)
            }
        } catch (_: Exception) {}
    }

    val dpd = DatePickerDialog(
        context,
        { _, year, monthOfYear, dayOfMonth ->
            val mes = monthOfYear + 1
            val iso = String.format(Locale.US, "%04d-%02d-%02d", year, mes, dayOfMonth)
            val legible = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, mes, year)
            onFechaSeleccionada(iso, legible)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )
    dpd.show()
}

private fun mostrarTimePicker(
    context: Context,
    horaActual: String,
    onHoraSeleccionada: (hora: String) -> Unit
) {
    val tz = TimeZone.getTimeZone("America/La_Paz")
    val cal = Calendar.getInstance(tz)

    var hora = cal.get(Calendar.HOUR_OF_DAY)
    var minuto = cal.get(Calendar.MINUTE)

    if (horaActual.isNotBlank()) {
        try {
            val partes = horaActual.trim().split(":")
            if (partes.size >= 2) {
                hora = partes[0].trim().toInt()
                minuto = partes[1].trim().toInt()
            }
        } catch (_: Exception) {}
    }

    val tpd = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val horaFormateada = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
            onHoraSeleccionada(horaFormateada)
        },
        hora,
        minuto,
        true // Formato de 24 horas estricto
    )
    tpd.show()
}

// =====================================================
// EXTRACCIÓN DE ERRORES HTTP (INCLUYENDO HTTP 422)
// =====================================================

private fun extraerMensajeErrorApi(
    response: retrofit2.Response<*>,
    mensajePorDefecto: String
): String {
    // 1. Si viene en el body (ej: HTTP 200 con success: false)
    try {
        val body = response.body()
        if (body is com.example.comunimap.models.ApiResponse && !body.message.isNullOrBlank()) {
            return body.message
        }
        if (body is com.example.comunimap.models.ComunidadAdminResponse && !body.message.isNullOrBlank()) {
            return body.message
        }
        if (body is com.example.comunimap.models.ComunidadSubirImagenResponse && !body.message.isNullOrBlank()) {
            return body.message
        }
        if (body is com.example.comunimap.models.ComunidadResponse && !body.message.isNullOrBlank()) {
            return body.message
        }
    } catch (_: Exception) {}

    // 2. Si viene en errorBody (ej: HTTP 422, 400, 401, 403, 500)
    try {
        val errorStr = response.errorBody()?.string()
        if (!errorStr.isNullOrBlank()) {
            val json = JSONObject(errorStr)
            if (json.has("message")) {
                val msg = json.optString("message", "").trim()
                if (msg.isNotEmpty()) return msg
            }
            if (json.has("error")) {
                val err = json.optString("error", "").trim()
                if (err.isNotEmpty()) return err
            }
        }
    } catch (_: Exception) {}

    // 3. Códigos HTTP estándar de la API de Comunidad
    return when (response.code()) {
        401 -> "Sesión inválida o caducada (HTTP 401). Inicia sesión nuevamente."
        403 -> "Operación no autorizada (HTTP 403). Solo administradores pueden realizar esta acción."
        404 -> "La publicación no existe o fue eliminada (HTTP 404)."
        409 -> "Conflicto (HTTP 409): la publicación ya tiene una imagen o está archivada."
        413 -> "La fotografía supera el límite permitido de 4 MB (HTTP 413)."
        422 -> "$mensajePorDefecto: la imagen o los datos no cumplen los requisitos (HTTP 422)."
        500, 502 -> "Error temporal de servidor o almacenamiento (HTTP ${response.code()}). Inténtalo más tarde."
        else -> "$mensajePorDefecto (HTTP ${response.code()})"
    }
}

/**
 * Valida, corrige la orientación y optimiza la imagen seleccionada asegurando que cumpla
 * con los requisitos de comunidad_imagen.php (JPG, PNG, WEBP, máx. 4 MB).
 */
private fun prepararImagenComunidad(
    context: Context,
    uri: Uri
): Pair<MultipartBody.Part, File> {
    val contentResolver = context.contentResolver

    // 1. Validar y detectar tipo MIME
    var mimeType = contentResolver.getType(uri) ?: "image/jpeg"
    if (mimeType == "application/octet-stream") {
        mimeType = "image/jpeg"
    }

    val esJpg = mimeType.contains("jpeg", ignoreCase = true) || mimeType.contains("jpg", ignoreCase = true)
    val esPng = mimeType.contains("png", ignoreCase = true)
    val esWebp = mimeType.contains("webp", ignoreCase = true)

    if (!esJpg && !esPng && !esWebp) {
        throw IllegalArgumentException("Formato de imagen no permitido ($mimeType). Solo se aceptan fotografías JPG, PNG o WEBP.")
    }

    val extension = when {
        esPng -> ".png"
        esWebp -> ".webp"
        else -> ".jpg"
    }

    // 2. Detectar orientación EXIF
    val orientation = try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            ExifInterface(inputStream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (_: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }

    val rotationDegrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    val limiteBytes = 4 * 1024 * 1024L // 4 MB

    val tempFile = File(context.cacheDir, "comunidad_subida_${System.currentTimeMillis()}$extension")

    // Si la imagen no requiere rotación, intentamos copiarla directamente
    if (rotationDegrees == 0f) {
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            tempFile.delete()
            throw IllegalStateException("No se pudo leer la imagen seleccionada: ${e.message}")
        }

        // Si ya cumple el tamaño máximo y no está vacía, no recomprimir innecesariamente
        if (tempFile.length() in 1..limiteBytes) {
            val mediaType = mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
            val requestBody = tempFile.asRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("imagen", tempFile.name, requestBody)
            return Pair(part, tempFile)
        }
    }

    // Si requiere rotación o excede los 4 MB, decodificar, rotar y comprimir optimizadamente
    try {
        val optionsBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, optionsBounds)
        }

        val origWidth = optionsBounds.outWidth
        val origHeight = optionsBounds.outHeight
        if (origWidth <= 0 || origHeight <= 0) {
            tempFile.delete()
            throw IllegalArgumentException("La imagen seleccionada está vacía o dañada.")
        }

        // Calcular factor de escala para no agotar la memoria
        var sampleSize = 1
        val maxDim = 2048
        var tempW = origWidth
        var tempH = origHeight
        while (tempW > maxDim || tempH > maxDim) {
            sampleSize *= 2
            tempW /= 2
            tempH /= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val decodedBitmap = contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: run {
            tempFile.delete()
            throw IllegalStateException("No se pudo decodificar la imagen seleccionada.")
        }

        val bitmapFinal = if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true)
            if (rotated != decodedBitmap) {
                decodedBitmap.recycle()
            }
            rotated
        } else {
            decodedBitmap
        }

        val compressFormat = if (esPng && tempFile.length() <= limiteBytes) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }

        var quality = 85
        var outputBytes: ByteArray
        do {
            val bos = ByteArrayOutputStream()
            bitmapFinal.compress(compressFormat, quality, bos)
            outputBytes = bos.toByteArray()
            quality -= 15
        } while (outputBytes.size > limiteBytes && quality >= 35)

        bitmapFinal.recycle()

        if (outputBytes.size > limiteBytes) {
            tempFile.delete()
            throw IllegalArgumentException("La fotografía supera los 4 MB y no se pudo reducir al tamaño permitido.")
        }

        FileOutputStream(tempFile).use { it.write(outputBytes) }

        val finalMime = if (compressFormat == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        val mediaType = finalMime.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
        val requestBody = tempFile.asRequestBody(mediaType)
        val part = MultipartBody.Part.createFormData("imagen", tempFile.name, requestBody)
        return Pair(part, tempFile)
    } catch (e: Exception) {
        tempFile.delete()
        if (e is IllegalArgumentException || e is IllegalStateException) throw e
        throw IllegalStateException("Error al preparar la fotografía: ${e.message}")
    }
}

// =====================================================
// UTILIDADES: NORMALIZACIÓN Y PARSEO DE FECHAS
// =====================================================

/**
 * Normaliza cualquier formato de fecha a ISO "AAAA-MM-DD"
 */
private fun normalizarFechaIso(fechaRaw: String?): String {
    if (fechaRaw.isNullOrBlank()) return ""
    val limpio = fechaRaw.trim()
    // Si viene "2026-11-14 16:00:00" o "2026-11-14T16:00:00"
    if (limpio.length >= 10 && limpio[4] == '-' && limpio[7] == '-') {
        return limpio.substring(0, 10)
    }
    // Si viniera en formato "14/11/2026"
    if (limpio.length == 10 && limpio[2] == '/' && limpio[5] == '/') {
        val partes = limpio.split("/")
        return "${partes[2]}-${partes[1]}-${partes[0]}"
    }
    return limpio
}

/**
 * Convierte "AAAA-MM-DD" a "dd/MM/yyyy" para presentación visual amigable
 */
private fun isoAFormatoLegible(fechaIso: String): String {
    if (fechaIso.isBlank()) return ""
    return try {
        val partes = fechaIso.trim().split("-")
        if (partes.size == 3) {
            String.format(Locale.US, "%02d/%02d/%04d", partes[2].toInt(), partes[1].toInt(), partes[0].toInt())
        } else {
            fechaIso
        }
    } catch (_: Exception) {
        fechaIso
    }
}

/**
 * Normaliza una hora a "HH:mm" (24 horas)
 */
private fun normalizarHoraMinuto(horaRaw: String?): String {
    if (horaRaw.isNullOrBlank()) return ""
    val limpio = horaRaw.trim()
    return try {
        val partes = limpio.split(":")
        if (partes.size >= 2) {
            val h = partes[0].trim().toInt()
            val m = partes[1].trim().toInt()
            String.format(Locale.US, "%02d:%02d", h, m)
        } else {
            limpio
        }
    } catch (_: Exception) {
        limpio
    }
}

/**
 * Extrae la hora "HH:mm" de una cadena de fecha completa "2026-11-15 23:59:00"
 */
private fun extraerHoraDeFechaCompleta(fechaCompletaRaw: String?): String {
    if (fechaCompletaRaw.isNullOrBlank()) return ""
    val limpio = fechaCompletaRaw.trim()
    val partes = limpio.split(" ", "T")
    if (partes.size >= 2) {
        return normalizarHoraMinuto(partes[1])
    }
    return ""
}

/**
 * Valida que una fecha y hora combinadas correspondan a un momento estrictamente futuro en America/La_Paz
 */
private fun validarFechaCierreFutura(fechaIso: String, horaMinuto: String): Pair<Boolean, String?> {
    if (fechaIso.isBlank()) {
        return Pair(false, "Selecciona la fecha de cierre de la encuesta.")
    }
    if (horaMinuto.isBlank()) {
        return Pair(false, "Selecciona la hora de cierre de la encuesta.")
    }
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("America/La_Paz")
            isLenient = false
        }
        val fechaHoraStr = "${fechaIso.trim()} ${horaMinuto.trim()}:00"
        val fechaParsed = sdf.parse(fechaHoraStr)
            ?: return Pair(false, "Formato de fecha de cierre no válido.")

        val ahora = Calendar.getInstance(TimeZone.getTimeZone("America/La_Paz")).time
        if (!fechaParsed.after(ahora)) {
            return Pair(false, "La fecha de cierre debe ser futura.")
        }
        Pair(true, null)
    } catch (_: Exception) {
        Pair(false, "Fecha u hora de cierre inválida.")
    }
}

/**
 * Formatea una fecha para presentación amigable en las tarjetas del muro
 */
private fun formatearFechaLegible(fechaStr: String): String {
    return try {
        val formatosEntrada = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )
        var date: java.util.Date? = null
        for (formato in formatosEntrada) {
            try {
                val sdf = SimpleDateFormat(formato, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("America/La_Paz")
                }
                date = sdf.parse(fechaStr)
                if (date != null) break
            } catch (_: Exception) {}
        }

        if (date != null) {
            val salida = SimpleDateFormat("dd/MM/yyyy", Locale("es", "BO")).apply {
                timeZone = TimeZone.getTimeZone("America/La_Paz")
            }
            salida.format(date)
        } else {
            fechaStr
        }
    } catch (_: Exception) {
        fechaStr
    }
}
