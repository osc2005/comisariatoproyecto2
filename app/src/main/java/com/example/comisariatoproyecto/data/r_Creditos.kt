package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


    import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        // Transacción atómica: verificar stock disponible y crear documento
        db.runTransaction { tx ->
            val snapshot = tx.get(productoRef)
            val stockActual = snapshot.getLong("stock")?.toInt() ?: 0
            val stockMinimo = snapshot.getLong("stockMinimo")?.toInt() ?: 0
            val disponibleParaVenta = stockActual - stockMinimo

            if (cantidad > disponibleParaVenta) {
                throw Exception("Solo hay $disponibleParaVenta unidades disponibles.")
            }

            // Hay stock suficiente — crear la reserva
            // El trigger se encarga de restar el stock automáticamente
            tx.set(db.collection("creditos").document(), nuevoCredito)
        }.await()
    }

// ─── OBTENER CREDITO UTILIZADO REAL ───────────────────────────────────────────
//suma las cuotas mensuales de los creditos activos que tiene el empleado conectado
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

    // ─── OBTENER CONFIGURACIÓN DEL CRÉDITO ───────────────────────────────────────
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

    //obtener reservas del empleado conectado
    suspend fun obtenerReservasDeEmpleado(empleadoId: String): List<m_Creditos> {
        return db.collection("creditos")
            .whereEqualTo("empleadoId", empleadoId)
            .get()
            .await()
            .documents.map { doc ->
                m_Creditos(
                    id = doc.id,
                    estado = doc.getString("estado") ?: "",
                    productoId = doc.getString("productoId") ?: ""
                )
            }
    }


    //cancelar reserva de lado del cliente
    suspend fun cancelarReserva(reservaId: String) {
        db.collection("creditos")
            .document(reservaId)
            .update("estado", "Cancelado", "estadoCredito", "Inactivo")
            .await()
    }

}
