package com.example

import com.example.comunimap.utils.FechaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class FechaUtilsTest {

    @Test
    fun calcularEdad_cumpleanosYaPasoEsteAnio() {
        val hoy = Calendar.getInstance()
        val anioActual = hoy.get(Calendar.YEAR)

        // Nacido el 1 de enero de hace 25 años (el cumpleaños ya ocurrió este año con certeza si hoy >= 1 de enero)
        val fecha = String.format(java.util.Locale.US, "%04d-01-01", anioActual - 25)
        val edad = FechaUtils.calcularEdad(fecha)

        assertNotNull(edad)
        // Si hoy es enero 1 o posterior, la edad debe ser 25
        assertEquals(25, edad)
    }

    @Test
    fun calcularEdad_cumpleanosAunNoPasaEsteAnio() {
        val hoy = Calendar.getInstance()
        val anioActual = hoy.get(Calendar.YEAR)

        // Nacido el 31 de diciembre de hace 20 años (si hoy no es 31 de diciembre, la edad cumplida es 19)
        // A menos que hoy sea exactamente 31 de diciembre
        val mesActual = hoy.get(Calendar.MONTH) + 1
        val diaActual = hoy.get(Calendar.DAY_OF_MONTH)

        if (mesActual < 12 || diaActual < 31) {
            val fecha = String.format(java.util.Locale.US, "%04d-12-31", anioActual - 20)
            val edad = FechaUtils.calcularEdad(fecha)
            assertNotNull(edad)
            assertEquals(19, edad)
        }
    }

    @Test
    fun calcularEdad_hoyEsCumpleanos() {
        val hoy = Calendar.getInstance()
        val anioActual = hoy.get(Calendar.YEAR)
        val mesActual = hoy.get(Calendar.MONTH) + 1
        val diaActual = hoy.get(Calendar.DAY_OF_MONTH)

        val fecha = String.format(java.util.Locale.US, "%04d-%02d-%02d", anioActual - 30, mesActual, diaActual)
        val edad = FechaUtils.calcularEdad(fecha)

        assertNotNull(edad)
        assertEquals(30, edad)
    }

    @Test
    fun calcularEdad_fechaInvalidaONula() {
        assertNull(FechaUtils.calcularEdad(null))
        assertNull(FechaUtils.calcularEdad(""))
        assertNull(FechaUtils.calcularEdad("   "))
        assertNull(FechaUtils.calcularEdad("2020/05/10"))
        assertNull(FechaUtils.calcularEdad("abc"))
    }

    @Test
    fun formatearFechaVisual() {
        assertEquals("15/08/1995", FechaUtils.formatearFechaVisual("1995-08-15"))
        assertEquals("01/01/2000", FechaUtils.formatearFechaVisual("2000-01-01 12:00:00"))
        assertNull(FechaUtils.formatearFechaVisual(null))
        assertNull(FechaUtils.formatearFechaVisual(""))
    }
}
