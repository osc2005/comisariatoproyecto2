package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PantallaDetalleProducto(
    productoId: String,
    productoNombre: String
) {
    // TODO: implementar diseño de la pantalla de detalle
    // Datos disponibles:
    //   - productoId    → usar para cargar el documento completo desde Firestore
    //   - productoNombre → disponible directo para mostrar en el título

    // Placeholder para que compile y navegue sin errores
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Detalle de: $productoNombre")
    }
}