package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class Usuario(
    val correo: String = "",
    val correoPersonal: String = "",
    val empleadoId: String = "",
    val estado: String = "",
    val fechaRegistro: Timestamp? = null,
    val nombre: String = "",
    val rolNombre: String = "",
    val rolId: String = "", // corregido
    val ultima_modificacion: Timestamp? = null
) {
    val estaActivo: Boolean
        get() = estado.lowercase() == "activo"

    val fechaRegistroFormateada: String
        get() = fechaRegistro?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "HN")).format(it)
        } ?: "Sin fecha"
}