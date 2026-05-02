package com.amedtorres.bagdrop.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.databinding.FragmentMisReservasBinding
import com.amedtorres.bagdrop.repository.ReservaRepository
import com.amedtorres.bagdrop.ui.adapters.ReservasAdapter
import com.amedtorres.bagdrop.ui.fragmentReservar.ReservarFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MisReservasFragment : Fragment() {

    private var _binding: FragmentMisReservasBinding? = null
    private val binding get() = _binding!!

    private val reservaRepository = ReservaRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var reservasAdapter: ReservasAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMisReservasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNavegacion()
        configurarRecyclerView()
        cargarReservas()
    }

    private fun configurarNavegacion() {
        // Nueva Reserva
        binding.btnNuevaReserva.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, ReservarFragment())
                .addToBackStack(null) // Permite volver atrás
                .commit()
        }

        // HistorialFragment
        binding.btnHistorial.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HistorialFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun configurarRecyclerView() {
        reservasAdapter = ReservasAdapter(
            listaReservas = emptyList(),
            onCancelarClick = { reservaCancelada ->
                cancelarReserva(reservaCancelada.idReserva)
            },
            onCompletarClick = { reservaCompletada ->
                completarReserva(reservaCompletada.idReserva)
            }
        )

        // Le decimos al RecyclerView cómo debe colocar las cosas (lista vertical)
        binding.rvReservas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservas.adapter = reservasAdapter
    }

    private fun cargarReservas() {
        val idUsuario = auth.currentUser?.uid ?: "usuario_anonimo"

        // Mostramos la ruleta y ocultamos textos
        binding.progressBarLista.visibility = View.VISIBLE
        binding.tvListaVacia.visibility = View.GONE
        binding.rvReservas.visibility = View.GONE

        // Vamos a Firebase a por los datos
        lifecycleScope.launch {
            val lista = reservaRepository.obtenerReservasActivasUsuario(idUsuario)

            binding.progressBarLista.visibility = View.GONE

            if (lista.isEmpty()) {
                // Si no tiene reservas, mostramos el mensaje de lista vacía
                binding.tvListaVacia.visibility = View.VISIBLE
            } else {
                // Si tiene, las mandamos al adaptador para que las pinte
                binding.rvReservas.visibility = View.VISIBLE

                // Ordenamos por fecha de inicio para que la más próxima salga arriba
                val listaOrdenada = lista.sortedBy { it.fechaInicio }
                reservasAdapter.actualizarLista(listaOrdenada)
            }
        }
    }

    private fun cancelarReserva(idReserva: String) {
        // Volvemos a mostrar la ruleta de carga
        binding.progressBarLista.visibility = View.VISIBLE
        binding.rvReservas.visibility = View.GONE

        lifecycleScope.launch {
            val exito = reservaRepository.cancelarReserva(idReserva)
            if (exito) {
                Toast.makeText(requireContext(), "Reserva cancelada correctamente", Toast.LENGTH_SHORT).show()
                // Recargamos la lista para que la tarjeta desaparezca
                cargarReservas()
            } else {
                binding.progressBarLista.visibility = View.GONE
                binding.rvReservas.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Error al cancelar la reserva", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun completarReserva(idReserva: String) {
        binding.progressBarLista.visibility = View.VISIBLE
        binding.rvReservas.visibility = View.GONE

        lifecycleScope.launch {
            val exito = reservaRepository.completarReserva(idReserva) // Asegúrate de haber creado esta función en tu ReservaRepository
            if (exito) {
                Toast.makeText(requireContext(), "¡Maleta recogida! Reserva completada", Toast.LENGTH_SHORT).show()
                cargarReservas()
            } else {
                binding.progressBarLista.visibility = View.GONE
                binding.rvReservas.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Error al completar la reserva", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}