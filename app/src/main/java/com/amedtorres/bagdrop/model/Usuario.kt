package com.amedtorres.bagdrop.model

/**
 * Clase que representa un usuario.
 *
 * @property idUsuario El identificador único del usuario.
 * @property nombre El nombre del usuario.
 * @property email El correo electrónico del usuario.
 * @property telefono El número de teléfono del usuario.
 * @property fechaRegistro La fecha de registro del usuario.
 */
data class Usuario(
    val idUsuario: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()  // mas facil de manejar en minilesungnfos
)
