package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.m_Creditos
import com.example.comisariatoproyecto.data.m_CuotaCredito
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_CuotaCredito
import com.example.comisariatoproyecto.data.r_Wishlist
import com.example.comisariatoproyecto.data.r_Reseñas

import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.NavyContainer
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import com.example.comisariatoproyecto.ui.theme.YellowStars
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProducto(
    producto: m_Productos?,
    repoCuotas: r_CuotaCredito,
    repoCreditos: r_Creditos,
    repoWishlist: r_Wishlist,          // ← nuevo parámetro
    repoReseñas: r_Reseñas, // Añadido para obtener puntuación real
    empleado: Empleado?,
    reservaPendiente: m_Creditos?,
    onBack: () -> Unit,
    onReservar: (m_Productos, Int?, Int) -> Unit,
    onCancelarReserva: (String) -> Unit,
    onVerComentarios: (String) -> Unit
) {
    val cuotas by repoCuotas.obtenerCuotas().collectAsState(initial = emptyList())

    var cantidad           by remember { mutableIntStateOf(1) }
    var modoCredito        by remember { mutableStateOf(true) }
    var plazoSeleccionado  by remember { mutableStateOf<m_CuotaCredito?>(null) }
    var porcentajeCfg      by remember { mutableDoubleStateOf(0.0) }
    var utilizadoActual    by remember { mutableDoubleStateOf(0.0) }
    // --- CARGAR PUNTUACIÓN REAL ---
    val reseñas by if (producto != null) {
        repoReseñas.obtenerReseñasDeProducto(producto.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val totalReseñas = reseñas.size
    val promedio = if (reseñas.isNotEmpty()) {
        val avg = reseñas.map { it.estrellas }.average()
        Math.round(avg * 10) / 10.0
    } else 0.0

    var cantidad by remember { mutableIntStateOf(1) }
    var modoCredito by remember { mutableStateOf(true) }
    var plazoSeleccionado by remember { mutableStateOf<m_CuotaCredito?>(null) }


    var porcentajeCfg by remember { mutableDoubleStateOf(0.0) }
    var utilizadoActual by remember { mutableDoubleStateOf(0.0) }
    var cargandoValidacion by remember { mutableStateOf(true) }

    // ── Wishlist: observamos en tiempo real si este producto ya está guardado ──
    val isWishlisted by repoWishlist
        .observarEnWishlist(producto?.id ?: "")
        .collectAsState(initial = false)

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (empleado != null) {
            try {
                porcentajeCfg   = repoCreditos.obtenerConfiguracionCredito()
                utilizadoActual = repoCreditos.obtenerCreditoUtilizadoReal(empleado.codigoEmpleado)
            } catch (_: Exception) { }
        }
        cargandoValidacion = false
    }

    fun formatLps(valor: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("es", "HN"))
        fmt.minimumFractionDigits = 2
        fmt.maximumFractionDigits = 2
        return "L. ${fmt.format(valor)}"
    }

    fun formatCuota(valor: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("es", "HN"))
        fmt.minimumFractionDigits = 2
        fmt.maximumFractionDigits = 2
        return "L. ${fmt.format(valor)}"
    }

    val requiereSeleccionarPlazo = modoCredito && plazoSeleccionado == null
    val precioBase     = if (modoCredito) producto?.precioCredito ?: 0.0 else producto?.precioContado ?: 0.0
    val totalSimulado  = precioBase * cantidad
    val cuotaSimulada  = if (modoCredito) {
        if (plazoSeleccionado != null) totalSimulado / plazoSeleccionado!!.id.toInt() else 0.0
    } else totalSimulado
    val limiteMensual  = (empleado?.salario?.toDouble() ?: 0.0) * porcentajeCfg
    val disponible     = (limiteMensual - utilizadoActual).coerceAtLeast(0.0)
    val excedeLimite   = !requiereSeleccionarPlazo && cuotaSimulada > disponible
    val estaAgotado    = producto != null && producto.stock <= producto.stockMinimo

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Producto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            if (producto != null) {
                Column {
                    HorizontalDivider(color = NavyPrimary.copy(alpha = 0.08f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceWhite)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Control de cantidad
                        Row(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = NavyPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceBase)
                                    .clickable(enabled = !estaAgotado) { if (cantidad > 1) cantidad-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("−", fontSize = 18.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
                            }
                            Box(
                                modifier = Modifier.width(36.dp).height(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$cantidad", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceBase)
                                    .clickable(enabled = !estaAgotado) { if (cantidad < producto.stock) cantidad++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", fontSize = 18.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
                            }
                        }

                        Button(
                            onClick = {
                                if (reservaPendiente != null) {
                                    onCancelarReserva(reservaPendiente.id)
                                } else {
                                    onReservar(
                                        producto,
                                        if (modoCredito) plazoSeleccionado?.id?.toInt() else null,
                                        cantidad
                                    )
                                }
                            },
                            enabled = when {
                                estaAgotado && reservaPendiente == null -> false
                                reservaPendiente != null -> true
                                requiereSeleccionarPlazo -> false
                                excedeLimite -> false
                                cargandoValidacion -> false
                                else -> true
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    estaAgotado          -> Color.Gray
                                    reservaPendiente != null -> Color.Red
                                    excedeLimite         -> Color.Gray
                                    requiereSeleccionarPlazo -> NavyPrimary.copy(alpha = 0.6f)
                                    else                 -> NavyPrimary
                                },
                                disabledContainerColor = if (excedeLimite)
                                    Color.Gray.copy(alpha = 0.5f)
                                else
                                    NavyPrimary.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = when {
                                    estaAgotado              -> "Agotado"
                                    excedeLimite             -> "Límite insuficiente"
                                    reservaPendiente != null -> "Cancelar reserva"
                                    requiereSeleccionarPlazo -> "Seleccione un plazo"
                                    else                     -> "Reservar producto"
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (producto == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Producto no disponible", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Hero image ───────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .background(NavyContainer),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model              = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )

                    // ── Botón corazón ────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable {
                                // Toggle en Firestore en segundo plano
                                scope.launch {
                                    repoWishlist.toggle(producto)
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = if (isWishlisted) Icons.Filled.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Lista de deseos",
                            tint               = if (isWishlisted) Color.Red else Color.White,
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                }

                // ── Contenido ────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        producto.categoriaNombre.uppercase(),
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = TextSecondary.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        producto.nombre,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = NavyPrimary,
                        lineHeight = 28.sp
                    )
                    Spacer(Modifier.height(6.dp))

                    // --- PUNTUACIÓN REAL ---
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onVerComentarios(producto.id) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val estrellasLlenas = promedio.toInt()
                        val mediaEstrella   = (promedio - estrellasLlenas) >= 0.5

                        repeat(5) { index ->
                            when {
                                index < estrellasLlenas -> Icon(
                                    Icons.Filled.Star, null,
                                    tint = YellowStars,
                                    modifier = Modifier.size(18.dp)
                                )
                                index == estrellasLlenas && mediaEstrella -> Icon(
                                    Icons.AutoMirrored.Filled.StarHalf, null,
                                    tint = YellowStars,
                                    modifier = Modifier.size(18.dp)
                                )
                                else -> Icon(
                                    Icons.Filled.StarOutline, null,
                                    tint = TextSecondary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(2.dp))
                        Text(
                            if (totalReseñas == 0) "Sin opiniones"
                            else "($totalReseñas ${if (totalReseñas == 1) "opinión" else "opiniones"})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.height(6.dp))

                    Text(
                        producto.descripcion,
                        fontSize   = 13.sp,
                        color      = TextSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = NavyPrimary.copy(alpha = 0.08f))
                    Spacer(Modifier.height(16.dp))

                    // Toggle Contado / Crédito
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceBase)
                            .padding(3.dp)
                    ) {
                        listOf("Contado" to false, "Crédito" to true).forEach { (label, esCredito) ->
                            val activo = modoCredito == esCredito
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (activo) SurfaceWhite else Color.Transparent)
                                    .clickable { modoCredito = esCredito }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                    color = if (activo) NavyPrimary else TextSecondary)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Precio
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (modoCredito) formatLps(producto.precioCredito)
                            else formatLps(producto.precioContado),
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = NavyPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (modoCredito) "precio al crédito" else "precio al contado",
                            fontSize  = 13.sp,
                            color     = TextSecondary.copy(alpha = 0.7f),
                            modifier  = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))

                    // Selector de plazos
                    if (modoCredito) {
                        Text(
                            "SELECCIONA TU PLAZO",
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.SemiBold,
                            color         = TextSecondary.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cuotas.forEach { cuota ->
                                val meses        = cuota.id.toInt()
                                val cuotaMensual = producto.precioCredito / meses.toDouble()
                                val seleccionado = plazoSeleccionado?.id == cuota.id

                                Column(
                                    modifier = Modifier
                                        .widthIn(min = 80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (seleccionado) NavyPrimary.copy(alpha = 0.05f)
                                            else SurfaceWhite
                                        )
                                        .border(
                                            width = if (seleccionado) 1.5.dp else 1.dp,
                                            color = if (seleccionado) NavyPrimary
                                            else NavyPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { plazoSeleccionado = cuota }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "$meses meses",
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color      = if (seleccionado) NavyPrimary else TextSecondary,
                                        maxLines   = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        formatCuota(cuotaMensual),
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = NavyPrimary,
                                        maxLines   = 1,
                                        softWrap   = false
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
