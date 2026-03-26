package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose

class r_Categoria {

    private  val db = FirebaseFirestore.getInstance()

    fun obtenerCategorias(): Flow<List<m_Categoria>> = callbackFlow {
        val listener = db.collection("categoria")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val categorias = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(m_Categoria::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(categorias)
            }
        awaitClose { listener.remove() }
    }

}