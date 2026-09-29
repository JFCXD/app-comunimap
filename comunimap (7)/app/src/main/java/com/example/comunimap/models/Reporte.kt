package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO DE DATOS: REPORTE
// =====================================================

// Representa exactamente los campos devueltos por el endpoint reportes.php desde MySQL.
@JsonClass(generateAdapter = true)
data class Reporte(
    @Json(name = "idReporte")
    val idReporte: String?,

    @Json(name = "titulo")
    val titulo: String,

    @Json(name = "descripcion")
    val descripcion: String,

    @Json(name = "ubicacion")
    val ubicacion: String?,

    @Json(name = "direccion")
    val direccion: String?,

    @Json(name = "estado")
    val estado: String,

    @Json(name = "imagen")
    val imagen: String?,

    @Json(name = "idUsuario")
    val idUsuario: String?,

    @Json(name = "usuario")
    val usuario: String?,

    @Json(name = "idCategoria")
    val idCategoria: String?,

    @Json(name = "categoria")
    val categoria: String?
)
