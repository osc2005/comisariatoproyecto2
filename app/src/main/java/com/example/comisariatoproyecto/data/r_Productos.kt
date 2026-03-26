package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose

class r_Productos {

    private  val db = FirebaseFirestore.getInstance()

    fun obtenerProductosPorCategoria(categoriaId: String): Flow<List<m_Productos>> = callbackFlow {
        val listener = db.collection("productos")
            .whereEqualTo("categoriaId", categoriaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val productos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(m_Productos::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(productos)
            }
        awaitClose { listener.remove() }
    }
}