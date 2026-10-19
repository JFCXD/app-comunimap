package com.example.comunimap.utils

// =====================================================
// CONSTANTES DE LA APLICACIÓN
// =====================================================

// Esta URL corresponde al servidor de producción publicado en Railway.
// Define la dirección base para todas las peticiones a la API de ComuniMap.
const val BASE_URL = "https://comunimap-api-production.up.railway.app/"

// URL base para archivos públicos y evidencia fotográfica servida desde Laravel en Railway
const val BASE_URL_STORAGE = "https://comunimap-production.up.railway.app/storage/"

/**
 * Construye la URL pública absoluta para la imagen de un reporte.
 * Si la ruta ya es una URL completa (http/https), se respeta tal cual.
 * Si es una ruta relativa (ej. "reportes/archivo.jpg"), se concatena con BASE_URL_STORAGE.
 * Si la ruta es nula o vacía, retorna null.
 */
fun obtenerUrlImagenReporte(ruta: String?): String? {
    if (ruta.isNullOrBlank()) return null
    val rutaLimpia = ruta.trim()
    return if (rutaLimpia.startsWith("http://", ignoreCase = true) || rutaLimpia.startsWith("https://", ignoreCase = true)) {
        rutaLimpia
    } else {
        BASE_URL_STORAGE + rutaLimpia.removePrefix("/")
    }
}
