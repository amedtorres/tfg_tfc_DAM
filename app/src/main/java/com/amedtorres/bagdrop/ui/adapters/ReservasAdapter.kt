package com.amedtorres.bagdrop.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amedtorres.bagdrop.R
import com.amedtorres.bagdrop.model.Reserva
import java.text.SimpleDateFormat
import java.util.Locale

class ReservasAdapter(
    private var listaReservas: List<Reserva>,
    private val onCancelarClick: (Reserva) -> Unit, // Función que avisa al Fragment al pulsar Cancelar
    private val onCompletarClick: (Reserva) -> Unit // Funcion que avisa al Fragment al pulsar Completar
) : RecyclerView.Adapter<ReservasAdapter.ReservaViewHolder>() {

    // vinculamos los elementos del XML
    class ReservaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        val tvFechas: TextView = itemView.findViewById(R.id.tvFechas)
        val tvDetalleMaletas: TextView = itemView.findViewById(R.id.tvDetalleMaletas)
        val tvPinValor: TextView = itemView.findViewById(R.id.tvPinValor)
        val btnCancelar: Button = itemView.findViewById(R.id.btnCancelarReservaItem)
        val btnCompletar: Button = itemView.findViewById(R.id.btnCompletarReservaItem)
    }

    // diseñoi xml q debe usar para cada tarjeta
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    //  rellenar los datos de la reserva
    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        val reserva = listaReservas[position]
        holder.tvEstado.text = reserva.estado
        holder.tvPrecio.text = String.format(Locale.getDefault(), "%.2f €", reserva.precioTotal)

        val formatoFecha = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val fechaInicioTxt = formatoFecha.format(reserva.fechaInicio)
        val fechaFinTxt = formatoFecha.format(reserva.fechaFin)
        holder.tvFechas.text = "$fechaInicioTxt - $fechaFinTxt"

        holder.tvDetalleMaletas.text = "${reserva.cantPeq} Peq | ${reserva.cantMed} Med | ${reserva.cantGde} Gde"
        holder.tvPinValor.text = reserva.pinAcceso
        holder.btnCompletar.setOnClickListener {
            onCompletarClick(reserva)
        }

        // botón Cancelar
        holder.btnCancelar.setOnClickListener {
            onCancelarClick(reserva)
        }

        // Ocultar botones para el Historial
        if (reserva.estado.lowercase() != "activa") {
            holder.btnCancelar.visibility = View.GONE
            holder.btnCompletar.visibility = View.GONE
        } else {
            holder.btnCancelar.visibility = View.VISIBLE
            holder.btnCompletar.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return listaReservas.size
    }

    // Funcion para actualizar la lista
    fun actualizarLista(nuevaLista: List<Reserva>) {
        listaReservas = nuevaLista
        notifyDataSetChanged()
    }
}
