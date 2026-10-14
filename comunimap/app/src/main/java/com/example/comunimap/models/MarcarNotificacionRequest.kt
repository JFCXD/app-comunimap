package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO: SOLICITUD PARA MARCAR NOTIFICACIÓN COMO LEÍDA
// =====================================================

@JsonClass(generateAdapter = true)
data class MarcarNotificacionRequest(
    @Json(name = "idNotificacion")
    val idNotificacion: String
)
