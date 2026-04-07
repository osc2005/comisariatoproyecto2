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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onBack: () -> Unit,
    onOpinar: () -> Unit = {}
) {
    data class Resena(
        val nombre: String,
        val estrellas: Int,
        val fecha: String,
        val comentario: String
    )
//PARA MOSTRAR EL MODAL PARA DEJAR LA OPINION
    var mostrarOpinar by remember { mutableStateOf(false) }


    // Datos de ejemplo (Luego podrías traerlos de Firebase usando el productoId)
    val resenas = listOf(
        Resena("Andrés García",    5, "15 Mar 2026", "Excelente equipo, la batería dura muchísimo y el trámite de crédito fue muy rápido."),
        Resena("María Bustamante", 4, "10 Mar 2026", "Muy buen producto, cumple con lo que promete. La entrega fue puntual."),
        Resena("Carlos Ramos",     5, "02 Mar 2026", "Increíble relación precio-calidad. Lo recomiendo ampliamente a mis compañeros."),
        Resena("Luisa Peralta",    3, "25 Feb 2026", "Bueno en general, aunque esperaba un poco más de rendimiento en aplicaciones pesadas."),
        Resena("Roberto Mejía",    5, "18 Feb 2026", "El proceso de reserva fue súper fácil. Feliz con mi compra."),
        Resena("Diana Flores",     4, "12 Feb 2026", "Bonito diseño y muy rápido. El crédito se aprobó en minutos."),
        Resena("José Morales",     5, "05 Feb 2026", "Sin duda lo mejor que he comprado en el comisariato. Muy recomendado.")
    )

    val ITEMS_POR_PAGINA = 3
    val totalPaginas = ceil(resenas.size / ITEMS_POR_PAGINA.toDouble()).toInt()

    var paginaActual by remember { mutableIntStateOf(1) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val resenasPagina = resenas
        .drop((paginaActual - 1) * ITEMS_POR_PAGINA)
        .take(ITEMS_POR_PAGINA)

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

            // ── Hero de calificación ─────────────────────────────────────
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
                        "4.6",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary,
                        lineHeight = 52.sp
                    )
                    Text(
                        "/ 5",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(4) {
                        Icon(Icons.Filled.Star, null, tint = YellowStars, modifier = Modifier.size(24.dp))
                    }
                    Icon(Icons.AutoMirrored.Filled.StarHalf, null, tint = YellowStars, modifier = Modifier.size(24.dp))
                }
                Text(
                    "${resenas.size} comentarios",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            HorizontalDivider(color = NavyPrimary.copy(alpha = 0.08f))

            // ── Card: ¿Qué te pareció? ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("¿Qué te pareció el producto?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    Text("Tu opinión ayuda a otros miembros.", fontSize = 12.sp, color = TextSecondary)
                }
                Button(
                    onClick = onOpinar,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Opinar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = NavyPrimary.copy(alpha = 0.08f))
            Spacer(Modifier.height(8.dp))

            // ── Lista de reseñas (página actual) ─────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                resenasPagina.forEach { resena ->
                    ResenaCard(resena.nombre, resena.estrellas, resena.fecha, resena.comentario)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Paginación ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón anterior
                IconButton(
                    onClick = {
                        paginaActual--
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    enabled = paginaActual > 1,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (paginaActual > 1) NavyPrimary.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Página anterior",
                        tint = if (paginaActual > 1) NavyPrimary else TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Números de página
                repeat(totalPaginas) { index ->
                    val pagina = index + 1
                    val esActiva = pagina == paginaActual

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (esActiva) NavyPrimary else Color.Transparent)
                            .border(
                                width = if (esActiva) 0.dp else 1.dp,
                                color = if (esActiva) Color.Transparent else NavyPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !esActiva) {
                                paginaActual = pagina
                                scope.launch { scrollState.animateScrollTo(0) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$pagina",
                            fontSize = 13.sp,
                            fontWeight = if (esActiva) FontWeight.Bold else FontWeight.Medium,
                            color = if (esActiva) Color.White else TextSecondary
                        )
                    }

                    if (index < totalPaginas - 1) Spacer(Modifier.width(4.dp))
                }

                Spacer(Modifier.width(8.dp))

                // Botón siguiente
                IconButton(
                    onClick = {
                        paginaActual++
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    enabled = paginaActual < totalPaginas,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (paginaActual < totalPaginas) NavyPrimary.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Página siguiente",
                        tint = if (paginaActual < totalPaginas) NavyPrimary else TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        //dejar esto funcionall.....
//        if (mostrarOpinar) {
//            OpinarBottomSheet(
//                productoNombre    = productoNombre,     // pásalo como parámetro
//                productoImagenUrl = productoImagenUrl,  // pásalo como parámetro
//                onDismiss = { mostrarOpinar = false },
//                onEnviar  = { estrellas, texto ->
//                    // lógica pendiente
//                    mostrarOpinar = false
//                }
//            )
//        }
    }
}

@Composable
private fun ResenaCard(
    nombre: String,
    estrellas: Int,
    fecha: String,
    comentario: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    nombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < estrellas) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = null,
                            tint = if (index < estrellas) YellowStars else TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
            Text(
                fecha.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )
        }
        Text(
            comentario,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}