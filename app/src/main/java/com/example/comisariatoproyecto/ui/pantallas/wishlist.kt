package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.comisariatoproyecto.data.m_WishlistItem
import com.example.comisariatoproyecto.data.r_Wishlist
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaWishlist(
    repoWishlist: r_Wishlist,
    onBack: () -> Unit,
    onVerProducto: (String) -> Unit,
    OnLogout: () -> Unit
) {
    val items by repoWishlist.obtenerWishlist().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mi lista de deseos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (items.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = NavyPrimary.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    "${items.size} items",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = NavyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(NavyPrimary.copy(alpha = 0.06f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = NavyPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        "Tu lista está vacía",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        "Presioná el corazón en cualquier\nproducto para guardarlo aquí.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(items, key = { _, it -> it.id }) { _, item ->
                    WishlistCard(
                        item = item,
                        onVerProducto = { onVerProducto(item.id) },
                        onQuitar = { scope.launch { repoWishlist.quitar(item.id) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun WishlistCard(
    item: m_WishlistItem,
    onVerProducto: () -> Unit,
    onQuitar: () -> Unit
) {
    val fmt = NumberFormat.getNumberInstance(Locale("es", "HN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val fechaFmt = SimpleDateFormat("dd/MM/yyyy", Locale("es", "HN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, NavyPrimary.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Fila principal ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Imagen
                AsyncImage(
                    model = item.imagenUrl,
                    contentDescription = item.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(78.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceBase)
                )

                // Info
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.nombre,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Agregado el ${fechaFmt.format(item.fechaAgregado.toDate())}",
                                fontSize = 11.sp,
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                        // Botón quitar (corazón)
                        IconButton(
                            onClick = onQuitar,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "Quitar",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Precio
                    Text(
                        "L. ${fmt.format(item.precioContado)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
            }

            // ── Botones de acción ────────────────────────────────────────
            HorizontalDivider(color = NavyPrimary.copy(alpha = 0.06f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onVerProducto,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, NavyPrimary.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Ver producto", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

            }
        }
    }
}