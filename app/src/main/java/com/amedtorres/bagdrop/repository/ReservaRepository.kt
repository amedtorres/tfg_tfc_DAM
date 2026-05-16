package com.amedtorres.bagdrop.repository

import android.util.Log
import com.amedtorres.bagdrop.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReservaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccionReservas = db.collection("reservas")

    // limites de cantidades para cada tipo de maleta
    private val MAX_PEQ = 30
    private val MAX_MED = 40
    private val MAX_GDE = 30

    // comprobacion si hay disponibilidad para fechas y cantidades solicitadas
    suspend fun comprobarDisponibilidad(
        cantPeqSol: Int,
        cantMedSol: Int,
        cantGdeSol: Int,
        inicioSolicitado: Long,
        finSolicitado: Long
    ): Boolean {
        return try {
            // busqueda de las reservas activas
            val snapshot = coleccionReservas.whereEqualTo("estado", "activa").get().await()
            var ocupadasPeq = 0
            var ocupadasMed = 0
            var ocupadasGde = 0

            // busqueda de solapamiento
            for (document in snapshot.documents) {
                val reservaExistente = document.toObject(Reserva::class.java)
                if (reservaExistente != null) {
                    if (reservaExistente.fechaInicio < finSolicitado && reservaExistente.fechaFin > inicioSolicitado) {
                        ocupadasPeq += reservaExistente.cantPeq
                        ocupadasMed += reservaExistente.cantMed
                        ocupadasGde += reservaExistente.cantGde
                    }
                }
            }

            // Comprobamos si las solicitadas + las ocupadas superan los limites
            val cabePeq = (cantPeqSol + ocupadasPeq) <= MAX_PEQ
            val cabeMed = (cantMedSol + ocupadasMed) <= MAX_MED
            val cabeGde = (cantGdeSol + ocupadasGde) <= MAX_GDE

            // delvolvemos true SOLO si hay espacio para los tres tipos a la vez
            cabePeq && cabeMed && cabeGde

        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error comprobando disponibilidad: ${e.message}")
            false
        }
    }

    // Guardar una nueva reserva en Firestore.
    suspend fun guardarReserva(reserva: Reserva): String? {
        return try {
            // Generamos un nuevo documento con un ID automático en la colección "reservas"
            val nuevaReferencia = coleccionReservas.document()
            val reservaConId = reserva.copy(idReserva = nuevaReferencia.id)
            nuevaReferencia.set(reservaConId).await()
            nuevaReferencia.id
        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error guardando reserva: ${e.message}")
            null
        }
    }

    //Obtiene todas las reservas Activas de un usuario concreto
    suspend fun obtenerReservasActivasUsuario(idUsuario: String): List<Reserva> {
        return try {
            val snapshot = coleccionReservas
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("estado", "activa")
                .get().await() // Descarga los datos
            snapshot.documents.mapNotNull { it.toObject(Reserva::class.java) }
        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error descargando reservas: ${e.message}")
            emptyList()
        }
    }

    //Cambia el estado de una reserva a "Cancelada".
    suspend fun cancelarReserva(idReserva: String): Boolean {
        return try {
            coleccionReservas.document(idReserva).update("estado", "Cancelada").await()
            true
        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error cancelando reserva: ${e.message}")
            false
        }
    }

    //Cambia el estado de una reserva a "Completada"
    suspend fun completarReserva(idReserva: String): Boolean {
        return try {
            coleccionReservas.document(idReserva).update("estado", "Completada").await()
            true
        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error completando reserva: ${e.message}")
            false
        }
    }

    // Guardar historial de reservas
    suspend fun obtenerHistorialUsuario(idUsuario: String): List<Reserva> {
        return try {
            val snapshot = coleccionReservas
                .whereEqualTo("idUsuario", idUsuario)
                .whereIn("estado", listOf("Cancelada", "Completada")) // Buqueda cualquiera de estos dos estados
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(Reserva::class.java) }
        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error descargando historial: ${e.message}")
            emptyList()
        }
    }
}