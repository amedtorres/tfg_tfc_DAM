package com.amedtorres.bagdrop.model

/**
 * Clase que representa un tipo de maleta.
 */
data class TipoMaleta(
    val idTipoMaleta: String = "",
    val nombre: String = "",
    val capacidadTotal: Int = 0,
    val precioDia: Double = 0.0
)
