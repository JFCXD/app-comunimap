package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO DE RESPUESTA DE SUBIDA DE IMAGEN (LARAVEL)
// =====================================================

// Mapea la respuesta devuelta por POST /api/subir-imagen-reporte en Laravel
@JsonClass(generateAdapter = true)
data class SubirImagenResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "mensaje")
    val mensaje: String? = null,

    @Json(name = "ruta")
    val ruta: String? = null,

    @Json(name = "url")
    val url: String? = null
)
