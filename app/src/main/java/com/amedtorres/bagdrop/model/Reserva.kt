package com.amedtorres.bagdrop.model

/**
 * Clase que representa una reserva.
 *@property idReserva El identificador único de la reserva.
 *@property idUsuario El identificador del usuario que realiza la reserva.
 *@property tipoMaletaId El identificador del tipo de maleta que se reserva.
 *@property fechaInicio La fecha de inicio de la reserva.
 * @property fechaFin la fecga de fin de la reserva
 * @property cantidad La cantidad de maletas reservadas
 * @property estado El estado de la reserva (activa, completada, cancelada)
 * @property pinAcceso El pin de acceso a la reserva.
 * @property precioTotal El precio total de la reserva.
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
