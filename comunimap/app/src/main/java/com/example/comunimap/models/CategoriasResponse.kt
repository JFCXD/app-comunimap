package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// RESPUESTA DE LA API: CATEGORÍAS
// =====================================================

// Representa la estructura devuelta por el endpoint categorias.php.
@JsonClass(generateAdapter = true)
data class CategoriasResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "total")
    val total: Int,

    @Json(name = "categorias")
    val categorias: List<Categoria>
)
