package com.example.comisariatoproyecto.ui.pantallas


// 🔹 Animaciones (CORRECTOS)
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring

// 🔹 Layouts y UI base
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke

// 🔹 Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*

// 🔹 Material 3
import androidx.compose.material3.*

// 🔹 Runtime
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// 🔹 UI helpers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// 🔹 App
import com.example.comisariatoproyecto.MainActivity

// 🔹 Data
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.NotificacionReserva
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_permisos

// 🔹 Theme
import com.example.comisariatoproyecto.ui.theme.GreenContainer
import com.example.comisariatoproyecto.ui.theme.GreenPrimary
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.NavyContainer
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary

// 🔹 Utils
import com.example.comisariatoproyecto.utils.SessionPrefs

private val BorderSubtle  = Color(0xFFDDE3EB)
private val TextHint      = Color(0xFF8C97A8)
private val PageBg        = Color(0xFFEEF1F8)

// ─── Pantalla principal ───────────────────────────────────────────────────────
@Composable
fun PantallaInicio(
    repo: r_permisos,
    repoCreditos: r_Creditos,
    onLogout: () -> Unit,
    onIrCatalogo: () -> Unit,
    onIrCredito: () -> Unit
) {
    val context      = LocalContext.current
    val activity     = context as? MainActivity
    val sessionPrefs = remember { SessionPrefs(context) }

    var empleado       by remember { mutableStateOf<Empleado?>(null) }
    var porcentajeCfg  by remember { mutableDoubleStateOf(0.0) }
    var utilizadoReal  by remember { mutableDoubleStateOf(0.0) }
    var cargando       by remember { mutableStateOf(true) }
    var conteoReservas by remember { mutableStateOf(Pair(0, 0)) }

    var notificacionesPendientes by remember { mutableStateOf<List<NotificacionReserva>>(emptyList()) }
    var mostrarPopupNotif        by remember { mutableStateOf(false) }

    // Marcar como leídas SOLO con el botón — no al cerrar el dialog
    fun marcarComoLeidas() {
        val idsLeidos = sessionPrefs.obtenerIdsLeidos()
        sessionPrefs.guardarIdsLeidos(idsLeidos + notificacionesPendientes.map { it.id }.toSet())
        notificacionesPendientes = emptyList()
        mostrarPopupNotif = false
    }

    LaunchedEffect(Unit) {
        cargando = true
        empleado = repo.obtenerMiPerfilEmpleado()
        empleado?.let { emp ->
            porcentajeCfg  = repoCreditos.obtenerConfiguracionCredito()
            utilizadoReal  = repoCreditos.obtenerCreditoUtilizadoReal(emp.codigoEmpleado)
            conteoReservas = repoCreditos.obtenerConteoReservas(emp.codigoEmpleado)

            val todasLasNotif = repoCreditos.obtenerReservasParaNotificar(emp.codigoEmpleado)
            val idsLeidos     = sessionPrefs.obtenerIdsLeidos()
            val nuevas        = todasLasNotif.filter { it.id !in idsLeidos }
            notificacionesPendientes = nuevas

            val idsYaNotificados = sessionPrefs.obtenerIdsNotificados()
            val paraNotificar    = nuevas.filter { it.id !in idsYaNotificados }
            paraNotificar.forEach { notif ->
                activity?.let { act ->
                    val titulo  = if (notif.estado == "Aprobado") "Reserva aprobada" else "Reserva rechazada"
                    val mensaje = "Tu reserva de \"${notif.productoNombre}\" fue ${notif.estado.lowercase()}."
                }
            }
            if (paraNotificar.isNotEmpty()) {
                sessionPrefs.guardarIdsNotificados(
                    idsYaNotificados + paraNotificar.map { it.id }.toSet()
                )
            }
        }
        cargando = false
    }

    // ── Popup notificaciones ──────────────────────────────────────────────────
    if (mostrarPopupNotif) {
        Dialog(
            // Cerrar con X o botón NO marca como leídas — solo el botón lo hace
            onDismissRequest = { mostrarPopupNotif = false }
        ) {
            Card(
                shape  = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Handle visual
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BorderSubtle)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "Notificaciones",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 16.sp,
                            color      = NavyPrimary
                        )
                        IconButton(
                            onClick  = { mostrarPopupNotif = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Cerrar",
                                tint     = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (notificacionesPendientes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin notificaciones nuevas",
                                color    = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // ── Lista con scroll cuando hay más de 9 notificaciones ──
                        Column(
                            modifier = Modifier
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            notificacionesPendientes.forEach { notif ->
                                val esAprobado = notif.estado == "Aprobado"
                                val colorFondo = if (esAprobado) GreenContainer else Color(0xFFFFF1F1)
                                val colorTexto = if (esAprobado) GreenPrimary   else Color(0xFFDC2626)
                                val icono      = if (esAprobado) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colorFondo)
                                        .padding(12.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector        = icono,
                                        contentDescription = null,
                                        tint               = colorTexto,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text       = notif.productoNombre,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize   = 13.sp,
                                            color      = NavyPrimary
                                        )
                                        Text(
                                            text     = "Reserva ${notif.estado.lowercase()}",
                                            fontSize = 12.sp,
                                            color    = colorTexto
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick  = { marcarComoLeidas() },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text(
                                "Marcar todas como leídas",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Loading ───────────────────────────────────────────────────────────────
    if (cargando) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NavyPrimary)
        }
        return
    }

    // ── Contenido principal ───────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderInicio(
            empleado            = empleado,
            cantidadNotif       = notificacionesPendientes.size,
            onVerNotificaciones = { mostrarPopupNotif = true }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            EtiquetaSeccion("Línea de crédito")
            Spacer(Modifier.height(8.dp))
            TarjetaLineaCredito(
                empleado         = empleado,
                porcentajeLimite = porcentajeCfg,
                utilizado        = utilizadoReal
            )

            Spacer(Modifier.height(4.dp))
            EtiquetaSeccion("Mis reservas")
            Spacer(Modifier.height(8.dp))
            FilaResumenReservas(
                pendientes = conteoReservas.first,
                activas    = conteoReservas.second
            )

            Spacer(Modifier.height(4.dp))
            EtiquetaSeccion("Acceso rápido")
            Spacer(Modifier.height(8.dp))
            FilaAccesoRapido(
                onIrCatalogo = onIrCatalogo,
                onIrCredito  = onIrCredito
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────
@Composable
fun HeaderInicio(
    empleado: Empleado?,
    cantidadNotif: Int = 0,
    onVerNotificaciones: () -> Unit = {}
) {
    val iniciales = empleado?.nombreCompleto
        ?.split(" ")
        ?.take(2)
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.joinToString("")
        ?: "U"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 36.dp)) {

            // Fila superior: marca + campana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary)
                    )
                    Text(
                        "Comisariato",
                        color    = SurfaceWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box {
                    IconButton(
                        onClick  = onVerNotificaciones,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint     = SurfaceWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = cantidadNotif > 0,
                        enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                        exit    = scaleOut(),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(x = 2.dp, y = (-2).dp)
                                .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text     = if (cantidadNotif > 9) "9+" else cantidadNotif.toString(),
                                color    = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Avatar + nombre
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = iniciales,
                        color      = SurfaceWhite,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(
                        "Bienvenido de vuelta",
                        color    = SurfaceWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Text(
                        empleado?.nombreCompleto ?: "Usuario",
                        color      = SurfaceWhite,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Curva inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(PageBg)
        )
    }
}

// ─── Etiqueta sección ─────────────────────────────────────────────────────────
@Composable
fun EtiquetaSeccion(texto: String) {
    Text(
        text          = texto.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Medium,
        letterSpacing = 1.2.sp,
        color         = TextSecondary,
        modifier      = Modifier.padding(top = 20.dp, bottom = 0.dp)
    )
}

// ─── Tarjeta línea de crédito ─────────────────────────────────────────────────
@Composable
fun TarjetaLineaCredito(
    empleado: Empleado?,
    porcentajeLimite: Double,
    utilizado: Double
) {
    val salario         = empleado?.salario?.toDouble() ?: 0.0
    val limiteCalculado = salario * porcentajeLimite
    val porcentajeBarra = if (limiteCalculado > 0.0)
        (utilizado / limiteCalculado).coerceIn(0.0, 1.0).toFloat()
    else 0f
    val disponible = (limiteCalculado - utilizado).coerceAtLeast(0.0)

    // Animación de la barra de progreso
    val animProgress by animateFloatAsState(
        targetValue   = porcentajeBarra,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label         = "progreso"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyPrimary),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box {
            // Decoración de fondo
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .offset(x = 260.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(GreenPrimary.copy(alpha = 0.10f))
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .offset(x = 290.dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            Column(modifier = Modifier.padding(22.dp)) {
                Text("Disponible", fontSize = 11.sp, color = SurfaceWhite.copy(alpha = 0.5f))
                Text(
                    text          = "L. ${"%,.2f".format(disponible)}",
                    fontSize      = 30.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = SurfaceWhite,
                    letterSpacing = (-0.5).sp,
                    lineHeight    = 34.sp
                )
                Text(
                    text     = "de L. ${"%,.2f".format(limiteCalculado)} límite total",
                    fontSize = 11.sp,
                    color    = SurfaceWhite.copy(alpha = 0.35f)
                )

                Spacer(Modifier.height(18.dp))

                LinearProgressIndicator(
                    progress   = { animProgress },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color      = GreenPrimary,
                    trackColor = SurfaceWhite.copy(alpha = 0.12f)
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Utilizado  L. ${"%,.2f".format(utilizado)}",
                        fontSize = 11.sp,
                        color    = SurfaceWhite.copy(alpha = 0.5f)
                    )
                    Text(
                        "${(porcentajeBarra * 100).toInt()}%",
                        fontSize = 11.sp,
                        color    = SurfaceWhite.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ─── Resumen reservas ─────────────────────────────────────────────────────────
@Composable
fun FilaResumenReservas(
    pendientes: Int,
    activas: Int,
    onVerPendientes: () -> Unit = {},
    onVerActivas: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CardConteo(
            modifier  = Modifier.weight(1f),
            cantidad  = pendientes,
            etiqueta  = "Pendientes",
            sublabel  = "Por revisar",
            iconoBg   = Color(0xFFEEF0FE),
            iconoTint = Color(0xFF3C3489),
            icono     = Icons.Outlined.Schedule,
            onClick   = onVerPendientes
        )
        CardConteo(
            modifier  = Modifier.weight(1f),
            cantidad  = activas,
            etiqueta  = "Activas",
            sublabel  = "En pago",
            iconoBg   = GreenContainer,
            iconoTint = GreenPrimary,
            icono     = Icons.Outlined.CheckCircle,
            onClick   = onVerActivas
        )
    }
}

@Composable
fun CardConteo(
    modifier: Modifier = Modifier,
    cantidad: Int,
    etiqueta: String,
    sublabel: String,
    iconoBg: Color,
    iconoTint: Color,
    icono: ImageVector,
    onClick: () -> Unit = {}
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(0.5.dp, BorderSubtle),
        onClick   = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconoBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = iconoTint, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                cantidad.toString(),
                fontSize   = 26.sp,
                fontWeight = FontWeight.Medium,
                color      = NavyPrimary,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(etiqueta, fontSize = 12.sp, color = TextSecondary)
            Text(sublabel, fontSize = 10.sp,  color = Color(0xFFB5BEC9))
        }
    }
}

// ─── Acceso rápido ────────────────────────────────────────────────────────────
@Composable
fun FilaAccesoRapido(
    onIrCatalogo: () -> Unit,
    onIrCredito: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CardAcceso(
            modifier  = Modifier.weight(1f),
            etiqueta  = "Explorar",
            sublabel  = "Catálogo ↗",
            iconoBg   = Color(0xFFEEF0FE),
            iconoTint = Color(0xFF3C3489),
            icono     = Icons.Outlined.ShoppingBag,
            onClick   = onIrCatalogo
        )
        CardAcceso(
            modifier  = Modifier.weight(1f),
            etiqueta  = "Mis reservas",
            sublabel  = "Historial ↗",
            iconoBg   = GreenContainer,
            iconoTint = GreenPrimary,
            icono     = Icons.Outlined.Description,
            onClick   = onIrCredito
        )
    }
}

@Composable
fun CardAcceso(
    modifier: Modifier = Modifier,
    etiqueta: String,
    sublabel: String,
    iconoBg: Color,
    iconoTint: Color,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(0.5.dp, BorderSubtle),
        onClick   = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconoBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = iconoTint, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
            Text(sublabel, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

// ─── Alias de color para uso interno ─────────────────────────────────────────
private val SurfaceWhite = Color(0xFFFFFFFF)