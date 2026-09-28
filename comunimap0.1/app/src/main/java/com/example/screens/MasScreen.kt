package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.models.Usuario

// =========================================================================
// ⚙️ PANTALLA: MÁS (CONFIGURACIÓN, PERFIL Y CERRAR SESIÓN)
// =========================================================================
// Ofrece las opciones estándar y esenciales de la aplicación:
// 1. Perfil de usuario (datos, rol y edición de contacto).
// 2. Configuración básica (Modo Oscuro/Claro, Notificaciones, Sonidos).
// 3. Cerrar sesión con confirmación.
// =========================================================================

enum class ModoTema {
    SISTEMA,
    CLARO,
    OSCURO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasScreen(
    usuarioActual: Usuario?,
    modoTemaActual: ModoTema,
    onCambiarModoTema: (ModoTema) -> Unit,
    onActualizarPerfil: (Usuario) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var mostrarDialogoPerfil by remember { mutableStateOf(false) }
    var mostrarDialogoConfiguracion by remember { mutableStateOf(false) }
    var mostrarDialogoAcercaDe by remember { mutableStateOf(false) }
    var mostrarConfirmarCerrarSesion by remember { mutableStateOf(false) }

    // Estados de configuración rápida
    var notificacionesHabilitadas by remember { mutableStateOf(true) }
    var sonidosHabilitados by remember { mutableStateOf(true) }
    var gpsAltaPrecision by remember { mutableStateOf(true) }
    var autoSincronizar by remember { mutableStateOf(true) }

    val usuario = usuarioActual ?: Usuario(
        idUsuario = 1,
        nombre = "Lic. Carlos Mendoza",
        email = "admin@comunimap.gob",
        telefono = "+591 70012345",
        rol = "Administrador",
        estado = "Activo"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, shape = CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.comunimap_logo),
                                contentDescription = "Logo ComuniMap",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ajustes y Cuenta",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =============================================================
            // 👤 SECCIÓN: TARJETA DE PERFIL
            // =============================================================
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDialogoPerfil = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar estilizado
                        val colorAvatar = when (usuario.rol.lowercase()) {
                            "administrador" -> Color(0xFF7C3AED)
                            "operador" -> Color(0xFF2563EB)
                            else -> Color(0xFF059669)
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colorAvatar.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = usuario.nombre.take(2).uppercase(),
                                color = colorAvatar,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = usuario.nombre,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = usuario.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colorAvatar.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = usuario.rol,
                                    color = colorAvatar,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver Perfil",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // =============================================================
            // ⚙️ SECCIÓN: CONFIGURACIÓN BÁSICA
            // =============================================================
            item {
                Text(
                    text = "Ajustes Principales",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        // Opción: Tema Claro / Oscuro
                        OpcionMasItem(
                            icono = when (modoTemaActual) {
                                ModoTema.OSCURO -> Icons.Default.DarkMode
                                ModoTema.CLARO -> Icons.Default.LightMode
                                ModoTema.SISTEMA -> Icons.Default.BrightnessAuto
                            },
                            colorIcono = Color(0xFF8B5CF6),
                            titulo = "Tema de la Aplicación",
                            subtitulo = when (modoTemaActual) {
                                ModoTema.OSCURO -> "Modo Oscuro activo"
                                ModoTema.CLARO -> "Modo Claro activo"
                                ModoTema.SISTEMA -> "Seguir configuración del sistema"
                            },
                            onClick = { mostrarDialogoConfiguracion = true }
                        )

                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        // Opción: Mi Perfil
                        OpcionMasItem(
                            icono = Icons.Default.AccountCircle,
                            colorIcono = Color(0xFF2563EB),
                            titulo = "Mi Perfil",
                            subtitulo = "Modificar nombre, teléfono y credenciales",
                            onClick = { mostrarDialogoPerfil = true }
                        )

                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        // Opción: Notificaciones
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notificaciones de Alertas",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Avisos de incidentes en tu barrio",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = notificacionesHabilitadas,
                                onCheckedChange = { notificacionesHabilitadas = it }
                            )
                        }

                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )

                        // Opción: Sonidos de alerta
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sonido de Emergencia",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Efectos sonoros en reportes urgentes",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = sonidosHabilitados,
                                onCheckedChange = { sonidosHabilitados = it }
                            )
                        }
                    }
                }
            }

            // =============================================================
            // ℹ️ SECCIÓN: ACERCA DE LA APLICACIÓN
            // =============================================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OpcionMasItem(
                        icono = Icons.Default.Info,
                        colorIcono = Color(0xFF0284C7),
                        titulo = "Acerca de ComuniMap",
                        subtitulo = "Versión 2.1.0 • Plataforma Vecinal",
                        onClick = { mostrarDialogoAcercaDe = true }
                    )
                }
            }

            // =============================================================
            // 🚪 SECCIÓN: CERRAR SESIÓN
            // =============================================================
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { mostrarConfirmarCerrarSesion = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("boton_cerrar_sesion")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // Modal: Selección de Tema (Claro / Oscuro / Sistema)
    if (mostrarDialogoConfiguracion) {
        Dialog(onDismissRequest = { mostrarDialogoConfiguracion = false }) {
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
                    Text(
                        text = "Configurar Tema",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Elige la apariencia visual que prefieras",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Opción Modo Claro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCambiarModoTema(ModoTema.CLARO)
                                mostrarDialogoConfiguracion = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = modoTemaActual == ModoTema.CLARO,
                            onClick = {
                                onCambiarModoTema(ModoTema.CLARO)
                                mostrarDialogoConfiguracion = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.LightMode, contentDescription = null, tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tema Claro", fontWeight = FontWeight.SemiBold)
                    }

                    // Opción Modo Oscuro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCambiarModoTema(ModoTema.OSCURO)
                                mostrarDialogoConfiguracion = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = modoTemaActual == ModoTema.OSCURO,
                            onClick = {
                                onCambiarModoTema(ModoTema.OSCURO)
                                mostrarDialogoConfiguracion = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tema Oscuro", fontWeight = FontWeight.SemiBold)
                    }

                    // Opción Automático / Sistema
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCambiarModoTema(ModoTema.SISTEMA)
                                mostrarDialogoConfiguracion = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = modoTemaActual == ModoTema.SISTEMA,
                            onClick = {
                                onCambiarModoTema(ModoTema.SISTEMA)
                                mostrarDialogoConfiguracion = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.BrightnessAuto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Predeterminado del Sistema", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TextButton(
                        onClick = { mostrarDialogoConfiguracion = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

    // Modal: Editar Perfil
    if (mostrarDialogoPerfil) {
        var nombreEdit by remember { mutableStateOf(usuario.nombre) }
        var emailEdit by remember { mutableStateOf(usuario.email) }
        var telefonoEdit by remember { mutableStateOf(usuario.telefono ?: "") }

        Dialog(onDismissRequest = { mostrarDialogoPerfil = false }) {
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
                    Text(
                        text = "Mi Perfil",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nombreEdit,
                        onValueChange = { nombreEdit = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailEdit,
                        onValueChange = { emailEdit = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = telefonoEdit,
                        onValueChange = { telefonoEdit = it },
                        label = { Text("Teléfono de Contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { mostrarDialogoPerfil = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onActualizarPerfil(
                                    usuario.copy(
                                        nombre = nombreEdit,
                                        email = emailEdit,
                                        telefono = telefonoEdit.ifBlank { null }
                                    )
                                )
                                mostrarDialogoPerfil = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }

    // Modal: Acerca de
    if (mostrarDialogoAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAcercaDe = false },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.comunimap_logo),
                    contentDescription = "Logo ComuniMap",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            },
            title = {
                Text(text = "ComuniMap v2.1.0", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MAPA INTERACTIVO\nTu barrio, tu reporte, un mejor futuro.",
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Plataforma comunitaria para la gestión de incidentes urbanos, alumbrado, vías y emergencias barriales.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoAcercaDe = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Diálogo de Confirmación: Cerrar Sesión
    if (mostrarConfirmarCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarCerrarSesion = false },
            icon = {
                Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("¿Deseas cerrar sesión?") },
            text = {
                Text("Regresarás a la pantalla de inicio de sesión de ComuniMap.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarCerrarSesion = false
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Componente reutilizable para opciones en la lista
@Composable
fun OpcionMasItem(
    icono: ImageVector,
    colorIcono: Color,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colorIcono.copy(alpha = 0.15f),
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitulo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
