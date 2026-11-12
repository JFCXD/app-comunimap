package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO: NOTIFICACIÓN INTERNA
// =====================================================

@JsonClass(generateAdapter = true)
data class Notificacion(
    @Json(name = "idNotificacion")
    val idNotificacion: Int,

    @Json(name = "idUsuario")
    val idUsuario: Int,

    @Json(name = "idReporte")
    val idReporte: Int?,

    @Json(name = "idPublicacion")
    val idPublicacion: Any? = null,

    @Json(name = "titulo")
    val titulo: String,

    @Json(name = "mensaje")
    val mensaje: String,

    @Json(name = "tipo")
    val tipo: String,

    @Json(name = "leida")
    val leida: Int,

    @Json(name = "fecha")
    val fecha: String
) {
    val idPublicacionInt: Int?
        get() = when (idPublicacion) {
            is Number -> idPublicacion.toInt()
            is String -> idPublicacion.toIntOrNull()
            else -> null
        }
}
