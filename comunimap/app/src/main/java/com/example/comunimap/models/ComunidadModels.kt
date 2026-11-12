package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELOS PARA EL MÓDULO COMUNIDAD
// =====================================================

/**
 * Respuesta devuelta por el endpoint comunidad.php (paginación y publicaciones)
 */
@JsonClass(generateAdapter = true)
data class ComunidadResponse(
    @Json(name = "success")
    val success: Boolean = false,

    @Json(name = "page")
    val page: Int = 1,

    @Json(name = "hasMore")
    val hasMore: Boolean = false,

    @Json(name = "totalPagina")
    val totalPagina: Int = 0,

    @Json(name = "publicaciones")
    val publicaciones: List<PublicacionComunidad> = emptyList(),

    @Json(name = "message")
    val message: String? = null
)

/**
 * Representa una publicación comunitaria: Comunicado, Actividad o Encuesta.
 */
@JsonClass(generateAdapter = true)
data class PublicacionComunidad(
    @Json(name = "idPublicacion")
    val idPublicacion: Any? = null,

    @Json(name = "idAutor")
    val idAutor: Any? = null,

    @Json(name = "autor")
    val autor: String? = null,

    @Json(name = "tipo")
    val tipo: String = "Comunicado", // "Comunicado", "Actividad", "Encuesta"

    @Json(name = "titulo")
    val titulo: String = "",

    @Json(name = "descripcion")
    val descripcion: String = "",

    @Json(name = "estado")
    val estado: String? = "Publicado", // "Borrador", "Publicado", "Archivado"

    @Json(name = "imagen")
    val imagen: String? = null,

    @Json(name = "imagenUrl")
    val imagenUrl: String? = null,

    @Json(name = "fechaCreacion")
    val fechaCreacion: String? = null,

    // Campos de Actividad
    @Json(name = "fechaActividad")
    val fechaActividad: String? = null,

    @Json(name = "horaActividad")
    val horaActividad: String? = null,

    @Json(name = "lugarActividad")
    val lugarActividad: String? = null,

    // Campos de Encuesta
    @Json(name = "fechaCierre")
    val fechaCierre: String? = null,

    @Json(name = "opciones")
    val opciones: List<OpcionEncuesta>? = null,

    @Json(name = "totalVotos")
    val totalVotos: Int? = 0,

    @Json(name = "miVoto")
    val miVoto: Any? = null,

    @Json(name = "encuestaAbierta")
    val encuestaAbierta: Boolean? = null
) {
    val idPublicacionInt: Int?
        get() = when (idPublicacion) {
            is Number -> idPublicacion.toInt()
            is String -> idPublicacion.toIntOrNull()
            else -> null
        }

    val miVotoInt: Int?
        get() = when (miVoto) {
            is Number -> miVoto.toInt()
            is String -> miVoto.toIntOrNull()
            else -> null
        }
}

/**
 * Representa una opción de respuesta en una encuesta comunitaria.
 */
@JsonClass(generateAdapter = true)
data class OpcionEncuesta(
    @Json(name = "idOpcion")
    val idOpcion: Any? = null,

    @Json(name = "texto")
    val texto: String = "",

    @Json(name = "orden")
    val orden: Int? = 0,

    @Json(name = "totalVotos")
    val totalVotos: Int? = 0
) {
    val idOpcionInt: Int?
        get() = when (idOpcion) {
            is Number -> idOpcion.toInt()
            is String -> idOpcion.toIntOrNull()
            else -> null
        }
}

/**
 * Petición para registrar un voto en votar_comunidad.php
 */
@JsonClass(generateAdapter = true)
data class VotarComunidadRequest(
    @Json(name = "idPublicacion")
    val idPublicacion: Int,

    @Json(name = "idOpcion")
    val idOpcion: Int
)

/**
 * Petición administrativa para crear, editar o cambiar estado de una publicación
 */
@JsonClass(generateAdapter = true)
data class ComunidadAdminRequest(
    @Json(name = "accion")
    val accion: String, // "crear", "editar", "estado"

    @Json(name = "idPublicacion")
    val idPublicacion: Int? = null,

    @Json(name = "tipo")
    val tipo: String? = null, // "Comunicado", "Actividad", "Encuesta"

    @Json(name = "titulo")
    val titulo: String? = null,

    @Json(name = "descripcion")
    val descripcion: String? = null,

    @Json(name = "estado")
    val estado: String? = null, // "Borrador", "Publicado", "Archivado"

    @Json(name = "fechaActividad")
    val fechaActividad: String? = null,

    @Json(name = "horaActividad")
    val horaActividad: String? = null,

    @Json(name = "lugarActividad")
    val lugarActividad: String? = null,

    @Json(name = "fechaCierre")
    val fechaCierre: String? = null,

    @Json(name = "opciones")
    val opciones: List<String>? = null
)

/**
 * Respuesta devuelta por comunidad_admin.php (crear, editar, cambiar estado)
 */
@JsonClass(generateAdapter = true)
data class ComunidadAdminResponse(
    @Json(name = "success")
    val success: Boolean = false,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "idPublicacion")
    val idPublicacion: Any? = null
) {
    val idPublicacionInt: Int?
        get() = when (idPublicacion) {
            is Number -> idPublicacion.toInt()
            is String -> idPublicacion.toIntOrNull()
            else -> null
        }
}

/**
 * Respuesta devuelta por comunidad_imagen.php (subir imagen multipart)
 */
@JsonClass(generateAdapter = true)
data class ComunidadSubirImagenResponse(
    @Json(name = "success")
    val success: Boolean = false,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "imagen")
    val imagen: String? = null,

    @Json(name = "imagenUrl")
    val imagenUrl: String? = null,

    @Json(name = "idPublicacion")
    val idPublicacion: Any? = null
) {
    val idPublicacionInt: Int?
        get() = when (idPublicacion) {
            is Number -> idPublicacion.toInt()
            is String -> idPublicacion.toIntOrNull()
            else -> null
        }
}
