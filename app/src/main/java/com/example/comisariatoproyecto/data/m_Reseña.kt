package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp

data class m_Reseña(
    val id: String = "",
    val productoId: String = "",
    val productoNombre: String = "",
    val empleadoId: String = "",
    val empleadoNombres: String = "",
    val empleadoApellidos: String = "",
    val creditoId: String = "",
    val estrellas: Int = 0,
    val comentario: String = "",
    val visible: Boolean = true,
    val fechaReseña: Timestamp? = null
)