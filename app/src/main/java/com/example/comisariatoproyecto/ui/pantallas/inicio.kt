package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
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
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.r_permisos

// Paleta consistente con el login y el menú inferior
private val Cafe       = Color(0xFF8B5A2B)
private val AzulOscuro = Color(0xFF1A2E4A)
private val Crema      = Color(0xFFFFE4C4)
private val FondoGris  = Color(0xFFF5F0EB)
private val Blanco     = Color.White
private val NegroSuave = Color(0xFF111827)
private val GrisTexto  = Color(0xFF6B7280)
private val VerdeOk    = Color(0xFF22C55E)



@Composable
fun PantallaInicio(repo: r_permisos) {
    var empleado by remember { mutableStateOf<Empleado?>(null) }



    LaunchedEffect(Unit) {
        empleado = repo.obtenerMiPerfilEmpleado()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderInicio(empleado = empleado)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TarjetaLineaCredito(empleado = empleado)
            Spacer(modifier = Modifier.height(14.dp))
            TarjetaProximoPago()
            Spacer(modifier = Modifier.height(14.dp))
            TarjetaNotificacion()
            Spacer(modifier = Modifier.height(20.dp))
            SeccionParaTi()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ----------------------------------------------------------------------------
// HEADER
// ----------------------------------------------------------------------------
@Composable
fun HeaderInicio(empleado: Empleado?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cafe)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comisariato",
                    color = Blanco,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Blanco.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = Blanco,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TODO: traer nombre del usuario logueado
            // colección: usuarios, campo: nombre
            Text(
                text = "Hola, ${empleado?.nombreCompleto}",
                color = Blanco,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bienvenido de vuelta a tu Concierge Financiero.",
                color = Blanco.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        }
    }
}

// ----------------------------------------------------------------------------
// TARJETA: LÍNEA DE CRÉDITO
// ----------------------------------------------------------------------------
@Composable
fun TarjetaLineaCredito(
    empleado: Empleado?
) {


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LÍNEA DE CRÉDITO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GrisTexto,
                    letterSpacing = 1.sp
                )
                // TODO: disponible = empleado.limiteCredito - credito.saldoPendiente
                // colección empleados: campo limiteCredito
                // colección creditos: campo saldoPendiente
                Text(
                    text = "Disponible: ${empleado?.salario}",
                    fontSize = 12.sp,
                    color = VerdeOk,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TODO: progreso = credito.totalCredito / empleado.limiteCredito
            // colección creditos: campo totalCredito
            // colección empleados: campo limiteCredito
            val progreso = 0.35f

            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Cafe,
                trackColor = Crema
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // TODO: credito.totalCredito (colección: creditos, campo: totalCredito)
                Text(text = "Utilizado: L. 3,500", fontSize = 12.sp, color = GrisTexto)
                // TODO: empleado.limiteCredito (colección: empleados, campo: limiteCredito)
                val disponible = (empleado?.salario ?: 0.0) * 0.25
                
                Text(text = "Total disponible: ${disponible}", fontSize = 12.sp, color = NegroSuave, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TARJETA: PRÓXIMO PAGO
// ----------------------------------------------------------------------------
@Composable
fun TarjetaProximoPago() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulOscuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRÓXIMO PAGO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Blanco.copy(alpha = 0.75f),
                    letterSpacing = 1.sp
                )
                // TODO: colección creditos, campo: estado
                // si estado == "Activo" -> "AL DÍA", sino -> "PENDIENTE"
                Surface(
                    color = VerdeOk.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "AL DÍA",
                        color = VerdeOk,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TODO: credito.cuotaMensual (colección: creditos, campo: cuotaMensual)
            Text(
                text = "L. 850.00",
                color = Blanco,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // TODO: fecha próximo pago a partir de credito.fechaInicio
                // colección: creditos, campo: fechaInicio
                Text(
                    text = "📅  15 May, 2024",
                    color = Blanco.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Button(
                    onClick = { /* TODO: navegar a Mi Crédito */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Crema),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Pagarlo ahora",
                        color = NegroSuave,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// TARJETA: NOTIFICACIÓN DE SOLICITUD
// ----------------------------------------------------------------------------
@Composable
fun TarjetaNotificacion() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = VerdeOk,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // TODO: último crédito del empleado
                // colección: creditos, campos: estado + productoId
                // buscar nombre en colección: productos, campo: nombre
                // texto: "Tu solicitud de ${producto.nombre} fue ${credito.estado}"
                Text(
                    text = "¡Tu solicitud de Smartphone Galaxy A54 fue APROBADA!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = NegroSuave
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { /* TODO: navegar al detalle en Mi Crédito */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Ver detalle →",
                        color = Cafe,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECCIÓN: PARA TI
// ----------------------------------------------------------------------------
@Composable
fun SeccionParaTi() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Para ti", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NegroSuave)
        TextButton(onClick = { /* TODO: navegar a Catálogo */ }) {
            Text(text = "Ver todo", color = Cafe, fontSize = 13.sp)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    val productosPlaceholder = listOf(
        Triple("Laptops",        "L. 12,500", "💻"),
        Triple("Audífonos Sony", "L. 2,800",  "🎧"),
        Triple("Tablet Samsung", "L. 8,200",  "📱"),
        Triple("Silla Gamer",    "L. 4,500",  "🪑"),
    )

    // Mostrar productos en filas de 2 columnas
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        productosPlaceholder.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { (nombre, precio, emoji) ->
                    TarjetaProducto(
                        nombre = nombre,
                        precio = precio,
                        emoji = emoji,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Si la última fila solo tiene 1 elemento, añadimos un espacio vacío
                if (fila.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

//----------------------------------------------------------------------------
// TARJETA INDIVIDUAL DE PRODUCTO — ajustada para cuadrícula
// ----------------------------------------------------------------------------
@Composable
fun TarjetaProducto(
    nombre: String,
    precio: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp) // Altura ajustada para 2 columnas
                    .background(Crema),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 50.sp)
            }

            Column(modifier = Modifier.padding(10.dp)) {
                // Categoría
                Surface(
                    color = Crema.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Tecnología",
                        fontSize = 9.sp,
                        color = Cafe,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = nombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NegroSuave,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = precio,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cafe
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* TODO: navegar al detalle */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Cafe),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = Blanco,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Solicitar",
                        color = Blanco,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
