package com.amedtorres.bagdrop.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @author Amed Torres
 * @version 1.0
 * @description Fragmento de Inicio - Muestra el saludo y el estado de la reserva
 */
class HomeFragment : Fragment() {

    // 1. Configuración de ViewBinding para Fragmentos (es un poco diferente que en Activities)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 2. Instancias de Firebase directamente aquí (Más adelante lo pasaremos al Repositorio si quieres)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nada más abrir el fragmento, cargamos el nombre del usuario
        cargarNombreUsuario()

        binding.btnHacerReserva.setOnClickListener {
            //boton de hacer reserva
            val fragment = com.amedtorres.bagdrop.ui.fragmentReservar.ReservarFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    /**
     * Función que busca el nombre del usuario en Firestore y lo pone en el TextView
     */
    private fun cargarNombreUsuario() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            // Vamos a la colección "usuarios", buscamos el documento con el UID
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Sacamos el campo "nombre" de la base de datos
                        val nombre = document.getString("nombre") ?: "Usuario"

                        // Actualizamos la interfaz
                        binding.tvSaludo.text = "Hola, $nombre"
                    } else {
                        binding.tvSaludo.text = "Hola, Viajero"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Error al obtener el usuario: ", exception)
                    binding.tvSaludo.text = "Hola,"
                }
        } else {
            // Si por algún motivo no hay sesión iniciada
            binding.tvSaludo.text = "Hola, Invitado"
        }
    }



    // 4. Limpieza de memoria (Obligatorio en Fragmentos)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}