package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.comisariatoproyecto.data.m_CreditoDetalle
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleCredito(
    credito: m_CreditoDetalle,
    onBack: () -> Unit
) {
    val repoCredito = remember { r_Creditos() }
    val scope       = rememberCoroutineScope()

    var mostrarDialogoCancelar by remember { mutableStateOf(false) }
    var cancelando             by remember { mutableStateOf(false) }
    var errorMsg               by remember { mutableStateOf<String?>(null) }

    val progreso = if (credito.plazoCuotas > 0)
        credito.cuotasPagadas.toFloat() / credito.plazoCuotas.toFloat()
    else 0f
    val porcentaje = (progreso * 100).toInt()

    if (mostrarDialogoCancelar) {
        AlertDialog(
            onDismissRequest = { if (!cancelando) mostrarDialogoCancelar = false },
            title = {
                Text(
                    text = "¿Cancelar solicitud?",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                Text(
                    text = "Esta acción no se puede deshacer. La solicitud de \"${credito.productoNombre}\" será cancelada.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        cancelando = true
                        scope.launch {
                            try {
                                repoCredito.cancelarReserva(credito.id)
                                mostrarDialogoCancelar = false
                                onBack()
                            } catch (e: Exception) {
                                errorMsg = "No se pudo cancelar: ${e.message}"
                                cancelando = false
                                mostrarDialogoCancelar = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancel),
                    enabled = !cancelando
                ) {
                    if (cancelando) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sí, cancelar", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCancelar = false }) {
                    Text("No, mantener", color = NavyPrimary)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DETALLE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = NavyPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Regresar",
                            tint = NavyPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = SurfaceBase
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            //  Sección producto
            Surface(modifier = Modifier.fillMaxWidth(), color = SurfaceWhite) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AsyncImage(
                        model = credito.productoImgUrl,
                        contentDescription = credito.productoNombre,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceBase),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = credito.productoNombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cantidad: ${credito.cantidad}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "L. ${String.format("%,.0f", credito.cuotaMensual)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = " /cuota",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = "TOTAL: L. ${String.format("%,.0f", credito.totalCredito)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Contenido según estado ────────────────────────────────────────
            when (credito.estado) {

                "Aprobado" -> {
                    // Progreso de cuotas
                    Surface(modifier = Modifier.fillMaxWidth(), color = SurfaceWhite) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${credito.cuotasPagadas} de ${credito.plazoCuotas} cuotas",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "$porcentaje%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progreso },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GreenPrimary,
                                trackColor = GreenContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (credito.saldoPendiente > 0) {
                                Text(
                                    text = "Saldo pendiente: L. ${String.format("%,.0f", credito.saldoPendiente)}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Historial de pagos
                    Surface(modifier = Modifier.fillMaxWidth(), color = SurfaceWhite) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HISTORIAL DE PAGOS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "SOLO PAGADOS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NavyPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            if (credito.cuotasPagadas == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceBase)
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.Inbox,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Sin cuotas pagadas",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "Las cuotas liquidadas aparecerán aquí.",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            } else {
                                repeat(credito.cuotasPagadas) { index ->
                                    FilaCuotaDetalle(
                                        numero = index + 1,
                                        monto  = credito.cuotaMensual
                                    )
                                    if (index < credito.cuotasPagadas - 1) {
                                        HorizontalDivider(
                                            color = DividerColor,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TarjetaInfoCredito(credito = credito, mostrarFechaAutoriza = true)
                }

                "Pendiente" -> {
                    BannerEstado(
                        icono    = Icons.Outlined.HourglassEmpty,
                        titulo   = "En revisión",
                        mensaje  = "Tu solicitud está siendo revisada por el comisariato. Te notificaremos cuando sea aprobada.",
                        colorFondo  = YellowContainer,
                        colorTexto  = YellowPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TarjetaInfoCredito(credito = credito, mostrarFechaAutoriza = false)
                }

                "Rechazado" -> {
                    BannerEstado(
                        icono    = Icons.Outlined.Cancel,
                        titulo   = "Solicitud rechazada",
                        mensaje  = "Esta solicitud no fue aprobada. Puedes comunicarte con el comisariato para más información.",
                        colorFondo  = RedContainer,
                        colorTexto  = RedCancel
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TarjetaInfoCredito(credito = credito, mostrarFechaAutoriza = true)
                }

                "Cancelado" -> {
                    BannerEstado(
                        icono    = Icons.Outlined.Block,
                        titulo   = "Solicitud cancelada",
                        mensaje  = "Cancelaste esta solicitud. Si lo necesitas, puedes hacer una nueva solicitud desde el catálogo.",
                        colorFondo  = SurfaceBase,
                        colorTexto  = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TarjetaInfoCredito(credito = credito, mostrarFechaAutoriza = false)
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = RedContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        color = RedCancel,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ── Botón cancelar — solo si estado == "Pendiente" ────────────────
            if (credito.estado == "Pendiente") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { mostrarDialogoCancelar = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancel),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedCancel),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cancelar solicitud",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Banner de estado (Pendiente / Rechazado / Cancelado) ──────────────────────
@Composable
fun BannerEstado(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    colorFondo: Color,
    colorTexto: Color
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = colorFondo) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorTexto,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mensaje,
                fontSize = 13.sp,
                color = colorTexto,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

// ── Tarjeta de datos financieros ──────────────────────────────────────────────
@Composable
fun TarjetaInfoCredito(credito: m_CreditoDetalle, mostrarFechaAutoriza: Boolean) {
    val fmt = SimpleDateFormat("d MMM yyyy", Locale("es"))
    Surface(modifier = Modifier.fillMaxWidth(), color = SurfaceWhite) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            FilaInfo("Plazo del crédito", "${credito.plazoCuotas} Meses")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            FilaInfo("Cuota mensual", "L. ${String.format("%,.0f", credito.cuotaMensual)}")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            FilaInfo("Total del crédito", "L. ${String.format("%,.0f", credito.totalCredito)}")
            credito.fechaRegistro?.let {
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                FilaInfo("Fecha de solicitud", fmt.format(it.toDate()))
            }
            if (mostrarFechaAutoriza) {
                credito.fechaAutoriza?.let {
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    FilaInfo("Fecha de autorización", fmt.format(it.toDate()))
                }
            }
        }
    }
}

// ── Fila genérica label / valor ───────────────────────────────────────────────
@Composable
fun FilaInfo(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
    }
}

// ── Fila de cuota pagada ──────────────────────────────────────────────────────
@Composable
fun FilaCuotaDetalle(numero: Int, monto: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Cuota #$numero",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NavyPrimary
                )
                Text(
                    text = "Pagada",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        Text(
            text = "L. ${String.format("%,.0f", monto)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GreenPrimary
        )
    }
}