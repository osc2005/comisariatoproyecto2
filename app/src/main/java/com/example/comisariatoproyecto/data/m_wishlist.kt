package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp

data class m_WishlistItem(
    val id: String = "",
    val nombre: String = "",
    val imagenUrl: String = "",          // ← agregar
    val precioContado: Double = 0.0,     // ← agregar
    val precioCredito: Double = 0.0,     // ← agregar
    val categoriaNombre: String = "",    // ← agregar
    val fechaAgregado: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)
