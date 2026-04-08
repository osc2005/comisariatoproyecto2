package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp

data class m_WishlistItem(
    val id: String = "",              // mismo ID que el producto
    val nombre: String = "",
    val fechaAgregado: Timestamp = Timestamp.now()
)
