package com.amedtorres.bagdrop.model

/**
 * Clase que representa un usuario.
*/
data class Usuario(
    val idUsuario: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()  // mas facil de manejar en minilesungnfos
)