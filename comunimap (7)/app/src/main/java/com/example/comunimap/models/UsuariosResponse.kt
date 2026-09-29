package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// RESPUESTA DE LA API: USUARIOS
// =====================================================

// Representa la estructura de respuesta JSON completa del endpoint usuarios.php.
@JsonClass(generateAdapter = true)
data class UsuariosResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "total")
    val total: Int,

    @Json(name = "usuarios")
    val usuarios: List<Usuario>
)
