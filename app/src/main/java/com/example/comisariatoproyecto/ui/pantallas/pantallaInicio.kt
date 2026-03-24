package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val cafeActivo = Color(0xFF8B5A2B)
private val GrisInactivo = Color(0xFF6B7280)
private val FondoMenu = Color(0xFFF7F7F7)
private val RojoLinea = Color(0xFF8B0000)

@Composable
fun MenuInferiorComisariato(
    itemSeleccionado: String = "Inicio",
    onItemClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // 🔴 Línea roja
        Spacer(
            modifier = Modifier
                .fillMaxWidth()

                .background(RojoLinea)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FondoMenu,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,

            ) {

                ItemMenu("Inicio", itemSeleccionado == "Inicio", onItemClick) {
                    Icon(Icons.Outlined.Home, contentDescription = null)
                }

                ItemMenu("Catálogo", itemSeleccionado == "Catálogo", onItemClick) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null)
                }

                ItemMenu("Mi Crédito", itemSeleccionado == "Mi Crédito", onItemClick) {
                    Icon(Icons.Outlined.CreditCard, contentDescription = null)
                }

                ItemMenu("Perfil", itemSeleccionado == "Perfil", onItemClick) {
                    Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun ItemMenu(
    titulo: String,
    seleccionado: Boolean,
    onItemClick: (String) -> Unit,
    icono: @Composable () -> Unit
) {
    val color = if (seleccionado) cafeActivo else GrisInactivo

    Column(
        modifier = Modifier
            .clickable { onItemClick(titulo) } // 🔥 CORREGIDO
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        CompositionLocalProvider(LocalContentColor provides color) {
            Box(
                modifier = Modifier.size(26.dp),
                contentAlignment = Alignment.Center
            ) {
                icono()
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = titulo,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (seleccionado) FontWeight.Medium else FontWeight.Normal
        )
    }
}