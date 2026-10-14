package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ====================================================================================================
// 👥 MODELO: USUARIO (Tabla `usuarios` en MySQL `gestion_alertas`)
// ====================================================================================================
// Estructura en la base de datos MySQL y respuesta de `usuarios.php`:
// - idUsuario: INT AUTO_INCREMENT PRIMARY KEY
// - nombre: VARCHAR(100)
// - email: VARCHAR(100) UNIQUE
// - telefono: VARCHAR(20)
// - rol: VARCHAR / ENUM('Administrador', 'Usuario')
// - estado: VARCHAR(20)
// ====================================================================================================

@JsonClass(generateAdapter = true)
data class Usuario(
    @Json(name = "idUsuario")
    val idUsuario: Int = 0,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "telefono")
    val telefono: String? = null,

    @Json(name = "rol")
    val rol: String = "Usuario", // "Administrador", "Usuario"

    @Json(name = "estado")
    val estado: String = "Activo"
) {
    // Propiedad de compatibilidad si alguna referencia secundaria la utiliza
    val id: Int
        get() = idUsuario
}

