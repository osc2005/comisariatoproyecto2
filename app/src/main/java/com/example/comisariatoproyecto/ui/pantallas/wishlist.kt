package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.data.m_WishlistItem
import com.example.comisariatoproyecto.data.r_Wishlist
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val BorderSubtleW = Color(0xFFDDE3EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaWishlist(
    repoWishlist: r_Wishlist,
    onBack: () -> Unit,
    onVerProducto: (String) -> Unit,   // navega al detalle pasando el id del producto
    OnLogout: () -> Unit
) {
    val items  by repoWishlist.obtenerWishlist().collectAsState(initial = emptyList())
    val scope  = rememberCoroutineScope()

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi lista de deseos", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            // ── Estado vacío ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint               = TextSecondary.copy(alpha = 0.4f),
                        modifier           = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Tu lista de deseos está vacía",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Presioná el corazón en cualquier\nproducto para guardarlo aquí.",
                        fontSize   = 13.sp,
                        color      = TextSecondary.copy(alpha = 0.6f),
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            // ── Lista ────────────────────────────────────────────────────────
            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding      = PaddingValues(vertical = 16.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    WishlistCard(
                        item     = item,
                        onClick  = { onVerProducto(item.id) },
                        onQuitar = {
                            scope.launch { repoWishlist.quitar(item.id) }
                        }
                    )
                }
            }
        }
    }
}

// ── Card individual ───────────────────────────────────────────────────────────
@Composable
private fun WishlistCard(
    item: m_WishlistItem,
    onClick: () -> Unit,
    onQuitar: () -> Unit
) {
    fun formatLps(valor: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("es", "HN"))
        fmt.minimumFractionDigits = 2
        fmt.maximumFractionDigits = 2
        return "L. ${fmt.format(valor)}"
    }
    fun formatearFecha(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", Locale("es", "HN"))
        return formatter.format(date)
    }



    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, BorderSubtleW)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEF1F8))
            ) {

            }

            // Info
            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp) // Espacio pequeño entre textos
            ) {
                // Título del Producto
                Text(
                    text = item.nombre,
                    fontSize = 15.sp, // Un poco más grande para resaltar
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    maxLines = 1, // Evita que empuje la fecha hacia abajo si el nombre es largo
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // Fecha (El diseño "bonito" abajito)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agregado el: ",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = formatearFecha(item.fechaAgregado),
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            // Botón quitar corazón
            IconButton(
                onClick  = onQuitar,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector        = Icons.Filled.Favorite,
                    contentDescription = "Quitar de wishlist",
                    tint               = Color.Red,
                    modifier           = Modifier.size(22.dp)
                )
            }
        }
    }
}



