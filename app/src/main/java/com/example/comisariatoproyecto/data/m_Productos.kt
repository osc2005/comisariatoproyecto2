package com.example.comisariatoproyecto.data



data class m_Productos(
    val id: String = "",
    val categoriaId: String = "",
    val categoriaNombre: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val estado: String = "",
    val imagenUrl: String = "",
    val precioContado: Double = 0.0,
    val precioCredito: Double = 0.0,
    val stock: Int = 0,
    val stockMinimo: Int = 0
)

