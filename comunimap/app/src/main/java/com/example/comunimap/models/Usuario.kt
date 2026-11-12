package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO DE DATOS: USUARIO
// =====================================================

// Representa exactamente los datos de un usuario devueltos por el endpoint usuarios.php desde MySQL.
@JsonClass(generateAdapter = true)
data class Usuario(
    @Json(name = "idUsuario")
    val idUsuario: String?,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "apellido")
    val apellido: String? = null,

    @Json(name = "fechaNacimiento")
    val fechaNacimiento: String? = null,

    @Json(name = "email")
    val email: String,

    @Json(name = "telefono")
    val telefono: String?,

    @Json(name = "rol")
    val rol: String,

    @Json(name = "estado")
    val estado: String
)
