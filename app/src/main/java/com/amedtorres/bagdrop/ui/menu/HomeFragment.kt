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
 */
class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val reservaRepository = ReservaRepository()

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

        // mapa del inicio
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapaUbicacion) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    // busqueda de reservas
    private fun cargarProximaReserva() {
        val uid = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            val lista = reservaRepository.obtenerReservasActivasUsuario(uid)

            if (lista.isNotEmpty()) {
                val proximaReserva = lista.sortedBy { it.fechaInicio }.first()
                mostrarReservaEnHome(proximaReserva)
            } else {
                binding.layoutSinReserva.visibility = View.VISIBLE
                binding.layoutConReserva.root.visibility = View.GONE
            }
        }
    }

    //mostramos recordatorio de reserva en el inicio
    private fun mostrarReservaEnHome(reserva: Reserva) {
        // Ocultamos la tarjeta vacía y mostramos el include
        binding.layoutSinReserva.visibility = View.GONE
        binding.layoutConReserva.root.visibility = View.VISIBLE

        val bindingReserva = binding.layoutConReserva

        bindingReserva.tvEstado.text = reserva.estado
        bindingReserva.tvPinValor.text = reserva.pinAcceso
        bindingReserva.tvPrecio.text = String.format(Locale.getDefault(), "%.2f €", reserva.precioTotal)

        val formato = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        bindingReserva.tvFechas.text = "${formato.format(reserva.fechaInicio)} - ${formato.format(reserva.fechaFin)}"

        bindingReserva.tvDetalleMaletas.text = "${reserva.cantPeq} Peq | ${reserva.cantMed} Med | ${reserva.cantGde} Gde"

        bindingReserva.btnCancelarReservaItem.visibility = View.GONE

        bindingReserva.btnCompletarReservaItem.visibility = View.GONE

        bindingReserva.tvTituloRecordatorio.visibility = View.VISIBLE
    }

    // Función para buscar el nombre del usuario en Firestore
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


    override fun onMapReady(googleMap: GoogleMap) {
        val ubicacionBagDrop = LatLng(40.462520, -3.581599)
        val miMarcador= googleMap.addMarker(
            MarkerOptions()
                .position(ubicacionBagDrop)
                .title("Local BagDrop")
                .snippet("C. San Severo, 18, 28042 Madrid")
        )
        // moviiemiento del mapa a la ubicacion
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionBagDrop, 16f))
        miMarcador?.showInfoWindow()
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
    }


}