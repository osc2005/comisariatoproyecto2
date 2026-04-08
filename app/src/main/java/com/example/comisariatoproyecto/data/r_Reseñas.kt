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
        visible: Boolean,
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
                "visible"           to true,
                "comentario"        to comentario,
                "fechaReseña"       to Timestamp.now()
            )

            coleccion.add(nuevaReseña).await()

        } catch (e: Exception) {
            throw e
        }
    }

    fun obtenerReseñaDeProductoPorEmpleado(productoId: String, empleadoId: String): Flow<m_Reseña?> =
        callbackFlow {
            val listener = coleccion
                .whereEqualTo("productoId", productoId)
                .whereEqualTo("empleadoId", empleadoId)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(null); return@addSnapshotListener }
                    trySend(snapshot?.documents?.firstOrNull()?.toReseña())
                }
            awaitClose { listener.remove() }
        }

    //  2. Reseñas de un producto en tiempo real
    fun obtenerReseñasDeProducto(productoId: String): Flow<List<m_Reseña>> =
        callbackFlow {
            val listener = coleccion
                .whereEqualTo("productoId", productoId)
                .whereEqualTo("visible", true) //
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
    // 6. Promedio y conteo de reseñas de un producto (Solo visibles)
    suspend fun obtenerEstadisticasVariosProductos(productoIds: List<String>): Map<String, Pair<Double, Int>> =
        withContext(Dispatchers.IO) {
            try {
                if (productoIds.isEmpty()) return@withContext emptyMap()
                
                // Firestore limit for whereIn is 30. Chunking if necessary.
                val chunks = productoIds.chunked(30)
                val resultadosMap = mutableMapOf<String, Pair<Double, Int>>()

                for (chunk in chunks) {
                    val resultado = coleccion
                        .whereIn("productoId", chunk)
                        .whereEqualTo("visible", true)
                        .get()
                        .await()

                    val reviewsByProduct = resultado.documents.groupBy { it.getString("productoId") ?: "" }
                    
                    for (prodId in chunk) {
                        val docs = reviewsByProduct[prodId] ?: emptyList()
                        val total = docs.size
                        if (total == 0) {
                            resultadosMap[prodId] = Pair(0.0, 0)
                        } else {
                            val suma = docs.sumOf { (it.getLong("estrellas") ?: 0L).toInt() }
                            val promedio = Math.round((suma.toDouble() / total) * 10) / 10.0
                            resultadosMap[prodId] = Pair(promedio, total)
                        }
                    }
                }
                resultadosMap
            } catch (e: Exception) {
                emptyMap()
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
                visible           = getBoolean("visible") ?: true,
                comentario        = getString("comentario") ?: "",
                fechaReseña       = getTimestamp("fechaReseña")
            )
        } catch (e: Exception) {
            null
        }
    }
}
