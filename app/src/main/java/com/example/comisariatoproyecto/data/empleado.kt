package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp

data class Empleado(
    val apellidos: String          = "",
    val codigoEmpleado: String     = "",
    val correo: String             = "",
    val departamentoId: String     = "",
    val departamentoNombre: String = "",
    val dni: String                = "",
    val estado: String             = "",
    val fechaRegistro: Timestamp?  = null,
    val nombres: String            = "",
    val salario: Double = 0.0, // Any? evita crash si Firestore lo guarda como String o Number
    val telefono: String           = ""
)

{
    // Nombre completo listo para mostrar en UI
    val nombreCompleto: String
        get() = "$nombres $apellidos".trim()

    // Salario formateado sin importar el tipo que tenga en Firestore
    val salarioFormateado: String
        get() = when (val s = salario) {
            is Number -> "L. %.2f".format(s.toDouble())
            is String -> s.toDoubleOrNull()?.let { "L. %.2f".format(it) } ?: s
            else      -> "No disponible"
        }

    // Fecha legible
    val fechaFormateada: String
        get() = fechaRegistro?.toDate()?.let {
            java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale("es", "HN")
            ).format(it)
        } ?: "Sin fecha"

    val estaActivo: Boolean
        get() = estado.lowercase() == "activo"
}
