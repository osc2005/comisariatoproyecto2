package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class r_Reseñas {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("reseñas")

    //  1. Crear reseña
    suspend fun crearReseña(
        creditoId: String,
        productoId: String,
        productoNombre: String,
        empleadoId: String,
        empleadoNombres: String,
        empleadoApellidos: String,
        estrellas: Int,
        comentario: String
    ) = withContext(Dispatchers.IO) {
        try {
            val existe = coleccion
                .whereEqualTo("creditoId", creditoId)
                .get()
                .await()

            if (!existe.isEmpty) {
                throw Exception("Ya registraste una opinión para esta compra.")
            }

            val nuevaReseña = hashMapOf(
                "productoId"        to productoId,
                "productoNombre"    to productoNombre,
                "empleadoId"        to empleadoId,
                "empleadoNombres"   to empleadoNombres,
                "empleadoApellidos" to empleadoApellidos,
                "creditoId"         to creditoId,
                "estrellas"         to estrellas,
                "comentario"        to comentario,
                "fechaReseña"       to Timestamp.now()
            )

            coleccion.add(nuevaReseña).await()

        } catch (e: Exception) {
            throw e
        }
    }

    //  2. Reseñas de un producto en tiempo real
    fun obtenerReseñasDeProducto(productoId: String): Flow<List<m_Reseña>> =
        callbackFlow {
            val listener = coleccion
                .whereEqualTo("productoId", productoId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val lista = snapshot?.documents?.mapNotNull { doc ->
                        doc.toReseña()
                    }?.sortedByDescending { it.fechaReseña?.seconds ?: 0L } ?: emptyList()

                    trySend(lista)
                }

            awaitClose { listener.remove() }
        }

    // 3. Obtener todas las reseñas de un empleado
    fun obtenerReseñasDeEmpleado(empleadoId: String): Flow<List<m_Reseña>> =
        callbackFlow {
            val listener = coleccion
                .whereEqualTo("empleadoId", empleadoId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val lista = snapshot?.documents?.mapNotNull { it.toReseña() } ?: emptyList()
                    trySend(lista)
                }
            awaitClose { listener.remove() }
        }

    // 4. Obtener reseña específica de un crédito
    fun obtenerReseñaDeCredito(creditoId: String): Flow<m_Reseña?> =
        callbackFlow {
            val listener = coleccion
                .whereEqualTo("creditoId", creditoId)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val resena = snapshot?.documents?.firstOrNull()?.toReseña()
                    trySend(resena)
                }
            awaitClose { listener.remove() }
        }

    //  5. Verificar si un crédito ya tiene reseña
    suspend fun creditoYaTieneReseña(creditoId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val resultado = coleccion
                    .whereEqualTo("creditoId", creditoId)
                    .get()
                    .await()
                !resultado.isEmpty
            } catch (e: Exception) {
                false
            }
        }

    // 6. Promedio y conteo de reseñas de un producto
    suspend fun obtenerEstadisticasProducto(productoId: String): Pair<Double, Int> =
        withContext(Dispatchers.IO) {
            try {
                val resultado = coleccion
                    .whereEqualTo("productoId", productoId)
                    .get()
                    .await()

                val total = resultado.size()
                if (total == 0) return@withContext Pair(0.0, 0)

                val sumaEstrellas = resultado.documents.sumOf { doc ->
                    (doc.getLong("estrellas") ?: 0L).toInt()
                }

                val promedio = sumaEstrellas.toDouble() / total
                val promedioDosDecimales = Math.round(promedio * 10) / 10.0

                Pair(promedioDosDecimales, total)

            } catch (e: Exception) {
                Pair(0.0, 0)
            }
        }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReseña(): m_Reseña? {
        return try {
            m_Reseña(
                id                = id,
                productoId        = getString("productoId") ?: "",
                productoNombre    = getString("productoNombre") ?: "",
                empleadoId        = getString("empleadoId") ?: "",
                empleadoNombres   = getString("empleadoNombres") ?: "",
                empleadoApellidos = getString("empleadoApellidos") ?: "",
                creditoId         = getString("creditoId") ?: "",
                estrellas         = (getLong("estrellas") ?: 0L).toInt(),
                comentario        = getString("comentario") ?: "",
                fechaReseña       = getTimestamp("fechaReseña")
            )
        } catch (e: Exception) {
            null
        }
    }
}
