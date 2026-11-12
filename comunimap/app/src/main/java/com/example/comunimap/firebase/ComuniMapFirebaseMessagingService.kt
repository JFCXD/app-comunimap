package com.example.comunimap.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.comunimap.MainActivity
import com.example.R
import com.example.comunimap.api.ApiClient
import com.example.comunimap.models.GuardarTokenFcmRequest
import com.example.comunimap.utils.PreferenciasManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio para recepción de Firebase Cloud Messaging (FCM) en ComuniMap.
 * Gestiona notificaciones de reportes y nuevas publicaciones comunitarias (COMUNIDAD).
 */
class ComuniMapFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CANAL_ID = "comunimap_general"
        const val CANAL_NOMBRE = "ComuniMap"
        const val CANAL_DESCRIPCION = "Notificaciones de ComuniMap"

        fun crearCanalNotificaciones(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val canalExistente = notificationManager.getNotificationChannel(CANAL_ID)
                if (canalExistente == null) {
                    val canal = NotificationChannel(
                        CANAL_ID,
                        CANAL_NOMBRE,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = CANAL_DESCRIPCION
                        enableVibration(true)
                    }
                    notificationManager.createNotificationChannel(canal)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificaciones(this)
    }

    /**
     * Cuando Firebase renueva el token FCM, se persiste localmente y, si existe
     * una sesión de usuario activa, se envía a guardar_token_fcm.php.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PreferenciasManager.guardarTokenFcm(this, token)

        val idUsuario = PreferenciasManager.obtenerIdUsuario(this)
        if (!idUsuario.isNullOrBlank()) {
            Log.d("FCM_TOKEN", "Token renovado para usuario activo $idUsuario, asociando con el servidor...")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = ApiClient.apiService.guardarTokenFcm(
                        GuardarTokenFcmRequest(idUsuario = idUsuario, token = token)
                    )
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        Log.d("FCM_TOKEN", "Token FCM actualizado en el servidor para usuario $idUsuario")
                    } else {
                        Log.w("FCM_TOKEN", "Respuesta no exitosa al actualizar token FCM: HTTP ${resp.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("FCM_TOKEN", "Error de red al actualizar token FCM renovado: ${e.message}")
                }
            }
        } else {
            Log.d("FCM_TOKEN", "Token FCM recibido sin sesión de usuario activa; se asociará en el próximo login.")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val tipo = remoteMessage.data["tipo"]?.trim()?.uppercase()
        val idPublicacionStr = remoteMessage.data["idPublicacion"]?.trim()
        val idPublicacion = idPublicacionStr?.toIntOrNull()
        val idNotificacionStr = remoteMessage.data["idNotificacion"]?.trim()
        val idNotificacion = idNotificacionStr?.toIntOrNull()
        val idReporteStr = remoteMessage.data["idReporte"]?.trim()
        val idReporte = idReporteStr?.toIntOrNull()

        // REGLA: Los avisos de COMUNIDAD son exclusivos para vecinos (Usuario). Los administradores no los reciben.
        if (tipo == "COMUNIDAD") {
            val rolActual = PreferenciasManager.obtenerRolUsuario(this)
            val esAdmin = rolActual?.trim()?.equals("Administrador", ignoreCase = true) == true
            if (esAdmin) {
                Log.d("FCM_COMUNIDAD", "Aviso de comunidad omitido: el usuario activo es Administrador.")
                return
            }

            if (idPublicacion == null || idPublicacion <= 0) {
                Log.w("FCM_COMUNIDAD", "Aviso de comunidad descartado por idPublicacion no válido: $idPublicacionStr")
                return
            }
        }

        // Obtener título y cuerpo priorizando los datos del backend
        val dataTitle = remoteMessage.data["titulo"] ?: remoteMessage.data["title"]
        val dataBody = remoteMessage.data["mensaje"] ?: remoteMessage.data["body"]
        val notifTitle = remoteMessage.notification?.title
        val notifBody = remoteMessage.notification?.body

        val title = (dataTitle ?: notifTitle)?.takeIf { it.isNotBlank() } ?: "ComuniMap"
        val body = (dataBody ?: notifBody)?.takeIf { it.isNotBlank() } ?: "Tienes una nueva notificación"

        mostrarNotificacion(
            titulo = title,
            mensaje = body,
            tipo = tipo,
            idPublicacion = idPublicacion,
            idNotificacion = idNotificacion,
            idReporte = idReporte
        )
    }

    private fun mostrarNotificacion(
        titulo: String,
        mensaje: String,
        tipo: String? = null,
        idPublicacion: Int? = null,
        idNotificacion: Int? = null,
        idReporte: Int? = null
    ) {
        crearCanalNotificaciones(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!tipo.isNullOrBlank()) putExtra("tipo", tipo)
            if (idPublicacion != null && idPublicacion > 0) putExtra("idPublicacion", idPublicacion)
            if (idNotificacion != null && idNotificacion > 0) putExtra("idNotificacion", idNotificacion)
            if (idReporte != null && idReporte > 0) putExtra("idReporte", idReporte)
        }

        // Notification ID único para evitar que distintos PendingIntent sobrescriban sus datos
        val notificationId = idNotificacion ?: ((System.currentTimeMillis() and 0x7FFFFFFF).toInt())

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
