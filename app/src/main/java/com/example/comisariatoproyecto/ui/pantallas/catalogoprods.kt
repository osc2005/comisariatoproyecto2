package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.data.m_Categoria
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Categoria
import com.example.comisariatoproyecto.data.r_Productos

// ── Paleta consistente con PantallaInicio ───────────────────────────────────
private val Cafe      = Color(0xFF8B5A2B)
private val Crema     = Color(0xFFFFE4C4)
private val FondoGris = Color(0xFFF5F0EB)
private val Blanco    = Color.White
private val NegroSuave = Color(0xFF111827)
private val GrisTexto  = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosCatalogo(
    repoCategoria: r_Categoria,
    repoProducto: r_Productos,
    onBack: () -> Unit,
    onVerProductos: (m_Categoria) -> Unit,
    onVerDetalleProducto: (m_Productos) -> Unit          // ← sin cambios
) {
    val categorias        by repoCategoria.obtenerCategorias().collectAsState(initial = null)
    val todosLosProductos by repoProducto.obtenerProductos().collectAsState(initial = emptyList())

    var textoBusqueda      by remember { mutableStateOf("") }
    var expandirSugerencias by remember { mutableStateOf(false) }

    val sugerencias = remember(textoBusqueda, todosLosProductos) {
        if (textoBusqueda.isBlank()) emptyList()
        else todosLosProductos.filter {
            it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    it.descripcion.contains(textoBusqueda, ignoreCase = true)
        }.take(8)
    }

    // Sin Scaffold propio — usamos Column directa para controlar el header
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
    ) {

        // ── HEADER (igual al de PantallaInicio) ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Cafe)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text = "Catálogo",
                    color = Blanco,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Explora nuestros productos y categorías",
                    color = Blanco.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }

        // ── CONTENIDO ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Barra de búsqueda predictiva ────────────────────────────────
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
                        Icon(Icons.Default.Search, contentDescription = null, tint = Cafe)
                    },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = {
                                textoBusqueda = ""
                                expandirSugerencias = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = GrisTexto)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cafe,
                        unfocusedBorderColor = Color(0xFFD1C4B8),
                        focusedContainerColor = Blanco,
                        unfocusedContainerColor = Blanco
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Sugerencias del dropdown
                if (sugerencias.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expandirSugerencias,
                        onDismissRequest = { expandirSugerencias = false }
                    ) {
                        sugerencias.forEach { producto ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = producto.nombre,
                                            fontWeight = FontWeight.Bold,
                                            color = NegroSuave
                                        )
                                        Text(
                                            text = "${producto.categoriaNombre}  •  L. ${producto.precioContado}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GrisTexto
                                        )
                                    }
                                },
                                onClick = {
                                    textoBusqueda = producto.nombre
                                    expandirSugerencias = false
                                    onVerDetalleProducto(producto)   // ← sin cambios
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Separador con label ─────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD1C4B8))
                Text(
                    text = "  Categorías  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrisTexto,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD1C4B8))
            }

            Spacer(Modifier.height(14.dp))

            // ── Grid 2×2 de categorías ──────────────────────────────────────
            when {
                categorias == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Cafe)
                    }
                }

                categorias!!.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay categorías registradas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrisTexto
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
                                onClick = { onVerProductos(categoria) }   // ← sin cambios
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Tarjeta individual de categoría ─────────────────────────────────────────
@Composable
fun TarjetaCategoria(
    categoria: m_Categoria,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Franja de color en la parte superior (igual que el box de emoji en TarjetaProducto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Cafe)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = categoria.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NegroSuave,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}