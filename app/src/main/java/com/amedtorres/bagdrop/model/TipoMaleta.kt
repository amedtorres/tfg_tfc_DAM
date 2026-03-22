package com.amedtorres.bagdrop.model

/**
 * Clase que representa un tipo de maleta.
 *
 * @property idTipoMaleta El identificador único del tipo de maleta.
 * @property nombre El nombre del tipo de maleta (Pequeña - Mediana - Grande).
 * @property capacidadTotal La capacidad total de la maleta en el local.
 * @property precioDia El precio por día de la maleta.
 */
data class TipoMaleta(
    val idTipoMaleta: String = "",
    val nombre: String = "",
    val capacidadTotal: Int = 0,
    val precioDia: Double = 0.0
)
