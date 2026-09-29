package com.example.comunimap.screens

import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.Usuario
import com.example.comunimap.utils.BASE_URL
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA: MÁS (AJUSTES Y CUENTA)
// =====================================================

// Paleta oscura navy basada exactamente en las capturas de referencia
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkCardBg = Color(0xFF162238)
private val DarkCardBorder = Color(0xFF1E293B)
private val TextMuted = Color(0xFF94A3B8)
private val AccentCyan = Color(0xFF38BDF8)
private val AccentBlue = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasScreen(
    usuarioActual: Usuario? = null,
    onCerrarSesion: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para switches interactivos de la referencia
    var notificacionesHabilitadas by remember { mutableStateOf(true) }
    var sonidoEmergenciaHabilitado by remember { mutableStateOf(true) }

    // Estados para prueba de conexión real con Railway
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionResult by remember { mutableStateOf<String?>(null) }
    var connectionSuccess by remember { mutableStateOf<Boolean?>(null) }
    var showAcercaDeDialog by remember { mutableStateOf(false) }

    // Datos del usuario que inició sesión
    val usuarioNombre = usuarioActual?.nombre ?: "Usuario"
    val usuarioEmail = usuarioActual?.email ?: "Sin correo"
    val usuarioRol = usuarioActual?.rol ?: "Usuario"

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
            } catch (ex: Exception) {
                Log.e("USUARIOS_ERROR", "Fallo al probar conexión", ex)
                connectionSuccess = false
                connectionResult = "No se pudo conectar con el servidor."
            } finally {
                isTestingConnection = false
            }
        }
    }

    Scaffold(
        containerColor = DarkNavyBg,
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
                                .background(Color(0xFF0F172A))
                                .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Logo ComuniMap",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Ajustes y Cuenta",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavyBg,
                    titleContentColor = Color.White
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // =====================================================
            // 1. TARJETA DE PERFIL
            // =====================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tarjeta_perfil"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                .background(Color(0xFF0D5E54)), // Fondo verde azulado oscuro
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iniciales,
                                color = Color(0xFF2DD4BF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Información: Nombre, correo y badge de rol
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = usuarioNombre,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = usuarioEmail,
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Badge de Rol
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF064E3B))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = usuarioRol,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Flecha derecha
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver Perfil",
                            tint = TextMuted,
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
                    color = AccentCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Tema de la Aplicación
                        AjusteItemRow(
                            icono = Icons.Default.BrightnessAuto,
                            colorIcono = Color(0xFFA78BFA),
                            colorFondoIcono = Color(0xFF2E2356),
                            titulo = "Tema de la Aplicación",
                            subtitulo = "Seguir configuración del sistema",
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Tema sincronizado con el sistema")
                                }
                            }
                        )

                        HorizontalDivider(
                            color = Color(0xFF1E293B),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 2. Mi Perfil
                        AjusteItemRow(
                            icono = Icons.Default.Person,
                            colorIcono = Color(0xFF60A5FA),
                            colorFondoIcono = Color(0xFF1E3A8A),
                            titulo = "Mi Perfil",
                            subtitulo = "Modificar nombre, teléfono y credenciales",
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Perfil: $usuarioNombre ($usuarioRol)")
                                }
                            }
                        )

                        HorizontalDivider(
                            color = Color(0xFF1E293B),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 3. Notificaciones de Alertas (Switch)
                        AjusteSwitchRow(
                            icono = Icons.Default.Notifications,
                            colorIcono = Color(0xFFFBBF24),
                            colorFondoIcono = Color(0xFF452B14),
                            titulo = "Notificaciones de Alertas",
                            subtitulo = "Avisos de incidentes en tu barrio",
                            checked = notificacionesHabilitadas,
                            onCheckedChange = { notificacionesHabilitadas = it }
                        )

                        HorizontalDivider(
                            color = Color(0xFF1E293B),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 4. Sonido de Emergencia (Switch)
                        AjusteSwitchRow(
                            icono = Icons.Default.VolumeUp,
                            colorIcono = Color(0xFF34D399),
                            colorFondoIcono = Color(0xFF0F4336),
                            titulo = "Sonido de Emergencia",
                            subtitulo = "Efectos sonoros en reportes urgentes",
                            checked = sonidoEmergenciaHabilitado,
                            onCheckedChange = { sonidoEmergenciaHabilitado = it }
                        )
                    }
                }
            }

            // =====================================================
            // 3. SECCIÓN ADICIONAL: ACERCA DE COMUNIMAP
            // =====================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { showAcercaDeDialog = true }
                        .testTag("card_acerca_de"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge circular/redondeado cian
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C4A6E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acerca de ComuniMap",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Versión 2.1.0 • Plataforma Vecinal",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // =====================================================
            // 4. BOTÓN INFERIOR: CERRAR SESIÓN
            // =====================================================
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onCerrarSesion,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB91C1C) // Rojo profundo como en la referencia
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

    // Diálogo con detalles de versión y botón para Probar Conexión real
    if (showAcercaDeDialog) {
        AlertDialog(
            onDismissRequest = { showAcercaDeDialog = false },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = TextMuted,
            title = {
                Text(
                    text = "ComuniMap 2.1.0",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Plataforma Vecinal interactiva para Cochabamba.",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Servidor: Railway Producción",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Text(
                        text = BASE_URL,
                        fontSize = 12.sp,
                        color = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón para probar conexión real
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
                        val colorText = if (isOk) Color(0xFF4ADE80) else Color(0xFFF87171)
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
                    Text("Cerrar", color = AccentCyan)
                }
            }
        )
    }
}

// Fila para cada ajuste con navegación o acción
@Composable
private fun AjusteItemRow(
    icono: ImageVector,
    colorIcono: Color,
    colorFondoIcono: Color,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícono dentro de un contenedor redondeado con color específico
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
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
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
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // Switch estilizado con píldora azul y botón negro/oscuro acorde a la captura
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF0F172A),
                checkedTrackColor = Color(0xFF60A5FA),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
