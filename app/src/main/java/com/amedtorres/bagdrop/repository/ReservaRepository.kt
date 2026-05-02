package com.amedtorres.bagdrop.repository

import android.util.Log
import com.amedtorres.bagdrop.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReservaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccionReservas = db.collection("reservas")

    // Límites de cantidades máximos para cada tipo de maleta
    private val MAX_PEQ = 30
    private val MAX_MED = 40
    private val MAX_GDE = 30

    /**
     * Comprueba si hay disponibilidad de taquillas para las fechas y cantidades solicitadas.
     * Utiliza corrutinas (suspend) para manejar la llamada asíncrona a Firebase de forma limpia.
     *
     * @return true si hay hueco para todas las maletas solicitadas, false si no.
     */
    suspend fun comprobarDisponibilidad(
        cantPeqSol: Int,
        cantMedSol: Int,
        cantGdeSol: Int,
        inicioSolicitado: Long,
        finSolicitado: Long
    ): Boolean {
        return try {
            // Buscamos solo las reservas que están ocupando sitio
            val snapshot = coleccionReservas.whereEqualTo("estado", "activa").get().await()

            var ocupadasPeq = 0
            var ocupadasMed = 0
            var ocupadasGde = 0

            for (document in snapshot.documents) {
                // Convertimos el documento de Firestore a objeto Kotlin
                val reservaExistente = document.toObject(Reserva::class.java)

                if (reservaExistente != null) {
                    // Lógica de Solapamiento de Fechas
                    // Si la reserva existente EMPIEZA ANTES de que el nuevo usuario SALGA...
                    // Y la reserva existente TERMINA DESPUÉS de que el nuevo usuario ENTRE...
                    // Significa que coinciden en el tiempo.
                    if (reservaExistente.fechaInicio < finSolicitado && reservaExistente.fechaFin > inicioSolicitado) {
                        ocupadasPeq += reservaExistente.cantPeq
                        ocupadasMed += reservaExistente.cantMed
                        ocupadasGde += reservaExistente.cantGde
                    }
                }
            }

            // Comprobamos si las solicitadas + las ocupadas superan el máximo del local
            val cabePeq = (cantPeqSol + ocupadasPeq) <= MAX_PEQ
            val cabeMed = (cantMedSol + ocupadasMed) <= MAX_MED
            val cabeGde = (cantGdeSol + ocupadasGde) <= MAX_GDE

            // Retornamos true SOLO si hay espacio para los tres tipos a la vez
            cabePeq && cabeMed && cabeGde

        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error comprobando disponibilidad: ${e.message}")
            // Si hay un error de conexión, por seguridad decimos que no hay disponibilidad
            false
        }
    }

    /**
     * Guarda una nueva reserva en Firestore.
     *
     * @param reserva El objeto Reserva a guardar.
     * @return El ID de la reserva generada si es exitoso, o null si falla.
     */
    suspend fun guardarReserva(reserva: Reserva): String? {
        return try {
            // Generamos un nuevo documento con un ID automático en la colección "reservas"
            val nuevaReferencia = coleccionReservas.document()

            // Le asignamos ese ID generado a nuestro objeto para tenerlo guardado
            val reservaConId = reserva.copy(idReserva = nuevaReferencia.id)

            // Subimos el objeto a Firebase
            nuevaReferencia.set(reservaConId).await()

            // Retornamos el ID por si la pantalla necesita saberlo
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

            // Convierte los documentos de Firebase a nuestra clase Reserva
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

    // Obtiene el historial de reservas (Canceladas o Completadas) de un usuario.

    suspend fun obtenerHistorialUsuario(idUsuario: String): List<Reserva> {
        return try {
            val snapshot = coleccionReservas
                .whereEqualTo("idUsuario", idUsuario)
                .whereIn("estado", listOf("Cancelada", "Completada")) // Busca cualquiera de estos dos estados
                .get().await()

            snapshot.documents.mapNotNull { it.toObject(Reserva::class.java) }
        } catch (e: Exception) {
            Log.e("ReservaRepository", "Error descargando historial: ${e.message}")
            emptyList()
        }
    }
}