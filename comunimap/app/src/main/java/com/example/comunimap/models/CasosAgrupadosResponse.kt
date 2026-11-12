package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// MODELO DE DATOS: CASOS AGRUPADOS POR PRIORIDAD
// =====================================================

/**
 * Respuesta completa del endpoint casos_agrupados.php de la API en Railway.
 */
@JsonClass(generateAdapter = true)
data class CasosAgrupadosResponse(
    @Json(name = "success")
    val success: Boolean = false,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "total")
    val total: Int = 0,

    @Json(name = "totales")
    val totales: TotalesCasosAgrupados? = null,

    @Json(name = "casos")
    val casos: CasosAgrupadosContainer? = null
)

@JsonClass(generateAdapter = true)
data class TotalesCasosAgrupados(
    @Json(name = "alta")
    val alta: Int = 0,

    @Json(name = "media")
    val media: Int = 0,

    @Json(name = "baja")
    val baja: Int = 0
)

@JsonClass(generateAdapter = true)
data class CasosAgrupadosContainer(
    @Json(name = "alta")
    val alta: List<CasoAgrupado> = emptyList(),

    @Json(name = "media")
    val media: List<CasoAgrupado> = emptyList(),

    @Json(name = "baja")
    val baja: List<CasoAgrupado> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CasoAgrupado(
    @Json(name = "idReporte")
    val idReporte: Any? = null,

    @Json(name = "titulo")
    val titulo: String = "",

    @Json(name = "categoria")
    val categoria: String = "",

    @Json(name = "idCategoria")
    val idCategoria: Any? = null,

    @Json(name = "direccion")
    val direccion: String? = null,

    @Json(name = "cantidadVecinos")
    val cantidadVecinos: Int = 0,

    @Json(name = "cantidadReportes")
    val cantidadReportes: Int = 0,

    @Json(name = "prioridad")
    val prioridad: String = ""
) {
    val idReporteFormateado: String
        get() = when (idReporte) {
            is Number -> idReporte.toLong().toString()
            else -> idReporte?.toString() ?: ""
        }

    val idReporteInt: Int?
        get() = when (idReporte) {
            is Number -> idReporte.toInt()
            else -> idReporte?.toString()?.toIntOrNull()
        }
}
