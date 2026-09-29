package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO: RESPUESTA DE INICIO DE SESIÓN
// =====================================================

// Estructura de respuesta del endpoint login.php
@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "usuario")
    val usuario: Usuario? = null
)
