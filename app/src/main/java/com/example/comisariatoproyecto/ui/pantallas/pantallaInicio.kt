package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary

@Composable
fun MenuInferiorComisariato(
    items: List<String>,
    itemSeleccionado: String = "Inicio",
    onItemClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            color     = NavyPrimary.copy(alpha = 0.1f),
            thickness = 0.5.dp
        )
        Surface(
            modifier        = Modifier.fillMaxWidth(),
            color           = SurfaceWhite,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                // ✅ Usa la lista recibida — no hardcodeado
                items.forEach { item ->
                    ItemMenu(
                        titulo      = item,
                        seleccionado = itemSeleccionado == item,
                        onItemClick  = onItemClick
                    ) {
                        when (item) {
                            "Inicio"     -> Icon(Icons.Outlined.Home,         contentDescription = null)
                            "Catálogo"   -> Icon(Icons.Outlined.Inventory2,   contentDescription = null)
                            "Mi Crédito" -> Icon(Icons.Outlined.CreditCard,   contentDescription = null)
                            "Perfil"     -> Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                        }
                    }
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
    val color = if (seleccionado) NavyPrimary else TextSecondary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onItemClick(titulo) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            Box(
                modifier         = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                icono()
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text       = titulo,
            color      = color,
            fontSize   = 11.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
        )
    }
}