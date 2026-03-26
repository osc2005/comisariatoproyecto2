package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.example.comisariatoproyecto.data.r_Categoria
import com.example.comisariatoproyecto.data.r_Productos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosCatalogo(
    repoCategoria: r_Categoria,
    repoProducto: r_Productos,
    onBack: () -> Unit,
    onVerProductos: (m_Categoria) -> Unit,
    onVerDetalleProducto: (m_Productos) -> Unit
) {
    val categorias by repoCategoria.obtenerCategorias().collectAsState(initial = null)
    val todosLosProductos by repoProducto.obtenerProductos().collectAsState(initial = emptyList())

    // Estado de la barra de búsqueda predictiva
    var textoBusqueda by remember { mutableStateOf("") }
    var expandirSugerencias by remember { mutableStateOf(false) }

    // Filtra productos según lo que escribe el usuario
    val sugerencias = remember(textoBusqueda, todosLosProductos) {
        if (textoBusqueda.isBlank()) emptyList()
        else todosLosProductos.filter {
            it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    it.descripcion.contains(textoBusqueda, ignoreCase = true)
        }.take(8) // máximo 8 sugerencias para no saturar
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(8.dp))

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
                        .fillMaxWidth(),
                    placeholder = { Text("Buscar un producto específico...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = {
                                textoBusqueda = ""
                                expandirSugerencias = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true
                )

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
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${producto.categoriaNombre}  •  L. ${producto.precioContado}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    textoBusqueda = producto.nombre
                                    expandirSugerencias = false
                                    onVerDetalleProducto(producto) // navega al detalle
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  o navega por categoría  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))

            when {
                categorias == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                categorias!!.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay categorías registradas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(categorias!!, key = { it.id }) { categoria ->
                            ElevatedCard(
                                onClick = { onVerProductos(categoria) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = categoria.nombre,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}