package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

data class Empleado(
    val apellidos: String = "",
    val codigoEmpleado: String = "",
    val correo: String = "",
    val departamentoId: String = "",

    @get:PropertyName("departamentoNombre")
    @set:PropertyName("departamentoNombre")
    var departamentoNombre: String = "",

    val dni: String = "",
    val estado: String = "",
    val fechaRegistro: Timestamp? = null,
    val nombres: String = "",
    val salario: Long = 0, // 🔥 CORREGIDO (antes String)
    val telefono: String = ""
) {
    val nombreCompleto: String
        get() = "$nombres $apellidos".trim()

    val salarioFormateado: String
        get() = "L. %,d".format(salario)

    val fechaFormateada: String
        get() = fechaRegistro?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "HN")).format(it)
        } ?: "Sin fecha"

    val estaActivo: Boolean
        get() = estado.lowercase() == "activo"
}