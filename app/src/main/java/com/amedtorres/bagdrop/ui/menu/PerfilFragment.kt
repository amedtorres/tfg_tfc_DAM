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

    // Instancias directas de Firebase (igual que hicimos en el HomeFragment)
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

    /**
     * Descarga los datos del usuario desde Firebase y los pinta en los TextViews
     */
    private fun cargarDatosPerfil() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid

            // 1. Correo y ID (Desde Firebase Auth)
            binding.tvEmailValor.text = user.email ?: "Sin correo"

            // Formateamos el ID para que no sea un texto larguísimo. Cogemos los 8 primeros caracteres.
            val idCorto = if (uid.length > 8) uid.substring(0, 8).uppercase() else uid.uppercase()
            binding.tvUsuarioIdValor.text = "#$idCorto"

            // 2. Nombre y Teléfono (Desde Firestore, colección "usuarios")
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Nombre
                        val nombre = document.getString("nombre") ?: "Usuario BagDrop"
                        binding.tvNombrePerfil.text = nombre

                        // Teléfono (Si no existe el campo en Firestore, ponemos un texto por defecto)
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

    /**
     * Da vida a la opción de cambiar contraseña y al botón de cerrar sesión
     */
    private fun configurarBotones() {

        // --- CAMBIAR CONTRASEÑA ---
        binding.itemCambiarContrasena.setOnClickListener {
            val email = auth.currentUser?.email
            if (email != null) {
                // Le pedimos a Firebase que envíe el correo oficial de recuperación
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

        // --- CERRAR SESIÓN ---
        binding.btnCerrarSesion.setOnClickListener {
            // Deslogueamos de Firebase
            auth.signOut()

            // Viajamos a la pantalla de Login y borramos el historial para que no pueda volver atrás
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