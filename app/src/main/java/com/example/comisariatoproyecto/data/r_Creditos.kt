package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class r_Creditos {

    private val db = FirebaseFirestore.getInstance()

    suspend fun crearReserva(
        producto: m_Productos,
        cantidad: Int,
        plazoMeses: Int?,
        empleado: Empleado
    ) {
        val configSnap = db.collection("configuracion")
            .document("creditoComisariato")
            .get().await()
        val porcentajeLimite = configSnap.getDouble("porcentajeLimite")
            ?: throw Exception("Error: No se encontró la configuración 'porcentajeLimite' en Firebase.")

        val esCredito = plazoMeses != null
        val precioBase = if (esCredito) producto.precioCredito else producto.precioContado
        val totalCredito = precioBase * cantidad
        val cuotaMensual = if (esCredito && plazoMeses!! > 0) totalCredito / plazoMeses else 0.0

        val productoRef = db.collection("productos").document(producto.id)

        val nuevoCredito = hashMapOf(
            "empleadoId" to empleado.codigoEmpleado,
            "empleadoNombres" to empleado.nombres,
            "empleadoApellidos" to empleado.apellidos,
            "empleadoAutoriza" to "",
            "productoId" to producto.id,
            "productoNombre" to producto.nombre,
            "productoImgUrl" to producto.imagenUrl,
            "cantidad" to cantidad,
            "estado" to "Pendiente",
            "estadoCredito" to "Inactivo",
            "cuotasPagadas" to 0,
            "saldoPendiente" to totalCredito,
            "mesCobro" to "",
            "fechaRegistro" to Timestamp.now(),
            "fechaAutoriza" to null,
            "datosFinancierosHistoricos" to hashMapOf(
                "salarioNetoAlMomento" to empleado.salario.toDouble(),
                "porcentajeLimiteAplicado" to porcentajeLimite,
                "plazoCuotas" to (plazoMeses ?: 1),
                "cuotaMensual" to cuotaMensual,
                "totalCredito" to totalCredito
            )
        )

        db.runTransaction { tx ->
            val snapshot = tx.get(productoRef)
            val stockActual = snapshot.getLong("stock")?.toInt() ?: 0
            val stockMinimo = snapshot.getLong("stockMinimo")?.toInt() ?: 0
            val disponibleParaVenta = stockActual - stockMinimo

            if (cantidad > disponibleParaVenta) {
                throw Exception("Solo hay $disponibleParaVenta unidades disponibles.")
            }

            tx.set(db.collection("creditos").document(), nuevoCredito)
        }.await()
    }

    suspend fun obtenerCreditoUtilizadoReal(empleadoId: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val snap = db.collection("creditos")
                    .whereEqualTo("empleadoId", empleadoId)
                    .whereEqualTo("estadoCredito", "Activo")
                    .get()
                    .await()

                snap.documents.sumOf {
                    it.getDouble("datosFinancierosHistoricos.cuotaMensual") ?: 0.0
                }
            } catch (e: Exception) {
                0.0
            }
        }
    }

    suspend fun obtenerConfiguracionCredito(): Double {
        return withContext(Dispatchers.IO) {
            try {
                val doc = db.collection("configuracion")
                    .document("creditoComisariato")
                    .get()
                    .await()

                doc.getDouble("porcentajeLimite")
                    ?: throw Exception("Configuración de límite no encontrada")
            } catch (e: Exception) {
                Log.e("r_permisos", "Error al obtener config: ${e.message}")
                0.0
            }
        }
    }

    suspend fun obtenerConteoReservas(empleadoId: String): Pair<Int, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val snap = db.collection("creditos")
                    .whereEqualTo("empleadoId", empleadoId)
                    .get()
                    .await()

                val pendientes = snap.documents.count { it.getString("estado") == "Pendiente" }
                val activas = snap.documents.count { it.getString("estadoCredito") == "Activo" }

                Pair(pendientes, activas)
            } catch (e: Exception) {
                Pair(0, 0)
            }
        }
    }

    suspend fun obtenerReservasDeEmpleado(empleadoId: String): List<m_Creditos> {
        return db.collection("creditos")
            .whereEqualTo("empleadoId", empleadoId)
            .get()
            .await()
            .documents.map { doc ->
                m_Creditos(
                    id             = doc.id,
                    estado         = doc.getString("estado") ?: "",
                    productoId     = doc.getString("productoId") ?: "",
                    productoNombre = doc.getString("productoNombre") ?: "",  // ← agregar
                    productoImgUrl = doc.getString("productoImgUrl") ?: ""   // ← agregar
                )
            }
    }

    suspend fun cancelarReserva(reservaId: String) {
        db.collection("creditos")
            .document(reservaId)
            .update("estado", "Cancelado", "estadoCredito", "Inactivo")
            .await()
    }

    fun obtenerReservasDetalladas(empleadoId: String): Flow<List<m_CreditoDetalle>> =
        callbackFlow {
            val listener = db.collection("creditos")
                .whereEqualTo("empleadoId", empleadoId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val lista = snapshot.documents.mapNotNull { doc ->
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val datos = doc.get("datosFinancierosHistoricos") as? Map<String, Any>

                            m_CreditoDetalle(
                                id             = doc.id,
                                productoId     = doc.getString("productoId") ?: "",
                                productoNombre = doc.getString("productoNombre") ?: "",
                                productoImgUrl = doc.getString("productoImgUrl") ?: "",
                                cantidad       = doc.getLong("cantidad")?.toInt() ?: 0,
                                estado         = doc.getString("estado") ?: "",
                                estadoCredito  = doc.getString("estadoCredito") ?: "",
                                cuotasPagadas  = doc.getLong("cuotasPagadas")?.toInt() ?: 0,
                                saldoPendiente = doc.getDouble("saldoPendiente") ?: 0.0,
                                fechaRegistro  = doc.getTimestamp("fechaRegistro"),
                                fechaAutoriza  = doc.getTimestamp("fechaAutoriza"),
                                cuotaMensual   = (datos?.get("cuotaMensual") as? Double) ?: 0.0,
                                plazoCuotas    = (datos?.get("plazoCuotas") as? Long)?.toInt() ?: 0,
                                totalCredito   = (datos?.get("totalCredito") as? Double) ?: 0.0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    trySend(lista)
                }

            awaitClose { listener.remove() }
        }

    // ─── NUEVA: obtener reservas Aprobadas o Canceladas para notificaciones ──
    suspend fun obtenerReservasParaNotificar(empleadoId: String): List<NotificacionReserva> {
        return withContext(Dispatchers.IO) {
            try {
                val snap = db.collection("creditos")
                    .whereEqualTo("empleadoId", empleadoId)
                    .get()
                    .await()

                snap.documents.mapNotNull { doc ->
                    val estado = doc.getString("estado") ?: return@mapNotNull null
                 if (estado != "Aprobado" && estado != "Rechazado") return@mapNotNull null

                    NotificacionReserva(
                        id             = doc.id,
                        productoNombre = doc.getString("productoNombre") ?: "Producto",
                        estado         = estado,
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

// ─── Modelo simple para notificaciones in-app ────────────────────────────────
data class NotificacionReserva(
    val id: String,
    val productoNombre: String,
    val estado: String   // "Aprobado" o "Cancelado"
)