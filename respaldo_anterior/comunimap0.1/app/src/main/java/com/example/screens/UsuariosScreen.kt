package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.models.Usuario

// =========================================================================
// 👥 PANTALLA: USUARIOS (CONSULTA Y FILTRADO REAL DE USUARIOS)
// =========================================================================
// Pantalla de solo lectura conectada directamente con `usuarios.php`.
// Permite buscar, filtrar por rol y refrescar los usuarios de MySQL.
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    usuarios: List<Usuario>,
    estaCargando: Boolean = false,
    onRefrescar: () -> Unit = {}
) {
    var busqueda by remember { mutableStateOf("") }
    var rolFiltro by remember { mutableStateOf("Todos") }

    val usuariosFiltrados = remember(usuarios, busqueda, rolFiltro) {
        usuarios.filter { u ->
            val coincideTexto = busqueda.isBlank() ||
                    u.nombre.contains(busqueda, ignoreCase = true) ||
                    u.email.contains(busqueda, ignoreCase = true) ||
                    (u.telefono != null && u.telefono.contains(busqueda))

            val coincideRol = when (rolFiltro) {
                "Todos" -> true
                else -> u.rol.equals(rolFiltro, ignoreCase = true)
            }

            coincideTexto && coincideRol
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Directorio de Usuarios",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${usuariosFiltrados.size} usuarios registrados en MySQL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Botón para refrescar usuarios desde MySQL (usuarios.php)
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
                                contentDescription = "Sincronizar usuarios",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Buscador de usuarios
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar usuario por nombre o email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Chips de filtrado por Rol
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Administrador", "Usuario").forEach { rol ->
                    FilterChip(
                        selected = rolFiltro == rol,
                        onClick = { rolFiltro = rol },
                        label = { Text(rol, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (usuariosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (estaCargando) "Cargando usuarios de MySQL..." else "No hay usuarios disponibles",
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
                                Text("Refrescar usuarios")
                            }
                        }
                    }
                }
            } else {
                // Lista de Usuarios
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(usuariosFiltrados, key = { it.idUsuario }) { usuario ->
                        ItemUsuarioCard(usuario = usuario)
                    }
                }
            }
        }
    }
}

// Tarjeta de información de un Usuario (solo lectura)
@Composable
fun ItemUsuarioCard(
    usuario: Usuario
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con iniciales
            val colorAvatar = when (usuario.rol.lowercase()) {
                "administrador" -> Color(0xFF7C3AED)
                else -> Color(0xFF2563EB)
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colorAvatar.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usuario.nombre.take(2).uppercase(),
                    color = colorAvatar,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!usuario.telefono.isNullOrBlank()) {
                    Text(
                        text = "Tel: ${usuario.telefono}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge del Rol
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colorAvatar.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = usuario.rol,
                            color = colorAvatar,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Badge del Estado
                    val colorEstado = if (usuario.estado.equals("Activo", ignoreCase = true)) Color(0xFF16A34A) else Color(0xFF6B7280)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colorEstado.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = usuario.estado,
                            color = colorEstado,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
