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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.comisariatoproyecto.data.m_Categoria
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Categoria
import com.example.comisariatoproyecto.data.r_Productos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// ── Paleta ──────────────────────────────────────────────────────────────────
private val Cafe        = Color(0xFF8B5A2B)
private val Crema       = Color(0xFFFFE4C4)
private val FondoGris   = Color(0xFFF5F0EB)
private val Blanco      = Color.White
private val NegroSuave  = Color(0xFF111827)
private val GrisTexto   = Color(0xFF6B7280)
private val GrisBorde   = Color(0xFFE5E7EB)
private val VerdeOk     = Color(0xFF22C55E)
private val RojoAgotado = Color(0xFFEF4444)

//  ViewModel
class CatalogoViewModel : ViewModel() {

    private val repoProd = r_Productos()
    private val repoCat  = r_Categoria()

    private val _productos   = MutableStateFlow<List<m_Productos>>(emptyList())
    private val _categorias  = MutableStateFlow<List<m_Categoria>>(emptyList())
    private val _cargando    = MutableStateFlow(true)

    val productos:  StateFlow<List<m_Productos>> = _productos
    val categorias: StateFlow<List<m_Categoria>> = _categorias
    val cargando:   StateFlow<Boolean>           = _cargando

    init {
        viewModelScope.launch {
            repoCat.obtenerCategorias().collect { lista ->
                _categorias.value = lista
            }
        }
        cargarTodos()
    }

    fun cargarTodos() {
        viewModelScope.launch {
            _cargando.value = true
            repoProd.obtenerProductos().collect { lista ->
                _productos.value = lista
                _cargando.value  = false
            }
        }
    }

    fun cargarPorCategoria(categoriaId: String) {
        viewModelScope.launch {
            _cargando.value = true
            repoProd.obtenerProductosPorCategoria(categoriaId).collect { lista ->
                _productos.value = lista
                _cargando.value  = false
            }
        }
    }
}

//  Pantalla principal
@Composable
fun PantallaCatalogo(
    onVerDetalle: (id: String, nombre: String) -> Unit = { _, _ -> },
    vm: CatalogoViewModel = viewModel()) {

    val productos   by vm.productos.collectAsState()
    val categorias  by vm.categorias.collectAsState()
    val cargando    by vm.cargando.collectAsState()

    var categoriaSeleccionadaId  by remember { mutableStateOf<String?>(null) }   // null = Todos
    var textoBusqueda            by remember { mutableStateOf("") }

    // Filtrado local por búsqueda (nombre o categoriaNombre)
    // El filtro por categoría ya viene filtrado desde Firestore via ViewModel
    val productosFiltrados = productos.filter { p ->
        val activo = p.estado == "Activo" // solo productos activos
        val busqueda = p.nombre.contains(textoBusqueda, ignoreCase = true) ||
                p.categoriaNombre.contains(textoBusqueda, ignoreCase = true)
        activo && busqueda
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
    ) {
        HeaderCatalogo(
            textoBusqueda            = textoBusqueda,
            onTextoCambia            = { textoBusqueda = it },
            categorias               = categorias,
            categoriaSeleccionadaId  = categoriaSeleccionadaId,
            onCategoriaClick         = { id ->
                categoriaSeleccionadaId = id
                if (id == null) vm.cargarTodos()
                else vm.cargarPorCategoria(id)
            }
        )

        when {
            cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Cafe)
                }
            }
            productosFiltrados.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No encontramos productos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NegroSuave
                        )
                        Text(
                            "Intentá con otra categoría o búsqueda",
                            fontSize = 13.sp,
                            color = GrisTexto
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                ) {
                    Spacer(Modifier.height(14.dp))
                    productosFiltrados.chunked(2).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            fila.forEach { producto ->
                                TarjetaCatalogo(
                                    producto = producto,
                                    modifier = Modifier.weight(1f),
                                    onVerDetalle = onVerDetalle
                                )
                            }
                            if (fila.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

//  Header
@Composable
fun HeaderCatalogo(
    textoBusqueda: String,
    onTextoCambia: (String) -> Unit,
    categorias: List<m_Categoria>,
    categoriaSeleccionadaId: String?,
    onCategoriaClick: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blanco)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Catálogo",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NegroSuave,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = onTextoCambia,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Buscar productos...", color = GrisTexto, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = GrisTexto) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Cafe,
                unfocusedBorderColor    = GrisBorde,
                focusedContainerColor   = FondoGris,
                unfocusedContainerColor = FondoGris
            )
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chip "Todos"
            ChipCategoria(
                nombre = "Todos",
                seleccionado = categoriaSeleccionadaId == null,
                onClick = { onCategoriaClick(null) }
            )
            // Chips dinámicos desde Firestore
            categorias.forEach { cat ->
                ChipCategoria(
                    nombre = cat.nombre,
                    seleccionado = cat.id == categoriaSeleccionadaId,
                    onClick = { onCategoriaClick(cat.id) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = GrisBorde, thickness = 1.dp)
    }
}

@Composable
fun ChipCategoria(nombre: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (seleccionado) Cafe else FondoGris
    ) {
        Text(
            text = nombre,
            fontSize = 13.sp,
            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
            color = if (seleccionado) Blanco else GrisTexto,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

//  Tarjeta de producto
@Composable
fun TarjetaCatalogo(
    producto: m_Productos,
    modifier: Modifier = Modifier,
    onVerDetalle: (id: String, nombre: String) -> Unit = { _, _ -> }) {

    val disponible = producto.estado == "Activo" && producto.stock > 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Crema)
            ) {
                if (producto.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "🛒",
                        fontSize = 56.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (disponible) VerdeOk else RojoAgotado
                ) {
                    Text(
                        text = if (disponible) "DISPONIBLE" else "AGOTADO",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = producto.categoriaNombre,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GrisTexto,
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = producto.nombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NegroSuave,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(6.dp))

                Text("Precio", fontSize = 10.sp, color = GrisTexto)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "L. ${String.format("%,.0f", producto.precioCredito)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Cafe
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (disponible) Cafe else GrisBorde)
                            .clickable(enabled = disponible) {
                                onVerDetalle(producto.id, producto.nombre)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }
            }
        }
    }
}