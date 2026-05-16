package com.amedtorres.bagdrop.ui.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amedtorres.bagdrop.databinding.FragmentPerfilBinding
import com.amedtorres.bagdrop.ui.auth.LoginActivity // Asegúrate de que esta ruta coincida con tu Login
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilFragment : Fragment() {
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosPerfil()
        configurarBotones()
    }

    // Cargamos los datos del perfil del usuario desde Firestore
    private fun cargarDatosPerfil() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            binding.tvEmailValor.text = user.email ?: "Sin correo"

            // formateo del ID para 8 caracteres
            val idCorto = if (uid.length > 8) uid.substring(0, 8).uppercase() else uid.uppercase()
            binding.tvUsuarioIdValor.text = "#$idCorto"

            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre") ?: "Usuario BagDrop"
                        binding.tvNombrePerfil.text = nombre
                        val telefonoDb = document.getString("telefono")
                        if (!telefonoDb.isNullOrEmpty()) {
                            binding.tvTelefonoValor.text = telefonoDb
                        } else {
                            binding.tvTelefonoValor.text = "No especificado"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PerfilFragment", "Error al cargar datos de Firestore", e)
                    binding.tvNombrePerfil.text = "usuario123"
                    binding.tvTelefonoValor.text = "numero0123"
                }
        }
    }

    // configuracion de los botones
    private fun configurarBotones() {
        binding.itemCambiarContrasena.setOnClickListener {
            val email = auth.currentUser?.email
            if (email != null) {
                // firebase envia  el correo de restablecimiento de contraseña
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "¡Revisa tu correo! Te hemos enviado un enlace.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Error al enviar el correo. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // boton de cerrar sesion
        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "¡Hasta pronto! Sesión cerrada.", Toast.LENGTH_SHORT).show()
            // cierra sesiin y borramos el historial para que no pueda volver atrás
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}