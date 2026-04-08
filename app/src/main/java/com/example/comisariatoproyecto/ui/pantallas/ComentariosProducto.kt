package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_Reseñas
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary
import com.example.comisariatoproyecto.ui.theme.YellowStars
import kotlinx.coroutines.launch
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentariosProducto(
    productoId: String,
    empleadoId: String,
    repoReseñas: r_Reseñas,
    repoCreditos: r_Creditos,
    onBack: () -> Unit,
    onOpinar: (creditoId: String) -> Unit
) {
    // 1. Obtenemos las reseñas en tiempo real desde Firebase
    val reseñas by repoReseñas
        .obtenerReseñasDeProducto(productoId)
        .collectAsState(initial = emptyList())

    // 2. Estadísticas calculadas dinámicamente
    val totalReseñas = reseñas.size
    val promedio = if (reseñas.isNotEmpty()) {
        val avg = reseñas.map { it.estrellas }.average()
        Math.round(avg * 10) / 10.0
    } else 0.0

    var creditoHabilitador by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // 3. Lógica de validación: Solo compradores con crédito APROBADO pueden opinar
    LaunchedEffect(productoId, empleadoId, reseñas.size) {
        val creditosAprobados = repoCreditos
            .obtenerReservasDeEmpleado(empleadoId)
            .filter { it.productoId == productoId && it.estado == "Aprobado" }

        var idEncontrado: String? = null
        for (credito in creditosAprobados) {
            val yaExiste = repoReseñas.creditoYaTieneReseña(credito.id)
            android.util.Log.d("DEBUG_OPINAR", "creditoId=${credito.id} yaExiste=$yaExiste")
            if (!yaExiste) {
                idEncontrado = credito.id
                break
            }
        }
        creditoHabilitador = idEncontrado
        android.util.Log.d("DEBUG_OPINAR", "creditoHabilitador=$creditoHabilitador")
    }
    val ITEMS_POR_PAGINA = 3
    val totalPaginas     = maxOf(1, ceil(reseñas.size / ITEMS_POR_PAGINA.toDouble()).toInt())
    var paginaActual     by remember { mutableIntStateOf(1) }
    val scrollState      = rememberScrollState()

    LaunchedEffect(reseñas.size) {
        if (paginaActual > totalPaginas) paginaActual = 1
    }

    val reseñasPagina = reseñas
        .drop((paginaActual - 1) * ITEMS_POR_PAGINA)
        .take(ITEMS_POR_PAGINA)

    val estrellasLlenas = promedio.toInt()
    val mediaEstrella   = (promedio - estrellasLlenas) >= 0.5

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                title = { Text("Opiniones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {

            // Hero de calificación real
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (totalReseñas == 0) "—" else String.format("%.1f", promedio),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary,
                        lineHeight = 52.sp
                    )
                    if (totalReseñas > 0) {
                        Text("/ 5", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                    }
                }

                if (totalReseñas > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { index ->
                            when {
                                index < estrellasLlenas -> Icon(Icons.Filled.Star, null, tint = YellowStars, modifier = Modifier.size(24.dp))
                                index == estrellasLlenas && mediaEstrella -> Icon(Icons.AutoMirrored.Filled.StarHalf, null, tint = YellowStars, modifier = Modifier.size(24.dp))
                                else -> Icon(Icons.Filled.StarOutline, null, tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                Text(
                    if (totalReseñas == 0) "Sin opiniones aún"
                    else "$totalReseñas ${if (totalReseñas == 1) "opinión" else "opiniones"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            HorizontalDivider(color = NavyPrimary.copy(alpha = 0.08f))

            // BOTÓN OPINAR: Solo si tiene una compra aprobada sin reseña
            if (creditoHabilitador != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿Qué te pareció el producto?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    Button(
                        onClick = { onOpinar(creditoHabilitador!!) },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Opinar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                HorizontalDivider(color = NavyPrimary.copy(alpha = 0.08f))
            }

            Spacer(Modifier.height(8.dp))

            if (reseñas.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("Aún no hay opiniones para este producto.", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reseñasPagina.forEach { reseña ->
                        ResenaCard(
                            nombre     = "${reseña.empleadoNombres} ${reseña.empleadoApellidos}",
                            estrellas  = reseña.estrellas,
                            fecha      = reseña.fechaReseña?.toDate()?.let {
                                java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es")).format(it)
                            } ?: "",
                            comentario = reseña.comentario
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (totalPaginas > 1) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                paginaActual--
                                scope.launch { scrollState.scrollTo(0) }
                            },
                            enabled = paginaActual > 1,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(if (paginaActual > 1) NavyPrimary.copy(alpha = 0.08f) else Color.Transparent)
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Anterior", tint = if (paginaActual > 1) NavyPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        repeat(totalPaginas) { index ->
                            val pagina = index + 1
                            val activa = pagina == paginaActual
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(if (activa) NavyPrimary else Color.Transparent).border(1.dp, if (activa) Color.Transparent else NavyPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).clickable(enabled = !activa) {
                                paginaActual = pagina
                                scope.launch { scrollState.scrollTo(0) }
                            }, contentAlignment = Alignment.Center) {
                                Text("$pagina", fontSize = 13.sp, fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium, color = if (activa) Color.White else TextSecondary)
                            }
                            if (index < totalPaginas - 1) Spacer(Modifier.width(4.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                paginaActual++
                                scope.launch { scrollState.scrollTo(0) }
                            },
                            enabled = paginaActual < totalPaginas,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(if (paginaActual < totalPaginas) NavyPrimary.copy(alpha = 0.08f) else Color.Transparent)
                        ) {
                            Icon(Icons.Default.ChevronRight, "Siguiente", tint = if (paginaActual < totalPaginas) NavyPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ResenaCard(nombre: String, estrellas: Int, fecha: String, comentario: String) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceWhite).border(1.dp, NavyPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                Text(fecha, fontSize = 11.sp, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                repeat(5) { index -> Icon(if (index < estrellas) Icons.Filled.Star else Icons.Filled.StarOutline, null, tint = if (index < estrellas) YellowStars else TextSecondary.copy(alpha = 0.2f), modifier = Modifier.size(16.dp)) }
            }
        }
        if (comentario.isNotBlank()) {
            Text(comentario, fontSize = 13.sp, color = NavyPrimary.copy(alpha = 0.8f), lineHeight = 18.sp)
        }
    }
}
