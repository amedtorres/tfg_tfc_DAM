package com.amedtorres.bagdrop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.databinding.ActivityRegisterBinding
import com.amedtorres.bagdrop.repository.AuthRepository
/**
 * @author Amed Torres
 * @version 1.0
 * @description Activity de Registro -  Clase que representa la vista de Registro
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    // Instancia del repositorio de autenticación
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // navegación a la vista de login
        binding.tvAcceder.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegistrar.setOnClickListener {
            crearCuenta()
        }
    }

    // funcion para recoeger la info del usuario pora su registro
    private fun crearCuenta(){
        val nombre = binding.etNombre.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val contrasena = binding.etPassword.text.toString().trim()

        // Validaciones de campos vacios
        if( nombre.isEmpty() || email.isEmpty() || telefono.isEmpty() || contrasena.isEmpty()){
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        //validacion de contraseña - min 6 caracteres, min 1 mayuscula y min 1 numero
        if (contrasena.length < 6 || !contrasena.any { it.isUpperCase() } || !contrasena.any { it.isDigit() }) {
            binding.tilPassword.error = "Mínimo 6 caracteres, 1 mayúscula y 1 número"
            return
        } else {
            // Si la contraseña cumple las reglas, limpiamos el error visual
            binding.tilPassword.error = null
        }

        // Llamada al repositorio para registrar el usuario
        authRepository.registrarUsuario(nombre, email, telefono, contrasena) {exito, mensaje ->
            if (exito) {
                Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // ccerramos pantalla de registro
            } else {
                // Si Firebase  da error, mostramos el mensaje
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }
}