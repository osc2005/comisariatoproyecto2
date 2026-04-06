package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.comisariatoproyecto.MainActivity
import com.example.comisariatoproyecto.mostrarNotificacion
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.NotificacionReserva
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.theme.GreenContainer
import com.example.comisariatoproyecto.ui.theme.GreenPrimary
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.NavyContainer
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary
import com.example.comisariatoproyecto.utils.SessionPrefs

private val BorderSubtle = Color(0xFFDDE3EB)
private val TextHint     = Color(0xFF8C97A8)

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

    // notificacionesPendientes = solo las NO leídas, se muestran en badge y popup
    var notificacionesPendientes by remember { mutableStateOf<List<NotificacionReserva>>(emptyList()) }
    var mostrarPopupNotif        by remember { mutableStateOf(false) }

    // Función reutilizable para marcar como leídas y limpiar el badge
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

            // 1. Traer reservas Aprobadas/Canceladas de Firestore
            val todasLasNotif = repoCreditos.obtenerReservasParaNotificar(emp.codigoEmpleado)

            // 2. Filtrar las que el usuario ya leyó en el popup
            val idsLeidos = sessionPrefs.obtenerIdsLeidos()
            val nuevas    = todasLasNotif.filter { it.id !in idsLeidos }
            notificacionesPendientes = nuevas

            // 3. Disparar notificación Android solo para las que no se han notificado aún
            val idsYaNotificados = sessionPrefs.obtenerIdsNotificados()
            val paraNotificar    = nuevas.filter { it.id !in idsYaNotificados }
            paraNotificar.forEach { notif ->
                activity?.let { act ->
                    val titulo  = if (notif.estado == "Aprobado") "✅ Reserva aprobada" else "❌ Reserva Rechazada"
                    val mensaje = "Tu reserva de \"${notif.productoNombre}\" fue ${notif.estado.lowercase()}."
                    mostrarNotificacion(act, titulo, mensaje)
                }
            }

            // 4. Marcar como ya notificadas por Android (para no repetir la notif del sistema)
            if (paraNotificar.isNotEmpty()) {
                sessionPrefs.guardarIdsNotificados(
                    idsYaNotificados + paraNotificar.map { it.id }.toSet()
                )
            }
        }

        cargando = false
    }

    // ── Popup de notificaciones ───────────────────────────────────────────────
    if (mostrarPopupNotif) {
        Dialog(onDismissRequest = { marcarComoLeidas() }) {
            Card(
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "Notificaciones",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp,
                            color      = NavyPrimary
                        )
                        IconButton(
                            onClick  = { marcarComoLeidas() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (notificacionesPendientes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin notificaciones nuevas",
                                color    = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        notificacionesPendientes.forEach { notif ->
                            val esAprobado = notif.estado == "Aprobado"
                            val colorFondo = if (esAprobado) GreenContainer   else Color(0xFFFFEDED)
                            val colorTexto = if (esAprobado) GreenPrimary     else Color(0xFFDC2626)
                            val icono      = if (esAprobado) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
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

                        Spacer(Modifier.height(4.dp))

                        // Botón "Marcar todas como leídas" al fondo
                        TextButton(
                            onClick  = { marcarComoLeidas() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                "Marcar como leídas",
                                fontSize = 12.sp,
                                color    = NavyPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (cargando) {
        Box(
            modifier = Modifier.fillMaxSize().background(SurfaceBase),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NavyPrimary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderInicio(
            empleado            = empleado,
            onLogout            = onLogout,
            cantidadNotif       = notificacionesPendientes.size,
            onVerNotificaciones = { mostrarPopupNotif = true }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            EtiquetaSeccion("Línea de crédito")
            Spacer(Modifier.height(8.dp))
            TarjetaLineaCredito(
                empleado         = empleado,
                porcentajeLimite = porcentajeCfg,
                utilizado        = utilizadoReal
            )

            Spacer(Modifier.height(20.dp))

            EtiquetaSeccion("Mis reservas")
            Spacer(Modifier.height(8.dp))
            FilaResumenReservas(
                pendientes = conteoReservas.first,
                activas    = conteoReservas.second
            )

            Spacer(Modifier.height(20.dp))

            TarjetaNotificacion()
            Spacer(Modifier.height(20.dp))

            EtiquetaSeccion("Acceso rápido")
            Spacer(Modifier.height(8.dp))
            FilaAccesoRapido(
                onIrCatalogo = onIrCatalogo,
                onIrCredito  = onIrCredito
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ─── Etiqueta de sección ──────────────────────────────────────────────────────
@Composable
fun EtiquetaSeccion(texto: String) {
    Text(
        text          = texto.uppercase(),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.Medium,
        letterSpacing = 1.sp,
        color         = TextSecondary
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
    val porcentajeBarra = if (limiteCalculado > 0.0) {
        (utilizado / limiteCalculado).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    val disponible = (limiteCalculado - utilizado).coerceAtLeast(0.0)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Disponible", fontSize = 11.sp, color = SurfaceWhite.copy(alpha = 0.6f))
            Text(
                text          = "L. ${"%,.2f".format(disponible)}",
                fontSize      = 30.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = SurfaceWhite,
                letterSpacing = (-0.3).sp,
                lineHeight    = 34.sp
            )
            Text(
                text     = "de L. ${"%,.2f".format(limiteCalculado)} de límite",
                fontSize = 11.sp,
                color    = SurfaceWhite.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress   = { porcentajeBarra },
                modifier   = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
                color      = GreenPrimary,
                trackColor = SurfaceWhite.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text     = "Utilizado  L. ${"%,.2f".format(utilizado)}",
                    fontSize = 11.sp,
                    color    = SurfaceWhite.copy(alpha = 0.6f)
                )
                Text(
                    text     = "${(porcentajeBarra * 100).toInt()}%",
                    fontSize = 11.sp,
                    color    = SurfaceWhite.copy(alpha = 0.45f)
                )
            }
        }
    }
}

// ─── Resumen de reservas ──────────────────────────────────────────────────────
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
            iconoBg   = NavyContainer.copy(alpha = 0.1f),
            iconoTint = NavyContainer,
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
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(0.5.dp, BorderSubtle),
        onClick   = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconoBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icono,
                    contentDescription = null,
                    tint               = iconoTint,
                    modifier           = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text       = cantidad.toString(),
                fontSize   = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color      = NavyPrimary,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(text = etiqueta, fontSize = 11.sp, color = TextSecondary)
            Text(text = sublabel,  fontSize = 10.sp, color = TextHint)
        }
    }
}

// ─── Notificación condicional ─────────────────────────────────────────────────
@Composable
fun TarjetaNotificacion() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = GreenContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Check,
                    contentDescription = null,
                    tint               = SurfaceWhite,
                    modifier           = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Smartphone Galaxy A54 aprobado",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = GreenPrimary
                )
                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick        = { },
                    contentPadding = PaddingValues(0.dp),
                    modifier       = Modifier.height(20.dp)
                ) {
                    Text(
                        text       = "Ver detalle →",
                        color      = GreenPrimary,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick  = { },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    tint               = GreenPrimary,
                    modifier           = Modifier.size(16.dp)
                )
            }
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
            sublabel  = "Categorías",
            iconoBg   = NavyPrimary.copy(alpha = 0.08f),
            iconoTint = NavyPrimary,
            icono     = Icons.Outlined.ShoppingBag,
            onClick   = onIrCatalogo
        )
        CardAcceso(
            modifier  = Modifier.weight(1f),
            etiqueta  = "Mis reservas",
            sublabel  = "Historial",
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
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(0.5.dp, BorderSubtle),
        onClick   = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconoBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icono,
                    contentDescription = null,
                    tint               = iconoTint,
                    modifier           = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text       = etiqueta,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = NavyPrimary
            )
            Text(
                text     = "$sublabel ↗",
                fontSize = 10.sp,
                color    = TextSecondary
            )
        }
    }
}

// ─── Header con badge en campana ──────────────────────────────────────────────
@Composable
fun HeaderInicio(
    empleado: Empleado?,
    onLogout: () -> Unit,
    cantidadNotif: Int = 0,
    onVerNotificaciones: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Comisariato",
                    color      = SurfaceWhite,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // ── Campana con badge ─────────────────────────────────────
                    Box {
                        IconButton(
                            onClick  = onVerNotificaciones,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceWhite.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector        = Icons.Outlined.Notifications,
                                contentDescription = "Notificaciones",
                                tint               = SurfaceWhite,
                                modifier           = Modifier.size(20.dp)
                            )
                        }

                        // Badge rojo — desaparece cuando cantidadNotif == 0
                        if (cantidadNotif > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = if (cantidadNotif > 9) "9+" else cantidadNotif.toString(),
                                    color      = SurfaceWhite,
                                    fontSize   = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick  = { onLogout() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceWhite.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint               = SurfaceWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text       = "Hola, ${empleado?.nombreCompleto ?: "Usuario"}",
                color      = SurfaceWhite,
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text     = "Bienvenido de vuelta.",
                color    = SurfaceWhite.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        }
    }
}