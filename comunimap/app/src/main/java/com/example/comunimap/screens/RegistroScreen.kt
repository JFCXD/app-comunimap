package com.example.comunimap.screens

import android.app.DatePickerDialog
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.CrearUsuarioRequest
import com.example.comunimap.utils.FechaUtils
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Calendar

// Paleta visual consistente con el tema navy de ComuniMap
private val DarkCardBg = Color(0xFF162238)
private val AccentBlue = Color(0xFF60A5FA)
private val AccentBlueButton = Color(0xFF3B82F6)
private val TextMuted = Color(0xFF94A3B8)
private val BorderColor = Color(0xFF334155)

/**
 * Diálogo / Pantalla modal para el registro de nuevos vecinos en ComuniMap.
 *
 * REGLAS:
 * - Rol asignado automáticamente: "Usuario" (sin opción de modificarlo)
 * - Estado inicial en el backend: "Activo"
 * - La contraseña se envía en texto plano (el backend PHP aplica sha1())
 * - Filtra errores técnicos (SQL, Duplicate entry, etc.) para ofrecer mensajes comprensibles
 */
@Composable
fun RegistroDialog(
    onDismiss: () -> Unit,
    onRegistroExitoso: (emailRegistrado: String) -> Unit,
    onVerTerminos: () -> Unit,
    onVerPrivacidad: () -> Unit
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") } // Formato ISO "YYYY-MM-DD"
    var fechaNacimientoVisual by remember { mutableStateOf("") } // Formato visual "DD/MM/YYYY"
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Interpreta la respuesta del backend de forma segura y amigable
    fun interpretarError(codigoHttp: Int, mensajeRaw: String?): String {
        val msg = mensajeRaw?.lowercase() ?: ""
        return when {
            msg.contains("ya existe") || msg.contains("ya está registrado") || msg.contains("duplicate") ||
                    (msg.contains("correo") && msg.contains("registrado")) || codigoHttp == 409 -> {
                "Ya existe una cuenta registrada con este correo."
            }
            codigoHttp == 400 -> {
                "Por favor verifica los datos ingresados."
            }
            else -> {
                if (!mensajeRaw.isNullOrBlank() &&
                    !msg.contains("sql") &&
                    !msg.contains("syntax") &&
                    !msg.contains("exception") &&
                    !msg.contains("mysqli") &&
                    !msg.contains("database") &&
                    !msg.contains("fatal")
                ) {
                    mensajeRaw
                } else {
                    "No se pudo crear la cuenta. Inténtalo nuevamente."
                }
            }
        }
    }

    fun ejecutarRegistro() {
        if (isSubmitting) return

        val nom = nombre.trim()
        val ape = apellido.trim()
        val fechaNac = fechaNacimiento.trim()
        val correo = email.trim()
        val tel = telefono.trim()

        if (nom.isBlank()) {
            formError = "Ingresa tu nombre"
            return
        }

        if (ape.isBlank()) {
            formError = "Ingresa tu apellido"
            return
        }

        if (fechaNac.isBlank()) {
            formError = "Selecciona tu fecha de nacimiento"
            return
        }

        if (correo.isBlank()) {
            formError = "Ingresa un correo electrónico"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            formError = "Ingresa un correo válido"
            return
        }

        if (tel.isBlank()) {
            formError = "Ingresa tu número de teléfono"
            return
        }

        if (password.isBlank()) {
            formError = "Ingresa una contraseña"
            return
        }

        if (confirmPassword.isBlank()) {
            formError = "Confirma tu contraseña"
            return
        }

        if (password != confirmPassword) {
            formError = "Las contraseñas no coinciden"
            return
        }

        coroutineScope.launch {
            isSubmitting = true
            formError = null
            try {
                // Rol fijado siempre a "Usuario" para registro vecinal público
                val request = CrearUsuarioRequest(
                    nombre = nom,
                    apellido = ape,
                    fechaNacimiento = fechaNac,
                    email = correo,
                    password = password,
                    telefono = tel,
                    rol = "Usuario"
                )

                Log.d("REGISTRO_HTTP", "POST crear_usuario.php para email: ${request.email}")
                val response = ApiClient.apiService.crearUsuario(request)
                Log.d("REGISTRO_HTTP", "HTTP: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Log.d("REGISTRO_SUCCESS", "Usuario registrado exitosamente: $correo")
                        onRegistroExitoso(correo)
                    } else {
                        formError = interpretarError(response.code(), body?.message)
                    }
                } else {
                    val errorBodyString = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    formError = interpretarError(response.code(), errorBodyString)
                }
            } catch (ex: IOException) {
                Log.e("REGISTRO_ERROR", "Error de red al crear cuenta", ex)
                formError = "No se pudo crear la cuenta. Verifica tu conexión e inténtalo nuevamente."
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (ex: Exception) {
                Log.e("REGISTRO_ERROR", "Excepción inesperada al crear cuenta", ex)
                formError = "No se pudo crear la cuenta. Verifica tu conexión e inténtalo nuevamente."
            } finally {
                isSubmitting = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        containerColor = DarkCardBg,
        titleContentColor = Color.White,
        textContentColor = TextMuted,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Crear Cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Registro de vecino en ComuniMap",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo: Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        formError = null
                    },
                    label = { Text("Nombre *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentBlue
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
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
                        .testTag("registro_nombre_input")
                )

                // Campo: Apellido
                OutlinedTextField(
                    value = apellido,
                    onValueChange = {
                        if (it.length <= 150) {
                            apellido = it
                            formError = null
                        }
                    },
                    label = { Text("Apellido *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentBlue
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
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
                        .testTag("registro_apellido_input")
                )

                // Campo: Fecha de Nacimiento con selector nativo DatePickerDialog
                val abrirDatePicker = {
                    val cal = Calendar.getInstance()
                    if (fechaNacimiento.isNotBlank()) {
                        val parts = fechaNacimiento.split("-")
                        if (parts.size == 3) {
                            val y = parts[0].toIntOrNull()
                            val m = parts[1].toIntOrNull()
                            val d = parts[2].toIntOrNull()
                            if (y != null && m != null && d != null) {
                                cal.set(y, m - 1, d)
                            }
                        }
                    } else {
                        cal.add(Calendar.YEAR, -20)
                    }
                    val dpd = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val mesReal = m + 1
                            fechaNacimiento = FechaUtils.formatearFechaIso(y, mesReal, d)
                            fechaNacimientoVisual = FechaUtils.formatearFechaVisual(y, mesReal, d)
                            formError = null
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                    dpd.datePicker.maxDate = System.currentTimeMillis()
                    dpd.show()
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fechaNacimientoVisual,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de nacimiento *") },
                        placeholder = { Text("DD/MM/AAAA", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = AccentBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = abrirDatePicker) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar fecha de nacimiento",
                                    tint = AccentBlue
                                )
                            }
                        },
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
                            .clickable { abrirDatePicker() }
                            .testTag("registro_fecha_nacimiento_input")
                    )

                    val edadCalculada = FechaUtils.calcularEdad(fechaNacimiento)
                    if (edadCalculada != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentBlue.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Edad calculada: $edadCalculada años",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Campo: Correo Electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        formError = null
                    },
                    label = { Text("Correo electrónico *") },
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
                        .testTag("registro_email_input")
                )

                // Campo: Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        formError = null
                    },
                    label = { Text("Teléfono *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = AccentBlue
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
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
                        .testTag("registro_telefono_input")
                )

                // Campo: Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        formError = null
                    },
                    label = { Text("Contraseña *") },
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
                        .testTag("registro_password_input")
                )

                // Campo: Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        formError = null
                    },
                    label = { Text("Confirmar contraseña *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AccentBlue
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = TextMuted
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { ejecutarRegistro() }
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
                        .testTag("registro_confirm_password_input")
                )

                // Mensaje de error amigable
                if (formError != null) {
                    Text(
                        text = formError ?: "",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.testTag("registro_error_text")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Cláusula de Términos y Condiciones y Privacidad
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Al registrarte, aceptas los Términos y Condiciones y la Política de Privacidad de ComuniMap.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Términos y Condiciones",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onVerTerminos() }
                                .padding(vertical = 2.dp)
                                .testTag("link_terminos_registro")
                        )

                        Text(
                            text = "  •  ",
                            color = TextMuted,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "Política de Privacidad",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onVerPrivacidad() }
                                .padding(vertical = 2.dp)
                                .testTag("link_privacidad_registro")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { ejecutarRegistro() },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlueButton
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_confirmar_registro")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrando...", color = Color.White)
                } else {
                    Text("Registrarme", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                enabled = !isSubmitting,
                modifier = Modifier.testTag("btn_cancelar_registro")
            ) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}
