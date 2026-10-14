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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio para recepción básica de Firebase Cloud Messaging (FCM).
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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Prioridad: si data tiene title/body usar esos valores, si no usar notification.title/body.
        val dataTitle = remoteMessage.data["title"]?.takeIf { it.isNotBlank() }
        val dataBody = remoteMessage.data["body"]?.takeIf { it.isNotBlank() }

        val notifTitle = remoteMessage.notification?.title?.takeIf { it.isNotBlank() }
        val notifBody = remoteMessage.notification?.body?.takeIf { it.isNotBlank() }

        val title = dataTitle ?: notifTitle ?: "ComuniMap"
        val body = dataBody ?: notifBody ?: "Tienes una nueva notificación"

        mostrarNotificacion(title, body)
    }

    private fun mostrarNotificacion(titulo: String, mensaje: String) {
        crearCanalNotificaciones(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = System.currentTimeMillis().toInt()

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
