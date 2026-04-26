package com.amedtorres.bagdrop.ui.fragmentReservar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.databinding.FragmentReservarBinding
import com.amedtorres.bagdrop.model.Reserva
import com.amedtorres.bagdrop.repository.ReservaRepository
import com.amedtorres.bagdrop.ui.menu.MisReservasFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservarFragment : Fragment() {

    private var _binding: FragmentReservarBinding? = null
    private val binding get() = _binding!!

    // 1. Instanciamos nuestro Repositorio y FirebaseAuth
    private val reservaRepository = ReservaRepository()
    private val auth = FirebaseAuth.getInstance()

    // Contadores actuales para las cantidades
    private var cantPeq = 0
    private var cantMed = 0
    private var cantGde = 0

    // Límites máximos para cada tipo de maleta
    private val MAX_PEQ = 30
    private val MAX_MED = 40
    private val MAX_GDE = 30

    // Variables para guardar las fechas
    private var fechaEntrada: Calendar? = null
    private var fechaSalida: Calendar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReservarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        botonesContadores()
        calendarios()
        botonesPrincipales()
    }

    // ==========================================
    // CONTADORES
    // ==========================================
    private fun botonesContadores() {
        binding.btnMasPeq.setOnClickListener { if (cantPeq < MAX_PEQ) { cantPeq++; actualizarTextos() } }
        binding.btnMenosPeq.setOnClickListener { if (cantPeq > 0) { cantPeq--; actualizarTextos() } }

        binding.btnMasMed.setOnClickListener { if (cantMed < MAX_MED) { cantMed++; actualizarTextos() } }
        binding.btnMenosMed.setOnClickListener { if (cantMed > 0) { cantMed--; actualizarTextos() } }

        binding.btnMasGde.setOnClickListener { if (cantGde < MAX_GDE) { cantGde++; actualizarTextos() } }
        binding.btnMenosGde.setOnClickListener { if (cantGde > 0) { cantGde--; actualizarTextos() } }
    }

    private fun getTotalMaletas(): Int = cantPeq + cantMed + cantGde

    private fun actualizarTextos() {
        binding.tvCantPeq.text = cantPeq.toString()
        binding.tvCantMed.text = cantMed.toString()
        binding.tvCantGde.text = cantGde.toString()

        binding.btnMasPeq.isEnabled = cantPeq < MAX_PEQ
        binding.btnMasMed.isEnabled = cantMed < MAX_MED
        binding.btnMasGde.isEnabled = cantGde < MAX_GDE
    }

    // ==========================================
    // CALENDARIOS
    // ==========================================
    private fun calendarios() {
        binding.etFechaEntrada.setOnClickListener { mostrarSelectorFechaYHora(true) }
        binding.etFechaSalida.setOnClickListener { mostrarSelectorFechaYHora(false) }
    }

    private fun mostrarSelectorFechaYHora(esEntrada: Boolean) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, anio, mes, dia ->
            TimePickerDialog(requireContext(), { _, hora, minuto ->
                val fechaSel = Calendar.getInstance()
                fechaSel.set(anio, mes, dia, hora, minuto)
                val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                if (esEntrada) {
                    fechaEntrada = fechaSel
                    binding.etFechaEntrada.setText(formato.format(fechaSel.time))
                } else {
                    if (fechaEntrada != null && fechaSel.before(fechaEntrada)) {
                        Toast.makeText(requireContext(), "La salida no puede ser antes de la entrada", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                    fechaSalida = fechaSel
                    binding.etFechaSalida.setText(formato.format(fechaSel.time))
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    // ==========================================
    // CÁLCULO DE PRECIO
    // ==========================================
    private fun calcularPrecioTotal(): Double {
        if (fechaEntrada == null || fechaSalida == null) return 0.0

        // Calculamos los días (mínimo 1 día de cobro)
        val diffMilisegundos = fechaSalida!!.timeInMillis - fechaEntrada!!.timeInMillis
        var dias = (diffMilisegundos / (1000 * 60 * 60 * 24)).toInt()
        if (dias == 0) dias = 1

        // Precios base: Peq: 3.0€, Med: 4.5€, Gde: 5.5€
        return ((cantPeq * 3.0) + (cantMed * 4.5) + (cantGde * 5.5)) * dias
    }

    // ==========================================
    // BOTONES PRINCIPALES (Conexión a Firebase)
    // ==========================================
    private fun botonesPrincipales() {

        binding.btnCancelar.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnComprobar.setOnClickListener {
            if (getTotalMaletas() == 0) {
                Toast.makeText(requireContext(), "Añade al menos 1 maleta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (fechaEntrada == null || fechaSalida == null) {
                Toast.makeText(requireContext(), "Selecciona las fechas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnComprobar.visibility = View.GONE
            binding.progressBarReserva.visibility = View.VISIBLE

            // USAMOS CORRUTINAS PARA HABLAR CON EL REPOSITORIO
            lifecycleScope.launch {
                val hayHueco = reservaRepository.comprobarDisponibilidad(
                    cantPeq, cantMed, cantGde,
                    fechaEntrada!!.timeInMillis,
                    fechaSalida!!.timeInMillis
                )

                binding.progressBarReserva.visibility = View.GONE

                if (hayHueco) {
                    binding.btnConfirmar.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "¡Taquillas disponibles!", Toast.LENGTH_SHORT).show()
                } else {
                    binding.btnComprobar.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Lo sentimos, no hay taquillas suficientes para esas fechas", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnConfirmar.setOnClickListener {
            // Desactivamos el botón para evitar doble clic accidental
            binding.btnConfirmar.isEnabled = false

            // 1. Preparamos todos los datos
            val pinGenerado = (100000..999999).random().toString()
            val usuarioId = auth.currentUser?.uid ?: "usuario_anonimo"
            val precio = calcularPrecioTotal()

            val nuevaReserva = Reserva(
                idUsuario = usuarioId,
                cantPeq = cantPeq,
                cantMed = cantMed,
                cantGde = cantGde,
                fechaInicio = fechaEntrada!!.timeInMillis,
                fechaFin = fechaSalida!!.timeInMillis,
                pinAcceso = pinGenerado,
                precioTotal = precio
            )

            // 2. Guardamos en Firebase usando nuestro Repositorio
            lifecycleScope.launch {
                val idGenerado = reservaRepository.guardarReserva(nuevaReserva)

                if (idGenerado != null) {
                    Toast.makeText(requireContext(), "¡Reserva completada!", Toast.LENGTH_LONG).show()

                    // Viajamos a la pantalla de Mis Reservas
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, MisReservasFragment())
                        .commit()
                } else {
                    binding.btnConfirmar.isEnabled = true
                    Toast.makeText(requireContext(), "Error al guardar la reserva", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}