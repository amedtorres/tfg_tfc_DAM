package com.amedtorres.bagdrop.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amedtorres.bagdrop.databinding.FragmentHistorialBinding
import com.amedtorres.bagdrop.repository.ReservaRepository
import com.amedtorres.bagdrop.ui.adapters.ReservasAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val reservaRepository = ReservaRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var historialAdapter: ReservasAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarBotones()
        configurarRecyclerView()
        cargarHistorial()
    }

    private fun configurarBotones() {
        binding.btnVolverHistorial.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun configurarRecyclerView() {
        // Reutilizamos el Adapter, pero si están en el historial no deberían poder cancelar.
        historialAdapter = ReservasAdapter(emptyList()) { _ ->
            // No hacemos nada al clickar "Cancelar" aquí
        }
        binding.rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorial.adapter = historialAdapter
    }

    private fun cargarHistorial() {
        val idUsuario = auth.currentUser?.uid ?: return

        binding.progressBarHistorial.visibility = View.VISIBLE
        binding.tvHistorialVacio.visibility = View.GONE
        binding.rvHistorial.visibility = View.GONE

        lifecycleScope.launch {
            val listaHistorial = reservaRepository.obtenerHistorialUsuario(idUsuario)

            binding.progressBarHistorial.visibility = View.GONE

            if (listaHistorial.isEmpty()) {
                binding.tvHistorialVacio.visibility = View.VISIBLE
            } else {
                binding.rvHistorial.visibility = View.VISIBLE

                // Ordenamos para que las más recientes salgan primero
                val listaOrdenada = listaHistorial.sortedByDescending { it.fechaInicio }
                historialAdapter.actualizarLista(listaOrdenada)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}