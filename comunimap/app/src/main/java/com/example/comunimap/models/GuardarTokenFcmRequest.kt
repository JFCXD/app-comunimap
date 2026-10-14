package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO DE PETICIÓN: GUARDAR TOKEN FCM
// =====================================================

/**
 * Payload JSON enviado al endpoint guardar_token_fcm.php
 * para asociar el token del dispositivo con el usuario en sesión.
 */
@JsonClass(generateAdapter = true)
data class GuardarTokenFcmRequest(
    @Json(name = "idUsuario")
    val idUsuario: String,

    @Json(name = "token")
    val token: String
)
