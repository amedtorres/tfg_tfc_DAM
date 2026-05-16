package com.amedtorres.bagdrop.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val misNotificaciones = NotificacionesApp(context)
        misNotificaciones.mostrarNotificacion(
            titulo = "¡Prepara tu equipaje!",
            mensaje = "Tu reserva en BagDrop comienza en 30 minutos."
        )
    }
}