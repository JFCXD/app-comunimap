package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ====================================================================================================
// 🏷️ MODELO: CATEGORÍA DE ALERTA (Tabla `categorias` en MySQL `gestion_alertas`)
// ====================================================================================================
// Estructura en la base de datos MySQL y respuesta de `categorias.php`:
// - idCategoria: INT AUTO_INCREMENT PRIMARY KEY
// - nombre: VARCHAR(50) NOT NULL
// - descripcion: TEXT (opcional)
// ====================================================================================================

@JsonClass(generateAdapter = true)
data class Categoria(
    @Json(name = "idCategoria")
    val idCategoria: Int = 0,

    @Json(name = "nombre")
    val nombre: String,

    @Json(name = "descripcion")
    val descripcion: String? = null
) {
    // Propiedad de compatibilidad con pantallas anteriores
    val id: Int
        get() = idCategoria

    // Propiedades visuales auxiliares para interfaz (calculadas según nombre)
    val icono: String
        get() = when {
            nombre.contains("bache", ignoreCase = true) || nombre.contains("vía", ignoreCase = true) -> "car"
            nombre.contains("luz", ignoreCase = true) || nombre.contains("alumbrado", ignoreCase = true) -> "light"
            nombre.contains("agua", ignoreCase = true) || nombre.contains("fuga", ignoreCase = true) -> "water"
            nombre.contains("basura", ignoreCase = true) || nombre.contains("residuo", ignoreCase = true) -> "trash"
            nombre.contains("seguridad", ignoreCase = true) -> "shield"
            nombre.contains("parque", ignoreCase = true) || nombre.contains("árbol", ignoreCase = true) -> "park"
            else -> "alerta"
        }

    val colorHex: String
        get() = when {
            nombre.contains("bache", ignoreCase = true) || nombre.contains("vía", ignoreCase = true) -> "#EA580C"
            nombre.contains("luz", ignoreCase = true) || nombre.contains("alumbrado", ignoreCase = true) -> "#F59E0B"
            nombre.contains("agua", ignoreCase = true) || nombre.contains("fuga", ignoreCase = true) -> "#0284C7"
            nombre.contains("basura", ignoreCase = true) || nombre.contains("residuo", ignoreCase = true) -> "#16A34A"
            nombre.contains("seguridad", ignoreCase = true) -> "#DC2626"
            nombre.contains("parque", ignoreCase = true) || nombre.contains("árbol", ignoreCase = true) -> "#059669"
            else -> "#7C3AED"
        }
}

