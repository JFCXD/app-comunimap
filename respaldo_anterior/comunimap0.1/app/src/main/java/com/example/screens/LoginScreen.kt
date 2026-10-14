package com.example.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.models.Usuario

// ====================================================================================================
// 🔐 PANTALLA: LOGIN / INICIO DE SESIÓN (ComuniMap)
// ====================================================================================================
// Esta pantalla gestiona la autenticación inicial del usuario en la app.
//
// 📌 ¿CÓMO SE PUEDE MODIFICAR O CONECTAR CON UNA BASE DE DATOS / API REAL?
// 1. Conexión Backend PHP/MySQL:
//    - Puedes reemplazar la lógica dentro del `onClick` del botón "Iniciar Sesión" para hacer una
//      llamada HTTP POST (con Retrofit o Ktor) hacia tu endpoint en PHP (ej: `login.php`).
//    - Ejemplo: `val respuesta = apiService.login(LoginRequest(email, password))`
// 2. Autenticación con Firebase Auth / Google Sign-In:
//    - Puedes integrar FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
// 3. Roles en el sistema:
//    - Actualmente detecta el rol por la presencia de palabras clave o respuesta del servidor
//      ("Administrador", "Operador", "Ciudadano").
// ====================================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    estaCargando: Boolean = false
) {
    // ------------------------------------------------------------------------------------------------
    // 📝 VARIABLES DE ESTADO DEL FORMULARIO
    // Modifica los valores por defecto si deseas iniciar con campos vacíos ("")
    // ------------------------------------------------------------------------------------------------
    var email by remember { mutableStateOf("jose@gmail.com") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ----------------------------------------------------------------------------------------
            // 🎨 SECCIÓN 1: LOGOTIPO OFICIAL E IDENTIDAD VISUAL DE COMUNIMAP
            // El logo se carga desde `res/drawable/img_comunimap_logo.png`.
            // Para cambiar el logo, sustituye dicho recurso o cambia el ID aquí.
            // ----------------------------------------------------------------------------------------
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(12.dp, shape = CircleShape),
                shape = CircleShape,
                color = Color.White
            ) {
                Image(
                    painter = painterResource(id = R.drawable.comunimap_logo),
                    contentDescription = "Logo ComuniMap",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------------------------------------------------------------------------------------
            // 🏷️ SECCIÓN 2: TÍTULOS Y AVISO DEMO TEMPORAL
            // ----------------------------------------------------------------------------------------
            Text(
                text = "ComuniMap",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "MAPA INTERACTIVO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.secondary
            )



            Text(
                text = "Tu barrio, tu reporte, un mejor futuro.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // ----------------------------------------------------------------------------------------
            // 📋 SECCIÓN 3: TARJETA DE CREDENCIALES (FORMULARIO DE LOGIN)
            // ----------------------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Ingresa tus credenciales para continuar",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp)
                    )

                    // Campo de entrada: Correo Electrónico
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMensaje = null
                        },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo de entrada: Contraseña (con botón de mostrar/ocultar)
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMensaje = null
                        },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Visualizador de mensajes de error de validación
                    if (errorMensaje != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMensaje!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // --------------------------------------------------------------------------------
                    // 🚀 BOTÓN PRINCIPAL DE INICIO DE SESIÓN
                    // Valida el formato y emite el usuario autenticado hacia MainActivity / ViewModel.
                    // --------------------------------------------------------------------------------
                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                errorMensaje = "Por favor ingresa tu correo electrónico"
                            } else if (password.isBlank()) {
                                errorMensaje = "Por favor ingresa tu contraseña"
                            } else {
                                // 💡 Determinación del rol del usuario (Administrador o Usuario)
                                val rol = when {
                                    email.contains("admin", ignoreCase = true) -> "Administrador"
                                    else -> "Usuario"
                                }
                                val nombre = email.substringBefore("@")
                                    .replace(".", " ")
                                    .replaceFirstChar { it.uppercase() }

                                onLoginExitoso(
                                    Usuario(
                                        idUsuario = 1,
                                        nombre = if (nombre.isBlank()) "Carlos Mendoza" else nombre,
                                        email = email.trim(),
                                        telefono = "+591 70012345",
                                        rol = rol,
                                        estado = "Activo"
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (estaCargando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
