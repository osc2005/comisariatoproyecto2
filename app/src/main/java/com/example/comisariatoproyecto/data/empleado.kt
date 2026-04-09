package com.example.comisariatoproyecto.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Modelo de datos que representa a un empleado dentro del sistema.
 *
 * Esta clase está diseñada para mapear directamente documentos de Firebase Firestore.
 * Incluye propiedades básicas del empleado y propiedades calculadas para facilitar su uso en UI.
 */
data class Empleado(

    /** Apellidos del empleado */
    val apellidos: String = "",

    /** Código único del empleado */
    val codigoEmpleado: String = "",

    /** Correo electrónico del empleado */
    val correo: String = "",

    /** ID del departamento al que pertenece */
    val departamentoId: String = "",

    /**
     * Nombre del departamento.
     * Se usa PropertyName para asegurar compatibilidad con Firestore.
     */
    @get:PropertyName("departamentoNombre")
    @set:PropertyName("departamentoNombre")
    var departamentoNombre: String = "",

    /** Documento Nacional de Identidad */
    val dni: String = "",

    /** Estado del empleado (ej: "activo", "inactivo") */
    val estado: String = "",

    /** Fecha de inicio en la empresa */
    val fechaInicio: Timestamp? = null,

    /** Fecha de registro en Firestore */
    val fechaRegistro: Timestamp? = null,

    /** Nombres del empleado */
    val nombres: String = "",



    /**
     * Salario del empleado.
     * 🔥 Se maneja como Long para facilitar cálculos numéricos.
     */
    val salario: Long = 0,

    /** Número de teléfono */
    val telefono: String = ""
) {

    /**
     * Nombre completo del empleado.
     * Combina nombres y apellidos.
     */
    val nombreCompleto: String
        get() = "$nombres $apellidos".trim()

    /**
     * Salario formateado para visualización.
     * Ejemplo: L. 12,000
     */
    val salarioFormateado: String
        get() = "L. %,d".format(salario)

    /**
     * Fecha de registro formateada.
     * Formato: dd/MM/yyyy (configuración Honduras).
     * Si no hay fecha, retorna "Sin fecha".
     */
    val fechaFormateada: String
        get() = fechaInicio?.toDate()?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale("es", "HN")).format(it)
        } ?: "Sin fecha"


    /**
     * Indica si el empleado está activo.
     * Se evalúa en base al campo "estado".
     */
    val estaActivo: Boolean
        get() = estado.lowercase() == "activo"
}