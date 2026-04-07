package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.comisariatoproyecto.data.m_Categoria
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Productos



import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProductos(
    categoria: m_Categoria,
    repo: r_Productos,
    onBack: () -> Unit,
    onVerDetalle: (m_Productos) -> Unit
) {
    val productos by repo
        .obtenerProductosPorCategoria(categoria.id)
        .collectAsState(initial = null)

    var orden by remember { mutableStateOf("ninguno") }
    var buscar by remember { mutableStateOf("") }

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = NavyPrimary,
                    navigationIconContentColor = NavyPrimary
                ),
                title = {
                    Column {
                        Text(
                            text = categoria.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Productos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            OutlinedTextField(
                value = buscar,
                onValueChange = { buscar = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(SurfaceWhite, RoundedCornerShape(12.dp)),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    disabledContainerColor = SurfaceWhite,
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = orden == "menor_mayor",
                    onClick = { orden = if (orden == "menor_mayor") "ninguno" else "menor_mayor" },
                    label = { Text("Precio más bajo", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = orden == "mayor_menor",
                    onClick = { orden = if (orden == "mayor_menor") "ninguno" else "mayor_menor" },
                    label = { Text("Precio más alto", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            when {
                productos == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                }

                productos!!.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No hay productos en esta categoría.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    val filtrados = productos!!.filter {
                        it.nombre.contains(buscar, ignoreCase = true)
                    }.let { lista ->
                        when (orden) {
                            "menor_mayor" -> lista.sortedBy { it.precioContado }
                            "mayor_menor" -> lista.sortedByDescending { it.precioContado }
                            else -> lista
                        }
                    }

                    if (filtrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Sin resultados para \"$buscar\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Text(
                            text = "${filtrados.size} producto(s)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtrados, key = { it.id }) { producto ->
                                ProductoCard(
                                    producto = producto,
                                    onClick = { onVerDetalle(producto) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoCard(
    producto: m_Productos,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val estaAgotado = producto.stock <= producto.stockMinimo

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (pressed) 0.96f else 1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        interactionSource = remember { MutableInteractionSource() }.also { source ->
            LaunchedEffect(source) {
                source.interactions.collect { interaction ->
                    pressed = interaction is PressInteraction.Press
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp) // Padding interno equilibrado
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceBase),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (estaAgotado) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center

                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "AGOTADO",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = producto.nombre.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,

                letterSpacing = 0.5.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(6.dp))

            Text(
                text = "L. " + String.format(java.util.Locale.US, "%,.2f", producto.precioContado),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }
    }
}