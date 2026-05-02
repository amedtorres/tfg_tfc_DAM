package com.amedtorres.bagdrop.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.databinding.FragmentHomeBinding
import com.amedtorres.bagdrop.model.Reserva
import com.amedtorres.bagdrop.repository.ReservaRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.gms.maps.model.LatLng

/**
 * @author Amed Torres
 * @version 1.0
 * @description Fragmento de Inicio - Muestra el saludo y el estado de la reserva
 */
class HomeFragment : Fragment(), OnMapReadyCallback {

    // 1. Configuración de ViewBinding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 2. Instancias de Firebase y Repositorio
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val reservaRepository = ReservaRepository() // Añadimos nuestro repositorio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargamos los datos del inicio
        cargarNombreUsuario()
        cargarProximaReserva()

        binding.btnHacerReserva.setOnClickListener {
            val fragment = com.amedtorres.bagdrop.ui.fragmentReservar.ReservarFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit()
        }

        // --- CONFIGURACIÓN DEL MAPA ---
        // Buscamos el fragmento del mapa que pusimos en el XML
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapaUbicacion) as SupportMapFragment?
        // Le decimos que nos avise cuando esté listo para usar
        mapFragment?.getMapAsync(this)
    }

    /**
     * Busca todas las reservas activas y muestra solo la más cercana en el tiempo
     */
    private fun cargarProximaReserva() {
        val uid = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            // Vamos a Firebase a por la lista de reservas
            val lista = reservaRepository.obtenerReservasActivasUsuario(uid)

            if (lista.isNotEmpty()) {
                // Ordenamos la lista por la fecha de inicio y cogemos la primera (la más próxima)
                val proximaReserva = lista.sortedBy { it.fechaInicio }.first()
                mostrarReservaEnHome(proximaReserva)
            } else {
                // Si no hay reservas, mostramos la tarjeta vacía y ocultamos la detallada
                binding.layoutSinReserva.visibility = View.VISIBLE
                binding.layoutConReserva.root.visibility = View.GONE
            }
        }
    }

    /**
     * Rellena la tarjeta de reserva (item_reserva) con los datos y la hace visible
     */
    private fun mostrarReservaEnHome(reserva: Reserva) {
        // Ocultamos la tarjeta vacía y mostramos el include
        binding.layoutSinReserva.visibility = View.GONE
        binding.layoutConReserva.root.visibility = View.VISIBLE

        // Accedemos a los Textos que están dentro del <include> (layoutConReserva)
        val bindingReserva = binding.layoutConReserva

        bindingReserva.tvEstado.text = reserva.estado
        bindingReserva.tvPinValor.text = reserva.pinAcceso
        bindingReserva.tvPrecio.text = String.format(Locale.getDefault(), "%.2f €", reserva.precioTotal)

        // Formateamos las fechas
        val formato = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        bindingReserva.tvFechas.text = "${formato.format(reserva.fechaInicio)} - ${formato.format(reserva.fechaFin)}"

        bindingReserva.tvDetalleMaletas.text = "${reserva.cantPeq} Peq | ${reserva.cantMed} Med | ${reserva.cantGde} Gde"

        // oculatamos el boton cancelar
        bindingReserva.btnCancelarReservaItem.visibility = View.GONE

        // oculatamos el boton completar
        bindingReserva.btnCompletarReservaItem.visibility = View.GONE


        //titul recordarotio solo para el home
        bindingReserva.tvTituloRecordatorio.visibility = View.VISIBLE
    }

    /**
     * Función que busca el nombre del usuario en Firestore y lo pone en el TextView
     */
    private fun cargarNombreUsuario() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre") ?: "Usuario"
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
            binding.tvSaludo.text = "Hola, Invitado"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Esta función se dispara automáticamente cuando Google Maps ha terminado de cargar.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        // Coordenadas de Calle San Severo, 18, Barajas, Madrid
        val ubicacionBagDrop = LatLng(40.462520, -3.581599)
//        val ubicacionBagDrop = LatLng(40.462283, -3.581364)

        // 1. Ponemos la chincheta (Marcador)
        val miMarcador= googleMap.addMarker(
            MarkerOptions()
                .position(ubicacionBagDrop)
                .title("Local BagDrop")
                .snippet("C. San Severo, 18, 28042 Madrid")
        )

        // 2. Movemos la cámara hacia tu local
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionBagDrop, 16f))

//        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionBagDrop, 16f), 2000, null)

        // 3. ¡LA MAGIA! Forzamos a que el cartelito se abra solo sin que el usuario toque nada
        miMarcador?.showInfoWindow()

        // 2. Movemos la cámara allí y le damos un zoom bonito (el 15f o 16f suele quedar genial para ver la calle)
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
    }


}