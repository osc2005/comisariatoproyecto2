package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp

// Modelo propio de la pantalla Mi Crédito — no modifica m_Creditos existente
// Mapea todos los campos que guarda r_Creditos.crearReserva() en Firestore
// colección: creditos
data class m_CreditoDetalle(
    val id: String = "",
    val productoId: String = "",
    val productoNombre: String = "",       // campo: productoNombre
    val productoImgUrl: String = "",       // campo: productoImgUrl
    val cantidad: Int = 0,                 // campo: cantidad
    val estado: String = "",               // campo: estado → "Pendiente"|"Aprobado"|"Rechazado"|"Cancelado"
    val estadoCredito: String = "",        // campo: estadoCredito → "Activo"|"Inactivo"
    val cuotasPagadas: Int = 0,            // campo: cuotasPagadas
    val saldoPendiente: Double = 0.0,      // campo: saldoPendiente
    val fechaRegistro: Timestamp? = null,  // campo: fechaRegistro
    val fechaAutoriza: Timestamp? = null,  // campo: fechaAutoriza
    // datosFinancierosHistoricos (mapa anidado en Firestore)
    val cuotaMensual: Double = 0.0,        // datosFinancierosHistoricos.cuotaMensual
    val plazoCuotas: Int = 0,              // datosFinancierosHistoricos.plazoCuotas
    val totalCredito: Double = 0.0         // datosFinancierosHistoricos.totalCredito
)