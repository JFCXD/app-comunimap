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

    // Token Administrativo para operaciones de sesión segura
    private const val KEY_TOKEN_API = "pref_token_api"

    fun obtenerTokenApi(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN_API, null)
    }

    fun guardarTokenApi(context: Context, token: String?) {
        if (!token.isNullOrBlank()) {
            getPrefs(context).edit().putString(KEY_TOKEN_API, token).apply()
        } else {
            getPrefs(context).edit().remove(KEY_TOKEN_API).apply()
        }
    }

    fun eliminarTokenApi(context: Context) {
        getPrefs(context).edit().remove(KEY_TOKEN_API).apply()
    }

    // Identificación del usuario en sesión activa
    private const val KEY_USUARIO_ID = "pref_usuario_id"
    private const val KEY_USUARIO_ROL = "pref_usuario_rol"
    private const val KEY_FCM_TOKEN = "pref_fcm_token"

    fun obtenerIdUsuario(context: Context): String? {
        return getPrefs(context).getString(KEY_USUARIO_ID, null)
    }

    fun obtenerRolUsuario(context: Context): String? {
        return getPrefs(context).getString(KEY_USUARIO_ROL, null)
    }

    fun guardarSesionUsuario(context: Context, idUsuario: String?, rol: String?) {
        getPrefs(context).edit().apply {
            if (!idUsuario.isNullOrBlank()) putString(KEY_USUARIO_ID, idUsuario) else remove(KEY_USUARIO_ID)
            if (!rol.isNullOrBlank()) putString(KEY_USUARIO_ROL, rol) else remove(KEY_USUARIO_ROL)
        }.apply()
    }

    fun eliminarSesionUsuario(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_USUARIO_ID)
            remove(KEY_USUARIO_ROL)
            remove(KEY_TOKEN_API)
        }.apply()
    }

    fun obtenerTokenFcm(context: Context): String? {
        return getPrefs(context).getString(KEY_FCM_TOKEN, null)
    }

    fun guardarTokenFcm(context: Context, token: String?) {
        if (!token.isNullOrBlank()) {
            getPrefs(context).edit().putString(KEY_FCM_TOKEN, token).apply()
        } else {
            getPrefs(context).edit().remove(KEY_FCM_TOKEN).apply()
        }
    }
}
