package com.example.comunimap.utils

import android.content.Context
import android.content.SharedPreferences

// =====================================================
// GESTOR DE PREFERENCIAS LOCALES (SharedPreferences)
// =====================================================

enum class ModoTema(val clave: String, val titulo: String, val descripcion: String) {
    CLARO("claro", "Claro", "Aspecto visual claro"),
    OSCURO("oscuro", "Oscuro", "Estilo navy oscuro clásico"),
    SISTEMA("sistema", "Seguir configuración del sistema", "Se adapta según el modo claro u oscuro del dispositivo")
}

object PreferenciasManager {
    private const val PREFS_NAME = "comunimap_config_prefs"
    private const val KEY_MODO_TEMA = "pref_modo_tema"
    private const val KEY_NOTIFICACIONES_ALERTAS = "pref_notificaciones_alertas"
    private const val KEY_SONIDO_EMERGENCIA = "pref_sonido_emergencia"
    private const val KEY_VIBRACION_ALERTAS = "pref_vibracion_alertas"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Modo de Tema
    fun obtenerModoTema(context: Context): ModoTema {
        val clave = getPrefs(context).getString(KEY_MODO_TEMA, ModoTema.SISTEMA.clave)
        return ModoTema.values().firstOrNull { it.clave == clave } ?: ModoTema.SISTEMA
    }

    fun guardarModoTema(context: Context, modo: ModoTema) {
        getPrefs(context).edit().putString(KEY_MODO_TEMA, modo.clave).apply()
    }

    // Notificaciones de alertas
    fun obtenerNotificacionesAlertas(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICACIONES_ALERTAS, true)
    }

    fun guardarNotificacionesAlertas(context: Context, habilitado: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICACIONES_ALERTAS, habilitado).apply()
    }

    // Sonido de emergencia
    fun obtenerSonidoEmergencia(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SONIDO_EMERGENCIA, true)
    }

    fun guardarSonidoEmergencia(context: Context, habilitado: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SONIDO_EMERGENCIA, habilitado).apply()
    }

    // Vibración
    fun obtenerVibracionAlertas(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIBRACION_ALERTAS, true)
    }

    fun guardarVibracionAlertas(context: Context, habilitado: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VIBRACION_ALERTAS, habilitado).apply()
    }
}
