package com.example.comisariatoproyecto.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

    class r_CuotaCredito(
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    ) {
        fun obtenerCuotas(): Flow<List<m_CuotaCredito>> = callbackFlow {
            val listener = db
                .collection("configuracion")
                .document("creditoComisariato")
                .collection("cuotas")
                .whereEqualTo("estado", true)   // solo activas
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val lista = snapshot.documents
                        .mapNotNull { doc ->
                            val meses = doc.id.toIntOrNull() ?: return@mapNotNull null
                            m_CuotaCredito(id = doc.id, estado = true)
                        }
                        .sortedBy { it.id.toInt() }   // orden: 3, 6, 9, 12...
                    trySend(lista)
                }
            awaitClose { listener.remove() }
        }
    }
