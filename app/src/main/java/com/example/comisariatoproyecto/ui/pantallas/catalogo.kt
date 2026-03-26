package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Misma paleta que PantallaInicio y el login
private val Cafe       = Color(0xFF8B5A2B)
private val Crema      = Color(0xFFFFE4C4)
private val FondoGris  = Color(0xFFF5F0EB)
private val Blanco     = Color.White
private val NegroSuave = Color(0xFF111827)
private val GrisTexto  = Color(0xFF6B7280)
private val GrisBorde  = Color(0xFFE5E7EB)
private val VerdeOk    = Color(0xFF22C55E)
private val RojoAgotado = Color(0xFFEF4444)

//  Modelo de datos local (placeholder)
// TODO: reemplazar con data class real conectada a Firestore
// colección: productos
data class ProductoCatalogo(
    val nombre: String,
    val marca: String,
    val precio: Double,          // campo: precioCredito
    val emoji: String,           // reemplazar con imagenUrl
    val categoria: String,       // campo: categoriaNombre
    val disponible: Boolean      // campo: activo == "Activo" && stock > 0
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PantallaCatalogoPreview() {
    PantallaCatalogo()
}


@Composable
fun PantallaCatalogo() {

    // Estado del filtro de categoría seleccionada
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    // Estado del texto de búsqueda
    var textoBusqueda by remember { mutableStateOf("") }

    // TODO: estas categorías deben venir de Firestore
    // colección: categoria, campo: nombre
    // ejemplo: val categorias = listOf("Todos") + categoriasFirestore.map { it.nombre }
    val categorias = listOf("Todos", "Tecnología", "Hogar", "Vestimenta", "Electrodomésticos", "Entretenimiento")

    // TODO: estos productos deben venir de Firestore
    // colección: productos, filtro: activo == "Activo"
    // campos: nombre, precioCredito, imagenUrl, categoriaNombre, stock
    val todosLosProductos = listOf(
        ProductoCatalogo("Laptop Dell Inspiron 15", "DELL",     12000.0, "💻", "Tecnología",       true),
        ProductoCatalogo("Cafetera Espresso Pro",  "ESPRESSO",  1500.0,  "☕", "Hogar",            false),
        ProductoCatalogo("Sony WH-1000XM4",        "SONY",      8500.0,  "🎧", "Entretenimiento",  true),
        ProductoCatalogo("Apple Watch Series 8",   "APPLE",     11200.0, "⌚", "Tecnología",       true),
        ProductoCatalogo("Microondas Samsung",     "SAMSUNG",   2500.0,  "📦", "Electrodomésticos",true),
        ProductoCatalogo("Tablet Lenovo Tab P12",  "LENOVO",    6800.0,  "📱", "Tecnología",       false),
        ProductoCatalogo("Silla Gamer DXRacer",    "DXRACER",   4500.0,  "🪑", "Hogar",            true),
        ProductoCatalogo("Audífonos JBL Tune",     "JBL",       1200.0,  "🎵", "Entretenimiento",  true),
    )

    // Filtrado local por categoría y búsqueda
    // TODO: cuando conectes Firestore, este filtrado se puede hacer con queries:
    // .whereEqualTo("categoriaNombre", categoriaSeleccionada)
    // .whereGreaterThanOrEqualTo("nombre", textoBusqueda)
    val productosFiltrados = todosLosProductos.filter { producto ->
        val coincideCategoria = categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada
        val coincideBusqueda  = producto.nombre.contains(textoBusqueda, ignoreCase = true) ||
                producto.marca.contains(textoBusqueda, ignoreCase = true)
        coincideCategoria && coincideBusqueda
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
    ) {
        // Header fijo con búsqueda y filtros
        HeaderCatalogo(
            textoBusqueda       = textoBusqueda,
            onTextoCambia       = { textoBusqueda = it },
            categorias          = categorias,
            categoriaSeleccionada = categoriaSeleccionada,
            onCategoriaClick    = { categoriaSeleccionada = it }
        )

        // Grid de productos con scroll
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            if (productosFiltrados.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No encontramos productos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NegroSuave
                        )
                        Text(
                            text = "Intentá con otra categoría o búsqueda",
                            fontSize = 13.sp,
                            color = GrisTexto
                        )
                    }
                }
            } else {
                // Grid 2 columnas
                productosFiltrados.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        fila.forEach { producto ->
                            TarjetaCatalogo(
                                producto = producto,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Si la fila tiene solo 1 producto, llenamos el espacio vacío
                        if (fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// HEADER: búsqueda + filtros de categoría

@Composable
fun HeaderCatalogo(
    textoBusqueda: String,
    onTextoCambia: (String) -> Unit,
    categorias: List<String>,
    categoriaSeleccionada: String,
    onCategoriaClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blanco)
            .padding(top = 16.dp, bottom = 0.dp)
    ) {
        // Título
        Text(
            text = "Catálogo",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NegroSuave,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = onTextoCambia,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text("Buscar productos...", color = GrisTexto, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = GrisTexto)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Cafe,
                unfocusedBorderColor = GrisBorde,
                focusedContainerColor   = FondoGris,
                unfocusedContainerColor = FondoGris
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chips de categorías con scroll horizontal
        // TODO: categorías vienen de Firestore
        // colección: categoria, campo: nombre
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEach { categoria ->
                val seleccionado = categoria == categoriaSeleccionada
                Surface(
                    modifier = Modifier.clickable { onCategoriaClick(categoria) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (seleccionado) Cafe else FondoGris
                ) {
                    Text(
                        text = categoria,
                        fontSize = 13.sp,
                        fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (seleccionado) Blanco else GrisTexto,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Divider(color = GrisBorde, thickness = 1.dp)
    }
}

// TARJETA DE PRODUCTO EN CATÁLOGO
@Composable
fun TarjetaCatalogo(
    producto: ProductoCatalogo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            // Foto del producto con chip de disponibilidad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Crema)
            ) {
                // TODO: reemplazar con AsyncImage de Coil cuando Firebase Storage esté listo:
                // AsyncImage(
                //     model = producto.imagenUrl,  (colección: productos, campo: imagenUrl)
                //     contentDescription = producto.nombre,
                //     modifier = Modifier.fillMaxSize(),
                //     contentScale = ContentScale.Crop
                // )
                Text(
                    text = producto.emoji,
                    fontSize = 56.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Chip DISPONIBLE / AGOTADO
                // TODO: basado en producto.activo == "Activo" && producto.stock > 0
                // colección: productos, campos: activo, stock
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (producto.disponible) VerdeOk else RojoAgotado
                ) {
                    Text(
                        text = if (producto.disponible) "DISPONIBLE" else "AGOTADO",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Info del producto
            Column(modifier = Modifier.padding(10.dp)) {

                // Marca
                // TODO: campo sugerido "marca" en colección: productos
                // si no existe, se puede usar categoriaNombre como sustituto
                Text(
                    text = producto.marca,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GrisTexto,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // TODO: producto.nombre (colección: productos, campo: nombre)
                Text(
                    text = producto.nombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NegroSuave,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Precio con label "Desde"
                Text(
                    text = "Precio",
                    fontSize = 10.sp,
                    color = GrisTexto
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TODO: producto.precioCredito (colección: productos, campo: precioCredito)
                    Text(
                        text = "L. ${String.format("%,.0f", producto.precio)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cafe
                    )

                    // Botón de flecha / solicitar
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (producto.disponible) Cafe else GrisBorde)
                            .clickable(enabled = producto.disponible) {
                                // TODO: navegar al detalle del producto
                                // pasarle el productoId para cargar datos de Firestore
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    }
                }
            }
        }
    }
}