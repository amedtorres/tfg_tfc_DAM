package com.amedtorres.bagdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.amedtorres.bagdrop.databinding.ActivityLoginBinding
import com.amedtorres.bagdrop.repository.AuthRepository
import com.amedtorres.bagdrop.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * @author: Amed Torres
 * @version: 1.0
 * @description: Activity de Login -  Clase que representa la vista de Login y gestiona la autenticación
 * */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Instanciamos el repositorio para hablar con Firebase
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navegación a la pantalla de Registro
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // botón "Acceder"
        binding.btnLogin.setOnClickListener {
            iniciarSesion()
        }
    }

    override fun onStart() {
        super.onStart()
        // Comprobamos si Firebase ya tiene un usuario guardado
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Nos saltamos el Login y lo mandamos directo a la app
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cerramos esta pantalla de Login para que no se quede de fondo
        }
    }

    private fun iniciarSesion() {
        // Recogemos los datos eliminando espacios en blanco accidentales
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validamos que no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce tu correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamamos a Firebase a través del Repositorio
        authRepository.iniciarSesion(email, password) { exito, mensaje ->
            if (exito) {
                // Si el login es correcto, vamos a la pantalla principal de la app
                Toast.makeText(this, "¡Bienvenido a BagDrop!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cerramos el Login para que no pueda volver atrás con el botón físico
            } else {
                // Si hay error (contraseña mal, etc.), lo mostramos
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }
}