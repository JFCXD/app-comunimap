package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO: SOLICITUD DE INICIO DE SESIÓN
// =====================================================

// Se envía al endpoint login.php en Railway con email y contraseña en texto plano.
// La API en PHP se encarga de convertir la contraseña con sha1() antes de consultar MySQL.
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String
)
