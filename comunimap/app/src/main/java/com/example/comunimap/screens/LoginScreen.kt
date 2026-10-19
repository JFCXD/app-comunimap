package com.example.comunimap.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.LoginRequest
import com.example.comunimap.models.Usuario
import kotlinx.coroutines.launch

// =====================================================
// PANTALLA: INICIO DE SESIÓN (LOGIN)
// =====================================================

// Colores del tema visual oscuro / navy según la captura de referencia
private val DarkNavyBg = Color(0xFF0B132B)
private val DarkCardBg = Color(0xFF162238)
private val AccentBlue = Color(0xFF60A5FA)
private val AccentBlueButton = Color(0xFF3B82F6)
private val TextMuted = Color(0xFF94A3B8)
private val BorderColor = Color(0xFF334155)

@Composable
fun LoginScreen(
    onLoginSuccess: (Usuario) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var registroExitosoMensaje by remember { mutableStateOf<String?>(null) }

    var showRegistroDialog by remember { mutableStateOf(false) }
    var showTerminosDialog by remember { mutableStateOf(false) }
    var showPrivacidadDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Función para autenticar contra login.php en Railway
    fun ejecutarLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Por favor ingresa tu correo y contraseña."
            return
        }

        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Enviar correo y contraseña en texto plano (el backend PHP aplica sha1())
                val req = LoginRequest(
                    email = email.trim(),
                    password = password
                )
                Log.d("USUARIOS_HTTP", "POST login.php para email: ${req.email}")
                val response = ApiClient.apiService.login(req)
                Log.d("USUARIOS_HTTP", "HTTP: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.usuario != null) {
                        Log.d("USUARIOS_RESPONSE", "Login exitoso para: ${body.usuario.nombre} (${body.usuario.rol})")
                        onLoginSuccess(body.usuario)
                    } else {
                        errorMessage = body?.message ?: "Correo o contraseña incorrectos."
                    }
                } else {
                    when (response.code()) {
                        400 -> errorMessage = "Completa correctamente el correo y la contraseña."
                        401 -> errorMessage = "Correo o contraseña incorrectos."
                        500 -> errorMessage = "No se pudo iniciar sesión. Inténtalo nuevamente."
                        else -> errorMessage = "No se pudo iniciar sesión. Inténtalo nuevamente."
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (ex: java.net.SocketTimeoutException) {
                Log.e("USUARIOS_ERROR", "Timeout en login", ex)
                errorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a Internet."
            } catch (ex: java.net.UnknownHostException) {
                Log.e("USUARIOS_ERROR", "Sin conexión en login", ex)
                errorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a Internet."
            } catch (ex: java.io.IOException) {
                Log.e("USUARIOS_ERROR", "Error de red en login", ex)
                errorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a Internet."
            } catch (ex: Exception) {
                Log.e("USUARIOS_ERROR", "Error en login", ex)
                errorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a Internet."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = DarkNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // =====================================================
            // LOGO CIRCULAR DE COMUNIMAP
            // =====================================================
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFF38BDF8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Logo ComuniMap",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Título
            Text(
                text = "ComuniMap",
                color = AccentBlue,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // Subtítulo
            Text(
                text = "MAPA INTERACTIVO",
                color = AccentBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slogan
            Text(
                text = "Tu barrio, tu reporte, un mejor futuro.",
                color = TextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // =====================================================
            // TARJETA DE CREDENCIALES
            // =====================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Bienvenido",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Ingresa tus credenciales para continuar",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo: Correo Electrónico
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                            registroExitosoMensaje = null
                        },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = AccentBlue
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo: Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                            registroExitosoMensaje = null
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AccentBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { ejecutarLogin() }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Mensaje de éxito tras registro de cuenta
                    if (registroExitosoMensaje != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_registro_exitoso_banner"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = registroExitosoMensaje ?: "",
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Mensaje de error
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.testTag("login_error_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón: Iniciar Sesión
                    Button(
                        onClick = { ejecutarLogin() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlueButton
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_iniciar_sesion")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Iniciando sesión...", color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Iniciar Sesión",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opción para registrar nueva cuenta de vecino
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿No tienes cuenta? ",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Crear cuenta",
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable {
                                    errorMessage = null
                                    registroExitosoMensaje = null
                                    showRegistroDialog = true
                                }
                                .padding(vertical = 4.dp)
                                .testTag("btn_crear_cuenta")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Enlaces informativos para vecinos antes de iniciar sesión
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Términos y Condiciones",
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { showTerminosDialog = true }
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .testTag("link_terminos_login")
                        )

                        Text(
                            text = "•",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Text(
                            text = "Política de Privacidad",
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { showPrivacidadDialog = true }
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .testTag("link_privacidad_login")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // =====================================================
    // DIÁLOGO: TÉRMINOS Y CONDICIONES (LOGIN)
    // =====================================================
    if (showTerminosDialog) {
        AlertDialog(
            onDismissRequest = { showTerminosDialog = false },
            containerColor = Color(0xFF162238),
            titleContentColor = Color.White,
            textContentColor = TextMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C4A6E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Términos y Condiciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
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
                        text = "Bienvenido a ComuniMap, la plataforma vecinal para Cochabamba. Al usar la aplicación aceptas los siguientes términos:",
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 16.sp
                    )

                    Column {
                        Text("1. Propósito Vecinal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "ComuniMap permite registrar y visualizar problemas comunitarios (baches, alumbrado, basura, seguridad) en un mapa interactivo.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text("2. Veracidad de los Reportes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "Te comprometes a subir reportes verdaderos con ubicación precisa. Queda prohibido emitir reportes falsos que movilicen cuadrillas indebidamente.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text("3. Respeto en la Comunidad", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "No se tolera lenguaje ofensivo, discriminatorio o contenido inapropiado en descripciones e imágenes.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text("4. Moderación y Autoridades", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "Los administradores supervisan las publicaciones y actualizan los estados de los incidentes conforme son atendidos.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTerminosDialog = false }) {
                    Text("Cerrar", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO: POLÍTICA DE PRIVACIDAD (LOGIN)
    // =====================================================
    if (showPrivacidadDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacidadDialog = false },
            containerColor = Color(0xFF162238),
            titleContentColor = Color.White,
            textContentColor = TextMuted,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF064E3B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Política de Privacidad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
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
                        text = "En ComuniMap la protección de la privacidad vecinal es prioritaria:",
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 16.sp
                    )

                    Column {
                        Text("1. Datos Personales", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "Solicitamos tu correo y nombre para identificar la autoría de reportes. Las credenciales están protegidas con hash criptográfico en la base de datos MySQL.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text("2. Ubicación GPS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "Solo se utiliza para ubicar incidentes en el mapa de Cochabamba. No realizamos seguimiento pasivo ni vendemos tus coordenadas.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text("3. Alertas Push FCM", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "Tu token FCM permite que el sistema te notifique sobre emergencias o cambios de estado en reportes comunitarios.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Column {
                        Text("4. Cifrado HTTPS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text(
                            "Toda comunicación hacia el servidor en Railway está protegida mediante certificados SSL/TLS seguros.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacidadDialog = false }) {
                    Text("Cerrar", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // =====================================================
    // DIÁLOGO: REGISTRO DE NUEVA CUENTA DE VECINO
    // =====================================================
    if (showRegistroDialog) {
        RegistroDialog(
            onDismiss = { showRegistroDialog = false },
            onRegistroExitoso = { emailRegistrado ->
                email = emailRegistrado
                password = ""
                showRegistroDialog = false
                registroExitosoMensaje = "Cuenta creada correctamente. Ya puedes iniciar sesión con tu correo y contraseña."
                errorMessage = null
            },
            onVerTerminos = { showTerminosDialog = true },
            onVerPrivacidad = { showPrivacidadDialog = true }
        )
    }
}
