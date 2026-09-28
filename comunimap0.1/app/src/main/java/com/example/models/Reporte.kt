package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ====================================================================================================
// 📋 MODELO: REPORTE / ALERTA CIUDADANA (Tabla `reportes` en MySQL `gestion_alertas`)
// ====================================================================================================
// Estructura real devuelta por `reportes.php`:
// {
//     "idReporte": "8",
//     "titulo": "basura",
//     "descripcion": "hay mucha basura",
//     "ubicacion": "-17.4518,-66.1287",
//     "direccion": "zona sud",
//     "estado": "En Proceso",
//     "imagen": "imagenes/archivo.jpg",
//     "idUsuario": "4",
//     "usuario": "fernando",
//     "idCategoria": "1",
//     "categoria": "Basura"
// }
// ====================================================================================================

@JsonClass(generateAdapter = true)
data class Reporte(
    @Json(name = "idReporte")
    val idReporte: Int = 0,

    @Json(name = "titulo")
    val titulo: String,

    @Json(name = "descripcion")
    val descripcion: String,

    @Json(name = "ubicacion")
    val ubicacion: String = "-17.3935,-66.1570",

    @Json(name = "direccion")
    val direccion: String? = "Ubicación en mapa",

    @Json(name = "estado")
    val estado: String = "Pendiente", // "Pendiente", "En Proceso", "Solucionado"

    @Json(name = "imagen")
    val imagen: String? = null,

    @Json(name = "idUsuario")
    val idUsuario: Int = 1,

    @Json(name = "usuario")
    val usuario: String? = "Usuario",

    @Json(name = "idCategoria")
    val idCategoria: Int = 1,

    @Json(name = "categoria")
    val categoria: String? = "General"
) {
    // ------------------------------------------------------------------------------------------------
    // 📍 GETTERS CALCULADOS: Coordenadas GPS a partir del campo `ubicacion` ("lat,lng")
    // ------------------------------------------------------------------------------------------------
    val latitud: Double
        get() = ubicacion.split(",").getOrNull(0)?.trim()?.toDoubleOrNull() ?: -17.3935

    val longitud: Double
        get() = ubicacion.split(",").getOrNull(1)?.trim()?.toDoubleOrNull() ?: -66.1570

    // Propiedades de compatibilidad para pantallas
    val id: Int
        get() = idReporte

    val categoriaId: Int
        get() = idCategoria

    val usuarioId: Int
        get() = idUsuario

    val usuarioNombre: String?
        get() = usuario

    val categoriaNombre: String?
        get() = categoria

    val fotoUrl: String?
        get() = imagen

    val prioridad: String
        get() = when (estado.lowercase().trim()) {
            "pendiente" -> "Alta"
            "en proceso" -> "Media"
            "solucionado" -> "Baja"
            else -> "Media"
        }

    val creadoEn: String
        get() = "Registrado"
}

// ----------------------------------------------------------------------------------------------------
// 📤 DTO: PETICIÓN PARA CREAR UN REPORTE (POST /crear_reporte.php)
// ----------------------------------------------------------------------------------------------------
@JsonClass(generateAdapter = true)
data class CrearReporteRequest(
    @Json(name = "titulo")
    val titulo: String,

    @Json(name = "descripcion")
    val descripcion: String,

    @Json(name = "ubicacion")
    val ubicacion: String, // Formato "lat,lng" ej: "-17.3935,-66.1570"

    @Json(name = "direccion")
    val direccion: String? = null,

    @Json(name = "idUsuario")
    val idUsuario: Int,

    @Json(name = "idCategoria")
    val idCategoria: Int
)

// ----------------------------------------------------------------------------------------------------
// 📥 DTO: RESPUESTA DE CREAR REPORTE (crear_reporte.php)
// ----------------------------------------------------------------------------------------------------
@JsonClass(generateAdapter = true)
data class CrearReporteResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "mensaje")
    val mensaje: String,

    @Json(name = "idReporte")
    val idReporte: Int? = null
)

