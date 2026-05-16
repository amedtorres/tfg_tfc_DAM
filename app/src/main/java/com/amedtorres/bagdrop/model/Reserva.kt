package com.amedtorres.bagdrop.model

/**
 * Clase que representa una reserva
 */
data class Reserva(
    val idReserva: String = "",
    val idUsuario: String = "",
    val cantPeq: Int = 0,
    val cantMed: Int = 0,
    val cantGde: Int = 0,
    val fechaInicio: Long = 0L, // Long en milisegundos para facilitar las comparaciones
    val fechaFin: Long = 0L,
    val estado: String = "activa",
    val pinAcceso: String = "",
    val precioTotal: Double = 0.0
)
