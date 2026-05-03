package com.amedtorres.bagdrop.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.amedtorres.bagdrop.R

class NotificacionesApp(private val context: Context) {
    // Identificador único de nuestro canal de notificaciones
    private val CHANNEL_ID = "bagdrop_reservas"

    init {
        crearCanalNotificacion()
    }

    /**
     * Desde Android 8, es obligatorio crear un "Canal" para las notificaciones.
     * Si no, Android las bloquea.
     */
    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Avisos de Reservas"
            val descriptionText = "Avisos importantes sobre tus maletas"
            val importance = NotificationManager.IMPORTANCE_HIGH // Importancia ALTA para que salga el pop-up

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Esta función es la que llamaremos cuando falten 30 minutos.
     */
    fun mostrarNotificacion(titulo: String, mensaje: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logobd)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Se borra cuando el usuario la toca

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // El número 1 es el ID de la notificación.
        notificationManager.notify(1, builder.build())
    }
}