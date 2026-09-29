package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELOS PARA RESPUESTAS Y PETICIONES A LA API
// =====================================================

// Estructura estándar devuelta por los endpoints de mutación en la API PHP
@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "message")
    val message: String? = null
)

// Petición para crear usuario: POST crear_usuario.php
@JsonClass(generateAdapter = true)
data class CrearUsuarioRequest(
    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "telefono")
    val telefono: String,

    @Json(name = "rol")
    val rol: String
)

// Petición para actualizar usuario: POST actualizar_usuario.php
@JsonClass(generateAdapter = true)
data class ActualizarUsuarioRequest(
    @Json(name = "idUsuario")
    val idUsuario: String,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "telefono")
    val telefono: String,

    @Json(name = "rol")
    val rol: String,

    @Json(name = "estado")
    val estado: String
)

// Petición para cambiar estado de usuario: POST cambiar_estado_usuario.php
@JsonClass(generateAdapter = true)
data class CambiarEstadoUsuarioRequest(
    @Json(name = "idUsuario")
    val idUsuario: String,

    @Json(name = "estado")
    val estado: String
)

// Petición para crear reporte: POST crear_reporte.php
@JsonClass(generateAdapter = true)
data class CrearReporteRequest(
    @Json(name = "titulo")
    val titulo: String,

    @Json(name = "descripcion")
    val descripcion: String,

    @Json(name = "ubicacion")
    val ubicacion: String,

    @Json(name = "direccion")
    val direccion: String,

    @Json(name = "idUsuario")
    val idUsuario: String,

    @Json(name = "idCategoria")
    val idCategoria: String,

    @Json(name = "imagen")
    val imagen: String? = null
)

// Petición para actualizar reporte: POST actualizar_reporte.php
@JsonClass(generateAdapter = true)
data class ActualizarReporteRequest(
    @Json(name = "idReporte")
    val idReporte: String,

    @Json(name = "titulo")
    val titulo: String,

    @Json(name = "descripcion")
    val descripcion: String,

    @Json(name = "ubicacion")
    val ubicacion: String,

    @Json(name = "direccion")
    val direccion: String,

    @Json(name = "idCategoria")
    val idCategoria: String,

    @Json(name = "estado")
    val estado: String
)

// Petición para cambiar estado de reporte: POST actualizar_estado.php
@JsonClass(generateAdapter = true)
data class ActualizarEstadoReporteRequest(
    @Json(name = "idReporte")
    val idReporte: String,

    @Json(name = "estado")
    val estado: String
)
