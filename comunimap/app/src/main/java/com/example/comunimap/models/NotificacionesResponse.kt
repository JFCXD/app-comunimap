package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO: RESPUESTA DE CONSULTA DE NOTIFICACIONES
// =====================================================

@JsonClass(generateAdapter = true)
data class NotificacionesResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "total")
    val total: Int,

    @Json(name = "notificaciones")
    val notificaciones: List<Notificacion>
)
