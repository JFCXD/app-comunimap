package com.example.comunimap.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// =====================================================
// RESPUESTA DE LA API: REPORTES
// =====================================================

// Representa la respuesta completa del endpoint reportes.php.
@JsonClass(generateAdapter = true)
data class ReportesResponse(
    @Json(name = "success")
    val success: Boolean,

    @Json(name = "total")
    val total: Int,

    @Json(name = "reportes")
    val reportes: List<Reporte>
)
