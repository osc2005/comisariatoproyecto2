package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class r_Wishlist(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "r_Wishlist"
    }

    // ── Obtener el ID real del documento en "usuarios" por correo ─────────────
    // Este es el fix principal — busca por correo, no por UID
    private suspend fun obtenerDocumentoIdUsuario(): String? {
        return try {
            val correo = auth.currentUser?.email?.trim()
            if (correo.isNullOrEmpty()) return null

            val snap = db.collection("usuarios")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (snap.isEmpty) {
                Log.w(TAG, "No se encontró documento de usuario para correo=$correo")
                return null
            }

            val docId = snap.documents.first().id
            Log.d(TAG, "ID real del documento usuario: $docId")
            docId

        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo docId usuario: ${e.message}")
            null
        }
    }

    // ── Referencia correcta a listaDeseos ─────────────────────────────────────
    private suspend fun wishlistRef() = obtenerDocumentoIdUsuario()?.let { docId ->
        db.collection("usuarios").document(docId).collection("listaDeseos")
    }

    // ── Observar en tiempo real si un producto está en la wishlist ────────────
    fun observarEnWishlist(productoId: String): Flow<Boolean> = callbackFlow {
        try {
            val ref = wishlistRef()?.document(productoId)
            if (ref == null) {
                trySend(false)
                close()
                return@callbackFlow
            }
            val listener = ref.addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e(TAG, "Error observando wishlist: ${error.message}")
                    return@addSnapshotListener
                }
                trySend(snap?.exists() == true)
            }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Error en observarEnWishlist: ${e.message}")
            trySend(false)
            close()
        }
    }

    // ── Obtener todos los items de la wishlist como Flow ──────────────────────
    fun obtenerWishlist(): Flow<List<m_WishlistItem>> = callbackFlow {
        try {
            val ref = wishlistRef()
            if (ref == null) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }
            val listener = ref
                .orderBy(
                    "fechaAgregado",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .addSnapshotListener { snap, error ->
                    if (error != null) {
                        Log.e(TAG, "Error obteniendo wishlist: ${error.message}")
                        return@addSnapshotListener
                    }
                    val items = snap?.documents?.mapNotNull { doc ->
                        try {
                            m_WishlistItem(
                                id             = doc.id,
                                nombre         = doc.getString("nombre") ?: "",
                                imagenUrl      = doc.getString("imagenUrl") ?: "",       // ← agregar
                                precioContado  = doc.getDouble("precioContado") ?: 0.0,  // ← agregar
                                precioCredito  = doc.getDouble("precioCredito") ?: 0.0,  // ← agregar
                                categoriaNombre = doc.getString("categoriaNombre") ?: "", // ← agregar
                                fechaAgregado  = doc.getTimestamp("fechaAgregado") ?: Timestamp.now()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error mapeando item wishlist: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Error en obtenerWishlist: ${e.message}")
            trySend(emptyList())
            close()
        }
    }

    // ── Agregar producto a la wishlist ────────────────────────────────────────
    suspend fun agregar(producto: m_Productos) {
        try {
            val ref = wishlistRef()?.document(producto.id) ?: run {
                Log.w(TAG, "No se pudo obtener referencia wishlist para agregar")
                return
            }
            val data = mapOf(
                "nombre"        to producto.nombre,
                "fechaAgregado" to Timestamp.now()
            )
            ref.set(data).await()
            Log.d(TAG, "Producto agregado a wishlist: ${producto.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error agregando a wishlist: ${e.message}")
        }
    }

    // ── Quitar producto de la wishlist ────────────────────────────────────────
    suspend fun quitar(productoId: String) {
        try {
            val ref = wishlistRef()?.document(productoId) ?: return
            ref.delete().await()
            Log.d(TAG, "Producto quitado de wishlist: $productoId")
        } catch (e: Exception) {
            Log.e(TAG, "Error quitando de wishlist: ${e.message}")
        }
    }

    // ── Toggle: si está lo quita, si no está lo agrega ────────────────────────
    suspend fun toggle(producto: m_Productos): Boolean {
        return try {
            val ref = wishlistRef()?.document(producto.id) ?: run {
                Log.w(TAG, "No se pudo obtener referencia wishlist para toggle")
                return false
            }

            val existe = ref.get().await().exists()
            Log.d(TAG, "Toggle wishlist producto=${producto.id} existe=$existe")

            if (existe) {
                ref.delete().await()
                false
            } else {
                val data = mapOf(
                    "nombre"          to producto.nombre,
                    "imagenUrl"       to producto.imagenUrl,
                    "precioContado"   to producto.precioContado,
                    "precioCredito"   to producto.precioCredito,
                    "categoriaNombre" to producto.categoriaNombre,
                    "fechaAgregado"   to Timestamp.now()
                )
                ref.set(data).await()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en toggle wishlist: ${e.message}")
            false
        }
    }
}