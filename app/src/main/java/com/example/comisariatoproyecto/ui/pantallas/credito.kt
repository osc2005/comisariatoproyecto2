package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.comisariatoproyecto.data.m_CreditoDetalle
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

private val filtrosCredito = listOf("Todos", "Pendiente", "Aprobado", "Rechazado", "Cancelado")
private val formatoFechaLista = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "HN"))

@Composable
fun PantallaCredito(
    filtroInicial: String = "Todos",
    onVerDetalle: (m_CreditoDetalle) -> Unit = {}
) {
    val repoAuth    = remember { r_permisos() }
    val repoCredito = remember { r_Creditos() }

    var filtroSeleccionado by remember { mutableStateOf(filtroInicial) }
    var creditos           by remember { mutableStateOf<List<m_CreditoDetalle>>(emptyList()) }
    var cargando           by remember { mutableStateOf(true) }
    var errorMsg           by remember { mutableStateOf<String?>(null) }

    // Traer empleado logueado → escuchar sus créditos en tiempo real
    LaunchedEffect(Unit) {
        try {
            val empleado   = repoAuth.obtenerMiPerfilEmpleado()
            val empleadoId = empleado?.codigoEmpleado

            if (empleadoId.isNullOrEmpty()) {
                errorMsg = "No se encontró el perfil del empleado."
                cargando = false
                return@LaunchedEffect
            }

            repoCredito.obtenerReservasDetalladas(empleadoId).collect { lista ->
                creditos = lista
                cargando = false
            }
        } catch (e: Exception) {
            errorMsg = "Error al cargar créditos: ${e.message}"
            cargando = false
        }
    }

    // Más reciente primero
    val creditosFiltrados = creditos
        .filter { filtroSeleccionado == "Todos" || it.estado == filtroSeleccionado }
        .sortedByDescending { it.fechaRegistro }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        //  Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 14.dp)
        ) {
            Text(
                text = "Mis Créditos",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Consultá el estado de tus solicitudes de crédito de forma sencilla y transparente.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }

        //  Chips de filtro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filtrosCredito.forEach { filtro ->
                val activo = filtro == filtroSeleccionado
                Surface(
                    modifier = Modifier.clickable { filtroSeleccionado = filtro },
                    shape = RoundedCornerShape(20.dp),
                    color = if (activo) NavyPrimary else SurfaceBase
                ) {
                    Text(
                        text = filtro,
                        fontSize = 13.sp,
                        fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (activo) Color.White else TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        //  Contenido 
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = NavyPrimary
                    )
                }
                errorMsg != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg ?: "", color = TextSecondary, fontSize = 14.sp)
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        if (creditosFiltrados.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📋", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No tenés créditos en este estado",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NavyPrimary
                                    )
                                    Text(
                                        text = "Podés solicitar uno desde el Catálogo",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        } else {
                            creditosFiltrados.forEach { credito ->
                                TarjetaCreditoLista(
                                    credito = credito,
                                    onClick = { onVerDetalle(credito) }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// navegación al detalle del crédito
@Composable
fun TarjetaCreditoLista(
    credito: m_CreditoDetalle,
    onClick: () -> Unit
) {
    val config = estadoConfigCredito(credito.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = credito.productoImgUrl,
                contentDescription = credito.productoNombre,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceBase),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = credito.productoNombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = config.containerColor
                    ) {
                        Text(
                            text = credito.estado.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = config.color,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatearFechaCredito(credito),
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                AccionRapidaCredito(credito = credito)
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Ver detalle",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AccionRapidaCredito(credito: m_CreditoDetalle) {
    when (credito.estado) {
        "Aprobado" -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = if (credito.estadoCredito == "Activo") GreenPrimary else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (credito.estadoCredito == "Activo") "• CRÉDITO ACTIVO" else "• CRÉDITO INACTIVO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (credito.estadoCredito == "Activo") GreenPrimary else TextSecondary
                )
            }
        }
        "Pendiente" -> {
            Text(
                text = "En revisión institucional",
                fontSize = 11.sp,
                color = OrangeWarning,
                fontWeight = FontWeight.Medium
            )
        }
        "Rechazado" -> {
            Text(
                text = "No cumple con los requisitos para el crédito",
                fontSize = 11.sp,
                color = RedCancel,
                fontWeight = FontWeight.SemiBold
            )
        }
        else -> { }
    }
}

data class EstadoVisualCredito(
    val color: Color,
    val containerColor: Color,
    val icono: ImageVector
)

fun estadoConfigCredito(estado: String): EstadoVisualCredito = when (estado) {
    "Aprobado"  -> EstadoVisualCredito(GreenPrimary,  GreenContainer,  Icons.Outlined.CheckCircle)
    "Pendiente" -> EstadoVisualCredito(OrangeWarning, OrangeContainer, Icons.Outlined.HourglassEmpty)
    "Rechazado" -> EstadoVisualCredito(RedCancel,     RedContainer,    Icons.Outlined.Cancel)
    else        -> EstadoVisualCredito(GrayNeutral,   GrayContainer,   Icons.Outlined.Info)
}

fun formatearFechaCredito(credito: m_CreditoDetalle): String {
    val timestamp = when (credito.estado) {
        "Aprobado", "Rechazado" -> credito.fechaAutoriza ?: credito.fechaRegistro
        else                    -> credito.fechaRegistro
    }
    val prefijo = when (credito.estado) {
        "Aprobado", "Rechazado" -> "Autorizado"
        else                    -> "Registrado"
    }
    return if (timestamp != null) {
        "$prefijo: ${formatoFechaLista.format(timestamp.toDate())}"
    } else {
        "$prefijo: —"
    }
}