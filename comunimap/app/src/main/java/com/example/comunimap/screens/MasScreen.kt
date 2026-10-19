package com.example.comunimap.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.Usuario
import com.example.comunimap.utils.BASE_URL
import com.example.comunimap.utils.ModoTema
import com.example.comunimap.utils.PreferenciasManager
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA: MÁS (AJUSTES Y CUENTA)
// =====================================================

// Paleta oscura Navy (estilo predeterminado de ComuniMap)
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkCardBg = Color(0xFF162238)
private val DarkCardBorder = Color(0xFF1E293B)
private val DarkTextMuted = Color(0xFF94A3B8)
private val DarkDivider = Color(0xFF1E293B)

// Paleta clara (modo claro real y limpio)
private val LightNavyBg = Color(0xFFF1F5F9)
private val LightCardBg = Color(0xFFFFFFFF)
private val LightCardBorder = Color(0xFFE2E8F0)
private val LightTextMuted = Color(0xFF64748B)
private val LightDivider = Color(0xFFE2E8F0)

private val AccentCyan = Color(0xFF38BDF8)
private val AccentBlue = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasScreen(
    usuarioActual: Usuario? = null,
    esModoOscuro: Boolean = true,
    modoTema: ModoTema = ModoTema.SISTEMA,
    onCambiarModoTema: (ModoTema) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Colores dinámicos según el modo claro / oscuro activo
    val containerBg = if (esModoOscuro) DarkNavyBg else LightNavyBg
    val cardBg = if (esModoOscuro) DarkCardBg else LightCardBg
    val cardBorder = if (esModoOscuro) DarkCardBorder else LightCardBorder
    val textoPrincipal = if (esModoOscuro) Color.White else Color(0xFF0F172A)
    val textoMuted = if (esModoOscuro) DarkTextMuted else LightTextMuted
    val dividerColor = if (esModoOscuro) DarkDivider else LightDivider
    val dialogBg = if (esModoOscuro) Color(0xFF1E293B) else Color.White
    val innerBoxBg = if (esModoOscuro) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    // Estados de switches persistidos en SharedPreferences
    var notificacionesHabilitadas by remember {
        mutableStateOf(PreferenciasManager.obtenerNotificacionesAlertas(context))
    }
    var sonidoEmergenciaHabilitado by remember {
        mutableStateOf(PreferenciasManager.obtenerSonidoEmergencia(context))
    }
    var vibracionHabilitada by remember {
        mutableStateOf(PreferenciasManager.obtenerVibracionAlertas(context))
    }

    // Estados para diálogos funcionales de cada sección
    var showTemaDialog by remember { mutableStateOf(false) }
    var showPerfilDialog by remember { mutableStateOf(false) }
    var showSeguridadDialog by remember { mutableStateOf(false) }
    var showAyudaDialog by remember { mutableStateOf(false) }
    var showTerminosDialog by remember { mutableStateOf(false) }
    var showPrivacidadDialog by remember { mutableStateOf(false) }
    var showAcercaDeDialog by remember { mutableStateOf(false) }
    var showCerrarSesionConfirmDialog by remember { mutableStateOf(false) }

    // Estados para prueba de conexión real con Railway
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionResult by remember { mutableStateOf<String?>(null) }
    var connectionSuccess by remember { mutableStateOf<Boolean?>(null) }

    // Datos del usuario que inició sesión
    val usuarioNombre = usuarioActual?.nombre ?: "Usuario"
    val usuarioEmail = usuarioActual?.email ?: "Sin correo"
    val usuarioRol = usuarioActual?.rol ?: "Usuario"
    val usuarioId = usuarioActual?.idUsuario ?: "N/A"

    // Función para probar la conexión real con Railway
    fun probarConexion() {
        coroutineScope.launch {
            isTestingConnection = true
            connectionResult = null
            connectionSuccess = null
            try {
                val inicio = System.currentTimeMillis()
                val resp = ApiClient.apiService.obtenerUsuarios()
                val duracionMs = System.currentTimeMillis() - inicio
                Log.d("USUARIOS_HTTP", "Prueba de conexión HTTP: ${resp.code()} en ${duracionMs}ms")

                if (resp.isSuccessful) {
                    connectionSuccess = true
                    connectionResult = "¡Conexión exitosa con Railway! (HTTP ${resp.code()} - ${duracionMs} ms)"
                } else {
                    connectionSuccess = false
                    connectionResult = "Error del servidor: HTTP ${resp.code()}"
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (ex: Exception) {
                Log.e("USUARIOS_ERROR", "Fallo al probar conexión", ex)
                connectionSuccess = false
                connectionResult = "No se pudo conectar con el servidor."
            } finally {
                isTestingConnection = false
            }
        }
    }

    // Subtítulo legible para el ajuste de Tema
    val subtituloTema = when (modoTema) {
        ModoTema.CLARO -> "Claro"
        ModoTema.OSCURO -> "Oscuro"
        ModoTema.SISTEMA -> "Seguir configuración del sistema"
    }

    Scaffold(
        containerColor = containerBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Logo circular de ComuniMap
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (esModoOscuro) Color(0xFF0F172A) else Color(0xFFDBEAFE))
                                .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Logo ComuniMap",
                                tint = if (esModoOscuro) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Ajustes y Cuenta",
                            color = textoPrincipal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerBg,
                    titleContentColor = textoPrincipal
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
        ) {
            // =====================================================
            // 1. TARJETA DE PERFIL
            // =====================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPerfilDialog = true }
                        .testTag("tarjeta_perfil"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (esModoOscuro) 3.dp else 1.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar circular con iniciales
                        val iniciales = usuarioNombre.take(2).uppercase()
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (esModoOscuro) Color(0xFF0D5E54) else Color(0xFFCCFBF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iniciales,
                                color = if (esModoOscuro) Color(0xFF2DD4BF) else Color(0xFF0F766E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Información: Nombre, correo y badge de rol
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = usuarioNombre,
                                color = textoPrincipal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = usuarioEmail,
                                color = textoMuted,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Badge de Rol
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (esModoOscuro) Color(0xFF064E3B) else Color(0xFFDCFCE7))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = usuarioRol,
                                    color = if (esModoOscuro) Color(0xFF34D399) else Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Flecha derecha para indicar que es interactivo
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver Perfil",
                            tint = textoMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // =====================================================
            // 2. SECCIÓN: AJUSTES PRINCIPALES
            // =====================================================
            item {
                Text(
                    text = "Ajustes Principales",
                    color = if (esModoOscuro) AccentCyan else Color(0xFF0284C7),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (esModoOscuro) 3.dp else 1.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Tema de la Aplicación
                        AjusteItemRow(
                            icono = when (modoTema) {
                                ModoTema.CLARO -> Icons.Default.LightMode
                                ModoTema.OSCURO -> Icons.Default.DarkMode
                                ModoTema.SISTEMA -> Icons.Default.BrightnessAuto
                            },
                            colorIcono = Color(0xFFA78BFA),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF2E2356) else Color(0xFFEDE9FE),
                            titulo = "Tema de la Aplicación",
                            subtitulo = subtituloTema,
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            testTag = "ajuste_tema_aplicacion",
                            onClick = { showTemaDialog = true }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 2. Mi Perfil
                        AjusteItemRow(
                            icono = Icons.Default.Person,
                            colorIcono = Color(0xFF60A5FA),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF1E3A8A) else Color(0xFFDBEAFE),
                            titulo = "Mi Perfil",
                            subtitulo = "Información del usuario y nivel de acceso",
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            testTag = "ajuste_mi_perfil",
                            onClick = { showPerfilDialog = true }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 3. Seguridad
                        AjusteItemRow(
                            icono = Icons.Default.Security,
                            colorIcono = Color(0xFF34D399),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF0F4336) else Color(0xFFDCFCE7),
                            titulo = "Seguridad",
                            subtitulo = "Estado de sesión, cifrado y permisos",
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            testTag = "ajuste_seguridad",
                            onClick = { showSeguridadDialog = true }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 4. Ayuda y Preguntas Frecuentes
                        AjusteItemRow(
                            icono = Icons.Default.HelpOutline,
                            colorIcono = Color(0xFFF472B6),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF4C1D38) else Color(0xFFFCE7F3),
                            titulo = "Ayuda y Preguntas Frecuentes",
                            subtitulo = "Guía de reportes, mapas y contacto",
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            testTag = "ajuste_ayuda",
                            onClick = { showAyudaDialog = true }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 5. Términos y Condiciones
                        AjusteItemRow(
                            icono = Icons.Default.Info,
                            colorIcono = Color(0xFF38BDF8),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
                            titulo = "Términos y Condiciones",
                            subtitulo = "Condiciones de uso y convivencia vecinal",
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            testTag = "ajuste_terminos",
                            onClick = { showTerminosDialog = true }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 6. Política de Privacidad
                        AjusteItemRow(
                            icono = Icons.Default.Security,
                            colorIcono = Color(0xFF10B981),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF064E3B) else Color(0xFFD1FAE5),
                            titulo = "Política de Privacidad",
                            subtitulo = "Tratamiento de datos, ubicación y alertas",
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            testTag = "ajuste_privacidad",
                            onClick = { showPrivacidadDialog = true }
                        )
                    }
                }
            }

            // =====================================================
            // 3. SECCIÓN: NOTIFICACIONES
            // =====================================================
            item {
                Text(
                    text = "Notificaciones",
                    color = if (esModoOscuro) AccentCyan else Color(0xFF0284C7),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (esModoOscuro) 3.dp else 1.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Notificaciones de Alertas
                        AjusteSwitchRow(
                            icono = Icons.Default.Notifications,
                            colorIcono = Color(0xFFFBBF24),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF452B14) else Color(0xFFFEF3C7),
                            titulo = "Notificaciones de Alertas",
                            subtitulo = "Avisos de incidentes vecinales en tu barrio",
                            checked = notificacionesHabilitadas,
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            onCheckedChange = { nuevoEstado ->
                                notificacionesHabilitadas = nuevoEstado
                                PreferenciasManager.guardarNotificacionesAlertas(context, nuevoEstado)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (nuevoEstado) "Notificaciones de alertas activadas" else "Notificaciones de alertas desactivadas"
                                    )
                                }
                            }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Sonido de Emergencia
                        AjusteSwitchRow(
                            icono = Icons.Default.VolumeUp,
                            colorIcono = Color(0xFF38BDF8),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
                            titulo = "Sonido de Emergencia",
                            subtitulo = "Efectos sonoros en reportes urgentes",
                            checked = sonidoEmergenciaHabilitado,
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            onCheckedChange = { nuevoEstado ->
                                sonidoEmergenciaHabilitado = nuevoEstado
                                PreferenciasManager.guardarSonidoEmergencia(context, nuevoEstado)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (nuevoEstado) "Sonido de alertas activado" else "Sonido de alertas silenciado"
                                    )
                                }
                            }
                        )

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Vibración de Alertas
                        AjusteSwitchRow(
                            icono = Icons.Default.Vibration,
                            colorIcono = Color(0xFFA78BFA),
                            colorFondoIcono = if (esModoOscuro) Color(0xFF2E2356) else Color(0xFFEDE9FE),
                            titulo = "Vibración de Alertas",
                            subtitulo = "Vibrar el teléfono en incidentes prioritarios",
                            checked = vibracionHabilitada,
                            textoPrincipal = textoPrincipal,
                            textoMuted = textoMuted,
                            onCheckedChange = { nuevoEstado ->
                                vibracionHabilitada = nuevoEstado
                                PreferenciasManager.guardarVibracionAlertas(context, nuevoEstado)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (nuevoEstado) "Vibración activada" else "Vibración desactivada"
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // =====================================================
            // 4. SECCIÓN: ACERCA DE COMUNIMAP
            // =====================================================
            item {
                Text(
                    text = "Información del Proyecto",
                    color = if (esModoOscuro) AccentCyan else Color(0xFF0284C7),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { showAcercaDeDialog = true }
                        .testTag("card_acerca_de"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (esModoOscuro) 3.dp else 1.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (esModoOscuro) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = if (esModoOscuro) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acerca de ComuniMap",
                                color = textoPrincipal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sistema Digital • Cochabamba, Bolivia",
                                color = textoMuted,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textoMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // =====================================================
            // 5. BOTÓN INFERIOR: CERRAR SESIÓN (CON CONFIRMACIÓN)
            // =====================================================
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { showCerrarSesionConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626) // Rojo accesible
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_cerrar_sesion")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar Sesión",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cerrar Sesión",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // =====================================================
    // DIÁLOGO 1: TEMA DE LA APLICACIÓN
    // =====================================================
    if (showTemaDialog) {
        AlertDialog(
            onDismissRequest = { showTemaDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BrightnessAuto,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Tema de la Aplicación", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selecciona el aspecto visual que prefieras para ComuniMap:",
                        fontSize = 13.sp,
                        color = textoMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    ModoTema.values().forEach { modo ->
                        val isSelected = modoTema == modo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        if (esModoOscuro) Color(0xFF2E2356) else Color(0xFFEDE9FE)
                                    } else Color.Transparent
                                )
                                .clickable {
                                    onCambiarModoTema(modo)
                                    showTemaDialog = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Tema cambiado a: ${modo.titulo}")
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onCambiarModoTema(modo)
                                    showTemaDialog = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Tema cambiado a: ${modo.titulo}")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = modo.titulo,
                                    color = if (isSelected) (if (esModoOscuro) Color.White else Color(0xFF5B21B6)) else textoPrincipal,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = modo.descripcion,
                                    color = textoMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemaDialog = false }) {
                    Text("Cerrar", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO 2: MI PERFIL (DETALLES Y CREDENCIALES)
    // =====================================================
    if (showPerfilDialog) {
        AlertDialog(
            onDismissRequest = { showPerfilDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (esModoOscuro) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (esModoOscuro) Color(0xFF60A5FA) else Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Mi Perfil", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Credenciales y datos de sesión", fontSize = 11.sp, color = textoMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PerfilDatoItem(
                        label = "Nombre de Usuario",
                        valor = usuarioNombre,
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    PerfilDatoItem(
                        label = "Correo Electrónico",
                        valor = usuarioEmail,
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    PerfilDatoItem(
                        label = "Rol Asignado",
                        valor = usuarioRol,
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted,
                        badgeColor = if (usuarioRol.equals("Administrador", ignoreCase = true)) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )

                    PerfilDatoItem(
                        label = "ID de Cuenta",
                        valor = "#$usuarioId",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    PerfilDatoItem(
                        label = "Estado de Cuenta",
                        valor = "Activa y Autenticada",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted,
                        iconoEstado = Icons.Default.CheckCircle,
                        iconoColor = Color(0xFF16A34A)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(innerBoxBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (usuarioRol.equals("Administrador", ignoreCase = true)) {
                                "Tienes privilegios de Administrador para gestionar reportes vecinales, categorías y cuentas de usuarios en Cochabamba."
                            } else {
                                "Tienes acceso de Vecino para crear reportes, ver el mapa interactivo y recibir avisos de emergencias en tu comunidad."
                            },
                            color = textoMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPerfilDialog = false }) {
                    Text("Cerrar", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO 3: SEGURIDAD
    // =====================================================
    if (showSeguridadDialog) {
        val notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        AlertDialog(
            onDismissRequest = { showSeguridadDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (esModoOscuro) Color(0xFF0F4336) else Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (esModoOscuro) Color(0xFF34D399) else Color(0xFF15803D),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Seguridad y Privacidad", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Protección de cuenta y datos", fontSize = 11.sp, color = textoMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SeguridadItem(
                        titulo = "Sesión Activa",
                        descripcion = "Autenticación válida y protegida en memoria.",
                        icono = Icons.Default.CheckCircle,
                        iconoColor = Color(0xFF16A34A),
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    SeguridadItem(
                        titulo = "Cifrado de Red (SSL/TLS)",
                        descripcion = "Toda comunicación con Railway se realiza sobre HTTPS seguro con certificados TLS 1.3.",
                        icono = Icons.Default.CheckCircle,
                        iconoColor = Color(0xFF16A34A),
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    SeguridadItem(
                        titulo = "Permiso POST_NOTIFICATIONS",
                        descripcion = if (notificationPermissionGranted) {
                            "Concedido en Android: recibes alertas de incidentes en tiempo real."
                        } else {
                            "Pendiente o denegado: puedes habilitarlo en los Ajustes del sistema de tu teléfono."
                        },
                        icono = if (notificationPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                        iconoColor = if (notificationPermissionGranted) Color(0xFF16A34A) else Color(0xFFF59E0B),
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    SeguridadItem(
                        titulo = "Privacidad de Ubicación",
                        descripcion = "Las coordenadas GPS se utilizan estrictamente para geolocalizar reportes vecinales y no se comparten con terceros.",
                        icono = Icons.Default.CheckCircle,
                        iconoColor = Color(0xFF16A34A),
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(innerBoxBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ComuniMap aplica el principio de mínimo privilegio para garantizar la integridad vecinal.",
                            color = textoMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSeguridadDialog = false }) {
                    Text("Entendido", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO 4: AYUDA Y PREGUNTAS FRECUENTES
    // =====================================================
    if (showAyudaDialog) {
        AlertDialog(
            onDismissRequest = { showAyudaDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (esModoOscuro) Color(0xFF4C1D38) else Color(0xFFFCE7F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = if (esModoOscuro) Color(0xFFF472B6) else Color(0xFFDB2777),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Centro de Ayuda", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Guía para vecinos de Cochabamba", fontSize = 11.sp, color = textoMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FaqItem(
                        pregunta = "¿Cómo crear un reporte?",
                        respuesta = "Dirígete a la pestaña 'Reportes' o al 'Mapa', presiona el botón '+' de nuevo reporte, selecciona la categoría adecuada (seguridad, luminarias, baches, etc.), describe el hecho y confirma la ubicación.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "¿Qué significan los estados de reporte?",
                        respuesta = "• Pendiente: recién ingresado por un vecino.\n• En Proceso: cuadrilla comunitaria o autoridades atendiéndolo.\n• Resuelto: solucionado satisfactoriamente.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "¿Cómo funcionan las alertas push?",
                        respuesta = "Al generarse un incidente grave o urgente en tu distrito, Firebase Cloud Messaging envía un aviso al teléfono con sonido y notificación en la barra de estado.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "¿A quién llamar en emergencias graves?",
                        respuesta = "Para riesgo vital o delitos en curso, comunícate de inmediato con Radio Patrulla 110, Bomberos 119 o FELCC Cochabamba.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAyudaDialog = false }) {
                    Text("Cerrar", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO 5: TÉRMINOS Y CONDICIONES
    // =====================================================
    if (showTerminosDialog) {
        AlertDialog(
            onDismissRequest = { showTerminosDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (esModoOscuro) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (esModoOscuro) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Términos y Condiciones", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Reglamento de uso vecinal", fontSize = 11.sp, color = textoMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FaqItem(
                        pregunta = "1. Propósito de la Plataforma",
                        respuesta = "ComuniMap es una herramienta vecinal participativa destinada al reporte, visualización y seguimiento transparente de incidencias en la comunidad de Cochabamba.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "2. Veracidad y Responsabilidad",
                        respuesta = "Cada usuario se compromete a emitir reportes fidedignos, con descripciones precisas y ubicación real. Está terminantemente prohibido publicar información falsa o maliciosa.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "3. Respeto y Convivencia",
                        respuesta = "Queda prohibido el uso de lenguaje soez, discriminatorio o injurioso. La plataforma fomenta la concordia y el trabajo articulado entre vecinos y cuadrillas de servicio.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "4. Moderación de Reportes",
                        respuesta = "Los administradores supervisan las publicaciones y pueden actualizar estados, consolidar reportes duplicados o eliminar aquellos que vulneren estas normas.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "5. Alcance del Servicio",
                        respuesta = "ComuniMap actúa como canal digital colaborativo de visibilización comunitaria; las obras físicas y reparaciones corresponden a las entidades municipales competentes.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTerminosDialog = false }) {
                    Text("Aceptar y Cerrar", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO 6: POLÍTICA DE PRIVACIDAD
    // =====================================================
    if (showPrivacidadDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacidadDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (esModoOscuro) Color(0xFF064E3B) else Color(0xFFD1FAE5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (esModoOscuro) Color(0xFF10B981) else Color(0xFF059669),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Política de Privacidad", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Protección de datos personales", fontSize = 11.sp, color = textoMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FaqItem(
                        pregunta = "1. Datos que Recopilamos",
                        respuesta = "ComuniMap almacena únicamente el nombre de usuario, correo electrónico para la autenticación y el rol asignado (Usuario o Administrador). Las contraseñas están resguardadas con hash criptográfico en la base de datos.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "2. Uso de Coordenadas GPS",
                        respuesta = "La ubicación geográfica solo se captura para georreferenciar incidentes sobre el mapa interactivo. No se realiza ningún rastreo en segundo plano ni fuera del proceso explícito de reporte.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "3. Tokens de Notificación FCM",
                        respuesta = "El token de Firebase Cloud Messaging se vincula a tu usuario para despachar avisos push esenciales sobre emergencias y cambios de estado en tus reportes.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "4. Transmisión Segura (HTTPS/TLS)",
                        respuesta = "Toda petición entre la aplicación y el servidor en Railway está cifrada de extremo a extremo mediante el protocolo TLS.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    FaqItem(
                        pregunta = "5. No Venta a Terceros",
                        respuesta = "ComuniMap no comercializa, transfiere ni expone información ciudadana a redes publicitarias ni entidades comerciales.",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacidadDialog = false }) {
                    Text("Comprendido", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO 7: ACERCA DE COMUNIMAP
    // =====================================================
    if (showAcercaDeDialog) {
        AlertDialog(
            onDismissRequest = { showAcercaDeDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (esModoOscuro) Color(0xFF0F172A) else Color(0xFFDBEAFE))
                            .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (esModoOscuro) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Acerca de ComuniMap",
                        fontWeight = FontWeight.Bold,
                        color = textoPrincipal,
                        fontSize = 18.sp
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
                    Text(
                        text = "ComuniMap",
                        color = if (esModoOscuro) AccentCyan else Color(0xFF0284C7),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Sistema Digital para la Gestión de Problemas Comunitarios mediante un Mapa Interactivo",
                        color = textoPrincipal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "Sistema digital orientado al registro, visualización, seguimiento y gestión de problemas comunitarios mediante un mapa interactivo.",
                        color = textoMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    PerfilDatoItem(
                        label = "Ubicación del Proyecto",
                        valor = "Cochabamba, Bolivia",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    PerfilDatoItem(
                        label = "Servidor / Backend",
                        valor = "Railway Producción (PHP & MySQL)",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    PerfilDatoItem(
                        label = "Versión de la Aplicación",
                        valor = "2.1.0",
                        textoPrincipal = textoPrincipal,
                        textoMuted = textoMuted
                    )

                    Text(
                        text = BASE_URL,
                        fontSize = 11.sp,
                        color = if (esModoOscuro) AccentCyan else Color(0xFF0284C7)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botón para probar conexión real con el servidor
                    Button(
                        onClick = { probarConexion() },
                        enabled = !isTestingConnection,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_probar_conexion_dialog")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Probando...", color = Color.White)
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probar conexión a Railway", color = Color.White)
                        }
                    }

                    // Resultado de la prueba
                    connectionResult?.let { msg ->
                        Spacer(modifier = Modifier.height(4.dp))
                        val isOk = connectionSuccess == true
                        val colorText = if (isOk) Color(0xFF16A34A) else Color(0xFFDC2626)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isOk) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = colorText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = msg,
                                color = colorText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAcercaDeDialog = false }) {
                    Text("Cerrar", color = if (esModoOscuro) AccentCyan else Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO 8: CONFIRMACIÓN DE CIERRE DE SESIÓN
    // =====================================================
    if (showCerrarSesionConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCerrarSesionConfirmDialog = false },
            containerColor = dialogBg,
            titleContentColor = textoPrincipal,
            textContentColor = textoMuted,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (esModoOscuro) Color(0xFF451A1A) else Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "¿Cerrar Sesión?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textoPrincipal
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas salir de tu cuenta en ComuniMap? Tendrás que iniciar sesión nuevamente con tu correo para acceder a tus reportes y mapa.",
                    color = textoMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCerrarSesionConfirmDialog = false
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirmar_cerrar_sesion")
                ) {
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCerrarSesionConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textoPrincipal)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// =====================================================
// COMPONENTES AUXILIARES DE UI
// =====================================================

// Fila para cada ajuste con navegación o acción
@Composable
private fun AjusteItemRow(
    icono: ImageVector,
    colorIcono: Color,
    colorFondoIcono: Color,
    titulo: String,
    subtitulo: String,
    textoPrincipal: Color,
    textoMuted: Color,
    testTag: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorFondoIcono),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = textoPrincipal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                color = textoMuted,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textoMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

// Fila para cada ajuste que contiene un Switch visual
@Composable
private fun AjusteSwitchRow(
    icono: ImageVector,
    colorIcono: Color,
    colorFondoIcono: Color,
    titulo: String,
    subtitulo: String,
    checked: Boolean,
    textoPrincipal: Color,
    textoMuted: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorFondoIcono),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = textoPrincipal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                color = textoMuted,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3B82F6),
                uncheckedThumbColor = textoMuted,
                uncheckedTrackColor = if (textoPrincipal == Color.White) Color(0xFF1E293B) else Color(0xFFE2E8F0)
            )
        )
    }
}

// Item para mostrar detalles del perfil del usuario
@Composable
private fun PerfilDatoItem(
    label: String,
    valor: String,
    textoPrincipal: Color,
    textoMuted: Color,
    badgeColor: Color? = null,
    iconoEstado: ImageVector? = null,
    iconoColor: Color = Color.Unspecified
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 11.sp, color = textoMuted, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconoEstado != null) {
                Icon(
                    imageVector = iconoEstado,
                    contentDescription = null,
                    tint = iconoColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (badgeColor != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = valor,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Text(
                    text = valor,
                    color = textoPrincipal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Item para sección de seguridad
@Composable
private fun SeguridadItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    iconoColor: Color,
    textoPrincipal: Color,
    textoMuted: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = iconoColor,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = textoPrincipal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = descripcion,
                color = textoMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// Item para preguntas frecuentes
@Composable
private fun FaqItem(
    pregunta: String,
    respuesta: String,
    textoPrincipal: Color,
    textoMuted: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = pregunta,
            color = textoPrincipal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = respuesta,
            color = textoMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}
