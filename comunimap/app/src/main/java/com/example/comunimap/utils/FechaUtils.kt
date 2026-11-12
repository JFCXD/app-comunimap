package com.example.comunimap.utils

import java.util.Calendar
import java.util.Locale

// =====================================================
// UTILIDADES PARA FECHA DE NACIMIENTO Y CÁLCULO DE EDAD
// =====================================================

object FechaUtils {

    /**
     * Calcula la edad cumplida en años a partir de una fecha de nacimiento en formato "YYYY-MM-DD".
     *
     * Compara el año, mes y día de nacimiento con la fecha actual del sistema.
     * Si la fecha no es válida o es futura, retorna null.
     */
    fun calcularEdad(fechaNacimientoStr: String?): Int? {
        if (fechaNacimientoStr.isNullOrBlank()) return null
        val fechaLimpia = fechaNacimientoStr.trim().split(" ")[0]
        val partes = fechaLimpia.split("-")
        if (partes.size != 3) return null

        val anio = partes[0].toIntOrNull() ?: return null
        val mes = partes[1].toIntOrNull() ?: return null // 1-12
        val dia = partes[2].toIntOrNull() ?: return null

        if (mes !in 1..12 || dia !in 1..31) return null

        val hoy = Calendar.getInstance()
        val anioActual = hoy.get(Calendar.YEAR)
        val mesActual = hoy.get(Calendar.MONTH) + 1
        val diaActual = hoy.get(Calendar.DAY_OF_MONTH)

        var edad = anioActual - anio
        if (mesActual < mes || (mesActual == mes && diaActual < dia)) {
            edad--
        }

        return if (edad >= 0) edad else null
    }

    /**
     * Convierte una fecha ISO "YYYY-MM-DD" a formato visual "DD/MM/YYYY".
     * Si no coincide con el formato esperado, devuelve el texto original si no está vacío.
     */
    fun formatearFechaVisual(fechaIsoStr: String?): String? {
        if (fechaIsoStr.isNullOrBlank()) return null
        val fechaLimpia = fechaIsoStr.trim().split(" ")[0]
        val partes = fechaLimpia.split("-")
        if (partes.size != 3) return fechaIsoStr
        val anio = partes[0]
        val mes = partes[1]
        val dia = partes[2]
        return "$dia/$mes/$anio"
    }

    /**
     * Genera la cadena en formato ISO "YYYY-MM-DD" para envío al backend.
     */
    fun formatearFechaIso(anio: Int, mes: Int, dia: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", anio, mes, dia)
    }

    /**
     * Genera la cadena en formato visual "DD/MM/YYYY".
     */
    fun formatearFechaVisual(anio: Int, mes: Int, dia: Int): String {
        return String.format(Locale.US, "%02d/%02d/%04d", dia, mes, anio)
    }
}
