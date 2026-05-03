package com.amedtorres.bagdrop.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Esta clase es un "BroadcastReceiver".
 * Android la despierta automáticamente a la hora que le digamos en el futuro,
 * ¡incluso si la app está cerrada!
 */
class AlarmaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Android nos ha despertado. ¡Es la hora!

        // 2. Cogemos nuestro "megáfono" (Asegúrate de usar el nombre que le hayas puesto a tu archivo)
        val misNotificaciones = NotificacionesApp(context)

        // 3. ¡Lanzamos el aviso a la pantalla del usuario!
        misNotificaciones.mostrarNotificacion(
            titulo = "¡Prepara tu equipaje!",
            mensaje = "Tu reserva en BagDrop comienza en 30 minutos."
        )
    }
}