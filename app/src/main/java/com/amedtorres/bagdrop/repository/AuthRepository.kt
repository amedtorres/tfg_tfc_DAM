package com.amedtorres.bagdrop.repository

import com.amedtorres.bagdrop.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * @author Amed Torres
     * @version 1.0
     * @param onComplete: Es un "Callback". Nos avisará si ha ido bien (Boolean) y nos dará un mensaje (String).
     */

    //  Función para registrar un nuevo usuario en Firebase Auth y guardar sus datos en Firestore.
    fun registrarUsuario(
        nombre: String,
        email: String,
        telefono: String,
        contrasena: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        // Creación del usuario en el sistema de Autenticación con correo y contraseña
        auth.createUserWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Si el registro va bien, obtenemos el ID único que Firebase le ha dado
                    val userId = auth.currentUser?.uid ?: ""

                    // 2. Creamos nuestro objeto Usuario con los datos del formulario
                    val nuevoUsuario = Usuario(
                        idUsuario = userId,
                        nombre = nombre,
                        email = email,
                        telefono = telefono,
                        fechaRegistro = System.currentTimeMillis() // Guardamos la fecha actual en milisegundos
                    )

                    // 3. Guardamos este objeto en la colección "usuarios" de nuestra base de datos (Firestore)
                    db.collection("usuarios").document(userId).set(nuevoUsuario)
                        .addOnSuccessListener {
                            onComplete(true, "Registro exitoso")
                        }
                        .addOnFailureListener { exception ->
                            onComplete(false, "Error al guardar datos: ${exception.message}")
                        }

                    // ... (código anterior de AuthRepository)
                } else {
                    // Atrapamos las excepciones específicas de Firebase
                    val mensajeError = when (task.exception) {
                        is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil - Minimo 6 caracteres."
                        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este correo."
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "El formato del correo es inválido."
                        else -> "Error en el registro: ${task.exception?.message}"
                    }
                    onComplete(false, mensajeError)
                }

            }
    }

    fun iniciarSesion(
        email: String,
        contrasena: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, "Inicio de sesión exitoso")
                } else {
                    // sifalla en el inicio de sesion
                    val mensajeError = when (task.exception) {
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "El usuario no existe."
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Correo no registrado o contraseña incorrecta."
                        else -> "Error en el inicio de sesión: ${task.exception?.message}"
                    }
                    onComplete(false, mensajeError)
                }
            }
    }
}
