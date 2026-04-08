package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.TaskAlt
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
import com.example.comisariatoproyecto.data.m_Reseña
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_Reseñas
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

private val filtrosCredito = listOf("Todos", "Pendiente", "Aprobado", "Pagado", "Rechazado", "Cancelado")
private val formatoFechaLista = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "HN"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCredito(
    filtroInicial: String = "Todos",
    onVerDetalle: (m_CreditoDetalle) -> Unit = {},
    onOpinar: (m_CreditoDetalle) -> Unit = {}
) {
    val repoAuth    = remember { r_permisos() }
    val repoCredito = remember { r_Creditos() }
    val repoReseñas = remember { r_Reseñas() }

    var filtroSeleccionado by remember { mutableStateOf(filtroInicial) }
    var expanded           by remember { mutableStateOf(false) }
    var creditos           by remember { mutableStateOf<List<m_CreditoDetalle>>(emptyList()) }
    var misReseñas         by remember { mutableStateOf<List<m_Reseña>>(emptyList()) }
    var cargando           by remember { mutableStateOf(true) }
    var errorMsg           by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val empleado   = repoAuth.obtenerMiPerfilEmpleado()
            val empleadoId = empleado?.codigoEmpleado

            if (empleadoId.isNullOrEmpty()) {
                errorMsg = "No se encontró el perfil del empleado."
                cargando = false
                return@LaunchedEffect
            }

            // Escuchar créditos
            repoCredito.obtenerReservasDetalladas(empleadoId).collect { lista ->
                creditos = lista
                cargando = false
            }
        } catch (e: Exception) {
            errorMsg = "Error al cargar créditos: ${e.message}"
            cargando = false
        }
    }

    // Escuchar reseñas del empleado para saber cuáles ya opinó
    LaunchedEffect(Unit) {
        repoAuth.obtenerMiPerfilEmpleado()?.codigoEmpleado?.let { id ->
            repoReseñas.obtenerReseñasDeEmpleado(id).collect { lista ->
                misReseñas = lista
            }
        }
    }

    val creditosFiltrados = creditos
        .filter { filtroSeleccionado == "Todos" || it.estado == filtroSeleccionado }
        .sortedWith(
            compareBy<m_CreditoDetalle> {
                when {
                    it.estado == "Aprobado" && it.estadoCredito == "Activo" -> 0
                    it.estado == "Pendiente" -> 1
                    else -> 2
                }
            }.thenByDescending { it.fechaRegistro }
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
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
                text = "Consultá el estado de tus solicitudes y gestioná tus créditos activos.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = filtroSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filtrar por estado", fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = NavyPrimary.copy(alpha = 0.2f),
                        focusedLabelColor = NavyPrimary
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceWhite)
                ) {
                    filtrosCredito.forEach { filtro ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = filtro,
                                    fontSize = 14.sp,
                                    color = if (filtro == filtroSeleccionado) NavyPrimary else TextSecondary,
                                    fontWeight = if (filtro == filtroSeleccionado) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            onClick = {
                                filtroSeleccionado = filtro
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NavyPrimary)
            } else if (errorMsg != null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 40.sp)
                    Text(errorMsg ?: "", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    if (creditosFiltrados.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📋", fontSize = 48.sp)
                                Text("No tenés créditos en este estado", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                                Text("Podés solicitar uno desde el Catálogo", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    } else {
                        creditosFiltrados.forEach { credito ->
                            // Buscamos si ya opinó sobre este crédito específico
                            val yaOpino = misReseñas.any { it.creditoId == credito.id }
                            
                            TarjetaCreditoLista(
                                credito = credito, 
                                onClick = { onVerDetalle(credito) },
                                yaOpino = yaOpino,
                                onOpinar = { onOpinar(credito) }
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

@Composable
fun TarjetaCreditoLista(
    credito: m_CreditoDetalle, 
    onClick: () -> Unit,
    yaOpino: Boolean,
    onOpinar: () -> Unit
) {
    val config = estadoConfigCredito(credito.estado)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = credito.productoImgUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceBase),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(credito.productoNombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(6.dp), color = config.containerColor) {
                            Text(credito.estado.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = config.color, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formatearFechaCredito(credito), fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    AccionRapidaCredito(credito = credito)
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }

            // BOTÓN DE OPINAR: Solo para Aprobado/Pagado y si no ha opinado
            if ((credito.estado == "Aprobado" || credito.estado == "Pagado") && !yaOpino) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = DividerColor, thickness = 0.5.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpinar() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.StarOutline, null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Opinar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun AccionRapidaCredito(credito: m_CreditoDetalle) {
    when (credito.estado) {
        "Aprobado" -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CreditCard, null, tint = if (credito.estadoCredito == "Activo") GreenPrimary else TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (credito.estadoCredito == "Activo") "• CRÉDITO ACTIVO" else "• CRÉDITO INACTIVO", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (credito.estadoCredito == "Activo") GreenPrimary else TextSecondary)
            }
        }
        "Pagado" -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("• TOTALMENTE CANCELADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
            }
        }
        "Pendiente" -> Text("En revisión institucional", fontSize = 11.sp, color = OrangeWarning, fontWeight = FontWeight.Medium)
        "Rechazado" -> Text("No cumple con los requisitos", fontSize = 11.sp, color = RedCancel, fontWeight = FontWeight.SemiBold)
        else -> { }
    }
}

data class EstadoVisualCredito(val color: Color, val containerColor: Color, val icono: ImageVector)

fun estadoConfigCredito(estado: String): EstadoVisualCredito = when (estado) {
    "Aprobado"  -> EstadoVisualCredito(GreenPrimary, GreenContainer, Icons.Outlined.CheckCircle)
    "Pagado"    -> EstadoVisualCredito(Color(0xFF3B82F6), Color(0xFFEFF6FF), Icons.Outlined.TaskAlt)
    "Pendiente" -> EstadoVisualCredito(OrangeWarning, OrangeContainer, Icons.Outlined.HourglassEmpty)
    "Rechazado" -> EstadoVisualCredito(RedCancel, RedContainer, Icons.Outlined.Cancel)
    else        -> EstadoVisualCredito(GrayNeutral, GrayContainer, Icons.Outlined.Info)
}

fun formatearFechaCredito(credito: m_CreditoDetalle): String {
    val timestamp = if (credito.estado in listOf("Aprobado", "Rechazado", "Pagado")) credito.fechaAutoriza ?: credito.fechaRegistro else credito.fechaRegistro
    val prefijo = if (credito.estado in listOf("Aprobado", "Rechazado", "Pagado")) "Autorizado" else "Registrado"
    return if (timestamp != null) "$prefijo: ${formatoFechaLista.format(timestamp.toDate())}" else "$prefijo: —"
}
