package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO DE DATOS: CATEGORÍA
// =====================================================

// Representa las categorías disponibles para clasificar reportes urbanos.
@JsonClass(generateAdapter = true)
data class Categoria(
    @Json(name = "idCategoria")
    val idCategoria: String?,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "descripcion")
    val descripcion: String?
)
