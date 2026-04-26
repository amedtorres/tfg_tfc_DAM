package com.amedtorres.bagdrop.ui.fragmentReservar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.databinding.FragmentReservarBinding
import com.amedtorres.bagdrop.ui.menu.MisReservasFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservarFragment : Fragment() {

    private var _binding: FragmentReservarBinding? = null
    private val binding get() = _binding!!

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

    // contadores con límites independientes
    private fun botonesContadores() {
        // MALETAS PEQUEÑAS - Límite 30
        binding.btnMasPeq.setOnClickListener {
            if (cantPeq < MAX_PEQ) { cantPeq++; actualizarTextos() }
        }
        binding.btnMenosPeq.setOnClickListener {
            if (cantPeq > 0) { cantPeq--; actualizarTextos() }
        }

        // MALETAS MEDIANAS - Límite 40
        binding.btnMasMed.setOnClickListener {
            if (cantMed < MAX_MED) { cantMed++; actualizarTextos() }
        }
        binding.btnMenosMed.setOnClickListener {
            if (cantMed > 0) { cantMed--; actualizarTextos() }
        }

        // MALETAS GRANDES - Limite 30
        binding.btnMasGde.setOnClickListener {
            if (cantGde < MAX_GDE) { cantGde++; actualizarTextos() }
        }
        binding.btnMenosGde.setOnClickListener {
            if (cantGde > 0) { cantGde--; actualizarTextos() }
        }
    }

    private fun getTotalMaletas(): Int {
        return cantPeq + cantMed + cantGde
    }

    private fun actualizarTextos() {
        binding.tvCantPeq.text = cantPeq.toString()
        binding.tvCantMed.text = cantMed.toString()
        binding.tvCantGde.text = cantGde.toString()

        // Desactivamos el botón "+" individualmente si llegan a su límite físico
        binding.btnMasPeq.isEnabled = cantPeq < MAX_PEQ
        binding.btnMasMed.isEnabled = cantMed < MAX_MED
        binding.btnMasGde.isEnabled = cantGde < MAX_GDE
    }

    // CALENDARIOS Y RELOJ
    private fun calendarios() {
        binding.etFechaEntrada.setOnClickListener { mostrarSelectorFechaYHora(true) }
        binding.etFechaSalida.setOnClickListener { mostrarSelectorFechaYHora(false) }
    }

    private fun mostrarSelectorFechaYHora(esEntrada: Boolean) {
        val calendarioActual = Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, anio, mes, dia ->
            TimePickerDialog(requireContext(), { _, hora, minuto ->

                val fechaSeleccionada = Calendar.getInstance()
                fechaSeleccionada.set(anio, mes, dia, hora, minuto)

                val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                if (esEntrada) {
                    fechaEntrada = fechaSeleccionada
                    binding.etFechaEntrada.setText(formato.format(fechaSeleccionada.time))
                } else {
                    // VALIDACIÓN EXTRA: Que la salida no sea antes de la entrada
                    if (fechaEntrada != null && fechaSeleccionada.before(fechaEntrada)) {
                        Toast.makeText(requireContext(), "La salida no puede ser antes de la entrada", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                    fechaSalida = fechaSeleccionada
                    binding.etFechaSalida.setText(formato.format(fechaSeleccionada.time))
                }

            }, calendarioActual.get(Calendar.HOUR_OF_DAY), calendarioActual.get(Calendar.MINUTE), true).show()

        }, calendarioActual.get(Calendar.YEAR), calendarioActual.get(Calendar.MONTH), calendarioActual.get(Calendar.DAY_OF_MONTH)).show()
    }


    // LÓGICA DE BOTONES PRINCIPALES
    private fun botonesPrincipales() {

        binding.btnCancelar.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnComprobar.setOnClickListener {
            // Mínimo 1 maleta
            if (getTotalMaletas() == 0) {
                Toast.makeText(requireContext(), "Añade al menos 1 maleta para reservar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Fechas completas
            if (fechaEntrada == null || fechaSalida == null) {
                Toast.makeText(requireContext(), "Por favor, selecciona las fechas de entrada y salida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Muestra la barra de carga
            binding.btnComprobar.visibility = View.GONE
            binding.progressBarReserva.visibility = View.VISIBLE

            // TODO: AQUÍ HAREMOS LA CONSULTA A FIREBASE EN EL SIGUIENTE PASO
            // De momento simulamos la espera de 1.5 segundos
            Handler(Looper.getMainLooper()).postDelayed({
                binding.progressBarReserva.visibility = View.GONE
                binding.btnConfirmar.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "¡Disponibilidad confirmada!", Toast.LENGTH_SHORT).show()
            }, 1500)
        }

        binding.btnConfirmar.setOnClickListener {
            val pinAcceso = (100000..999999).random().toString()
            Toast.makeText(requireContext(), "¡Reserva confirmada! Tu PIN es: $pinAcceso", Toast.LENGTH_LONG).show()

            // TODO: GUARDAR LA RESERVA EN FIREBASE

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, MisReservasFragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}