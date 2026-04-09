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
import com.example.comisariatoproyecto.data.r_Reseñas
import com.example.comisariatoproyecto.ui.componentes.RatingBar



import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Star
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
    repoReseñas: r_Reseñas, // ← nuevo
    onBack: () -> Unit,
    onVerDetalle: (m_Productos) -> Unit
) {
    val productos by repo
        .obtenerProductosPorCategoria(categoria.id)
        .collectAsState(initial = null)

    var ordenPrecio by remember { mutableStateOf("ninguno") }
    var mejorValorados by remember { mutableStateOf(false) }
    var buscar by remember { mutableStateOf("") }

    // Estado para guardar los promedios de todos los productos mostrados
    val promedios = remember { mutableStateMapOf<String, Pair<Double, Int>>() }

    // Cargar promedios masivamente cuando cambian los productos
    LaunchedEffect(productos) {
        productos?.let { lista ->
            val ids = lista.map { it.id }
            if (ids.isNotEmpty()) {
                val stats = repoReseñas.obtenerEstadisticasVariosProductos(ids)
                promedios.putAll(stats)
            }
        }
    }

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
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = ordenPrecio == "menor_mayor",
                    onClick = { ordenPrecio = if (ordenPrecio == "menor_mayor") "ninguno" else "menor_mayor" },
                    label = { Text("Precio más bajo", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = ordenPrecio == "mayor_menor",
                    onClick = { ordenPrecio = if (ordenPrecio == "mayor_menor") "ninguno" else "mayor_menor" },
                    label = { Text("Precio más alto", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = mejorValorados,
                    onClick = { mejorValorados = !mejorValorados },
                    label = { Text("Mejor valorados", fontSize = 11.sp) },
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
                    }.let { listaOriginal ->
                        // 1. APLICAR FILTRO DINÁMICO (Embudo)
                        val listaFiltrada = if (mejorValorados) {
                            val maxPuntaje = promedios.values.maxOfOrNull { it.first } ?: 0.0
                            // Si el máximo es alto (4-5), mostramos lo max a  4
                            // Si el máximo es bajo (ej: 3.5), mostramos lo que esté en ese nivel (3+).
                            val umbral = if (maxPuntaje >= 4.0) 4.0 else Math.floor(maxPuntaje)
                            
                            listaOriginal.filter { (promedios[it.id]?.first ?: 0.0) >= umbral && (promedios[it.id]?.second ?: 0) > 0 }
                        } else {
                            listaOriginal
                        }

                        // 2. APLICAR ORDENAMIENTO (Acomodo)
                        listaFiltrada.sortedWith(
                            when (ordenPrecio) {
                                "menor_mayor" -> compareBy { it.precioContado }
                                "mayor_menor" -> compareByDescending { it.precioContado }
                                else -> compareBy { 0 } // Mantener orden original
                            }
                        )
                    }

                    if (filtrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                Icon(
                                    if (mejorValorados) Icons.Default.Star else Icons.Default.Search, 
                                    null, 
                                    modifier = Modifier.size(48.dp), 
                                    tint = Color.LightGray
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = if (mejorValorados) "Aún no hay productos destacados con alta calificación aquí." 
                                           else "No se encontraron resultados para \"$buscar\"",
                                    textAlign = TextAlign.Center, 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
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
                                    promptStats = promedios[producto.id] ?: Pair(0.0, 0),
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
    promptStats: Pair<Double, Int>,
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
                text = producto.nombre,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.5.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(4.dp))

            RatingBar(promedio = promptStats.first, starSize = 12.dp)
            Spacer(Modifier.height(4.dp))

            Text(
                text = "L. " + String.format(java.util.Locale.US, "%,.2f", producto.precioContado),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }
    }
}