package com.example.utils

import androidx.compose.ui.graphics.Color
import com.example.models.Categoria
import com.example.models.Reporte
import com.example.models.Usuario

// ====================================================================================================
// ⚙️ UTILIDADES Y CONSTANTES GLOBALES (ComuniMap)
// ====================================================================================================
// En este archivo se centralizan los valores predeterminados del sistema:
// 1. URL de conexión a la API Backend / Base de Datos MySQL `gestion_alertas`.
// 2. Coordenadas iniciales de referencia de Cochabamba.
// 3. Colores temáticos de estados reales: Pendiente (rojo), En Proceso (amarillo), Solucionado (verde).
// ====================================================================================================

object Constantes {

    // ------------------------------------------------------------------------------------------------
    // 🌐 CONEXIÓN AL SERVIDOR / BACKEND REAL (PHP / MySQL)
    // ------------------------------------------------------------------------------------------------
    // • http://192.168.1.7/api_gestion/ : URL de la API en el servidor local XAMPP
    // • Base de datos: gestion_alertas
    const val DEFAULT_BASE_URL = "https://unimap.liveblog365.com/gestion/api_gestion/"

    // ------------------------------------------------------------------------------------------------
    // 📍 COORDENADAS GPS PREDETERMINADAS DEL MAPA
    // Centrado en Cochabamba, Bolivia: Latitud: -17.3935, Longitud: -66.1570
    // ------------------------------------------------------------------------------------------------
    const val DEFAULT_MAP_LATITUDE = -17.3935
    const val DEFAULT_MAP_LONGITUDE = -66.1570

    // Listas vacías por defecto: Los datos se consumen exclusivamente desde la API PHP real
    val CATEGORIAS_DEFECTO = emptyList<Categoria>()
    val USUARIOS_DEFECTO = emptyList<Usuario>()
    val REPORTES_INICIALES = emptyList<Reporte>()

    // ------------------------------------------------------------------------------------------------
    // 🎨 ASIGNACIÓN DE COLORES POR ESTADO REAL DEL REPORTE
    // Estados válidos: "Pendiente" (Rojo), "En Proceso" (Amarillo/Dorado), "Solucionado" (Verde)
    // ------------------------------------------------------------------------------------------------
    fun obtenerColorEstado(estado: String): Color {
        return when (estado.lowercase().trim()) {
            "pendiente" -> Color(0xFFDC2626)   // Rojo de alerta / pendiente
            "en proceso" -> Color(0xFFF59E0B)  // Amarillo / dorado en atención
            "solucionado" -> Color(0xFF16A34A) // Verde solucionado
            "resuelto" -> Color(0xFF16A34A)    // Compatibilidad secundaria
            else -> Color(0xFF6B7280)          // Gris neutral
        }
    }

    // ------------------------------------------------------------------------------------------------
    // 🚨 ASIGNACIÓN DE COLORES POR NIVEL DE PRIORIDAD
    // ------------------------------------------------------------------------------------------------
    fun obtenerColorPrioridad(prioridad: String): Color {
        return when (prioridad.lowercase().trim()) {
            "urgente" -> Color(0xFFDC2626) // Rojo emergencia
            "alta" -> Color(0xFFEA580C)    // Naranja
            "media" -> Color(0xFFD97706)   // Ámbar
            "baja" -> Color(0xFF059669)    // Verde suave
            else -> Color(0xFF6B7280)
        }
    }
}

