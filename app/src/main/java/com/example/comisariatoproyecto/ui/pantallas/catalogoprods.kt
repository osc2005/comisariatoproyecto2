package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.comisariatoproyecto.data.m_Categoria
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Categoria
import com.example.comisariatoproyecto.data.r_Productos
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosCatalogo(
    repoCategoria: r_Categoria,
    repoProducto: r_Productos,
    onBack: () -> Unit,
    onVerProductos: (m_Categoria) -> Unit,
    onVerDetalleProducto: (m_Productos) -> Unit
) {
    val categorias        by repoCategoria.obtenerCategorias().collectAsState(initial = null)
    val todosLosProductos by repoProducto.obtenerProductos().collectAsState(initial = emptyList())

    var textoBusqueda       by remember { mutableStateOf("") }
    var expandirSugerencias by remember { mutableStateOf(false) }

    val sugerencias = remember(textoBusqueda, todosLosProductos) {
        if (textoBusqueda.isBlank()) emptyList()
        else todosLosProductos.filter {
            val tieneStock = it.stock > it.stockMinimo
            tieneStock && (it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    it.descripcion.contains(textoBusqueda, ignoreCase = true))
        }.take(8)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        //  HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyPrimary)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text = "Catálogo",
                    color = SurfaceWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Explora nuestros productos y categorías",
                    color = SurfaceWhite.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }

        //  CONTENIDO
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            //  Barra de búsqueda predictiva
            ExposedDropdownMenuBox(
                expanded = expandirSugerencias && sugerencias.isNotEmpty(),
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = {
                        textoBusqueda = it
                        expandirSugerencias = true
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = { Text("Buscar un producto específico...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary)
                    },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = {
                                textoBusqueda = ""
                                expandirSugerencias = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor     = NavyPrimary,
                        unfocusedBorderColor   = NavyPrimary.copy(alpha = 0.2f),
                        focusedContainerColor   = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (sugerencias.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandirSugerencias,
                        onDismissRequest = { expandirSugerencias = false },
                        modifier = Modifier.background(SurfaceWhite)
                    ) {
                        sugerencias.forEach { producto ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = producto.nombre,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "${producto.categoriaNombre}  •  L. ${producto.precioContado}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    textoBusqueda = producto.nombre
                                    expandirSugerencias = false
                                    onVerDetalleProducto(producto)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            //  Separador con label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = NavyPrimary.copy(alpha = 0.1f))
                Text(
                    text = "  Categorías  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = NavyPrimary.copy(alpha = 0.1f))
            }

            Spacer(Modifier.height(14.dp))

            //  Grid 2×2 de categorías
            when {
                categorias == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                }
                categorias!!.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay categorías registradas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(categorias!!, key = { it.id }) { categoria ->
                            TarjetaCategoria(
                                categoria = categoria,
                                onClick   = { onVerProductos(categoria) }
                            )
                        }
                    }
                }
            }
        }
    }
}

//  Tarjeta de categoría con imagen de fondo y gradiente negro suave
@Composable
fun TarjetaCategoria(
    categoria: m_Categoria,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        label = "scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        interactionSource = remember { MutableInteractionSource() }.also { source ->
            LaunchedEffect(source) {
                source.interactions.collect { interaction ->
                    pressed = interaction is PressInteraction.Press
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Fondo de color fallback si la imagen no carga o imagenUrl esta vacio
            // Se ve NavyPrimary con gradiente y texto blanco
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NavyPrimary.copy(alpha = 0.85f))
            )

            // Imagen de fondo desde Firebase Storage
            // colección: categoria, campo: imagenUrl
            // ifBlank { null } hace que Coil no intente cargar si el campo esta vacio
            AsyncImage(
                model              = categoria.imagenUrl.ifBlank { null },
                contentDescription = categoria.nombre,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )

            // Gradiente negro suave de abajo hacia arriba para que el texto sea legible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            // Nombre de la categoría — abajo a la izquierda, en blanco
            Text(
                text       = categoria.nombre,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                color      = Color.White,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}