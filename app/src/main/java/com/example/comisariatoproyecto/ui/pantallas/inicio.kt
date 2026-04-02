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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.theme.GreenContainer
import com.example.comisariatoproyecto.ui.theme.GreenPrimary
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.NavyContainer
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite
import com.example.comisariatoproyecto.ui.theme.TextSecondary





private val BorderSubtle   = Color(0xFFDDE3EB)
private val TextHint       = Color(0xFF8C97A8)

// ─── Pantalla principal ───────────────────────────────────────────────────────
@Composable
fun PantallaInicio(
    repo: r_permisos,
    repoCreditos: r_Creditos,
    onLogout: () -> Unit
) {
    var empleado by remember { mutableStateOf<Empleado?>(null) }
    var porcentajeCfg by remember { mutableDoubleStateOf(0.0) }
    var utilizadoReal by remember { mutableDoubleStateOf(0.0) }
    var cargando by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        cargando = true
        // 1. Cargar perfil
        empleado = repo.obtenerMiPerfilEmpleado()

        // 2. Cargar configuración y créditos si hay empleado
        empleado?.let { emp ->
            porcentajeCfg = repoCreditos.obtenerConfiguracionCredito()
            utilizadoReal = repoCreditos.obtenerCreditoUtilizadoReal(emp.codigoEmpleado)
        }
        cargando = false
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
        HeaderInicio(empleado = empleado, onLogout = onLogout)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            EtiquetaSeccion("Línea de crédito")
            Spacer(Modifier.height(8.dp))
            TarjetaLineaCredito(
                empleado = empleado,
                porcentajeLimite = porcentajeCfg,
                utilizado = utilizadoReal
            )

            Spacer(Modifier.height(20.dp))

            EtiquetaSeccion("Mis reservas")
            Spacer(Modifier.height(8.dp))
            FilaResumenReservas(pendientes = 3, activas = 5)

            Spacer(Modifier.height(20.dp))

            // Envolver en `if (tieneNotificacion)` cuando sea dinámico
            TarjetaNotificacion()
            Spacer(Modifier.height(20.dp))

            EtiquetaSeccion("Acceso rápido")
            Spacer(Modifier.height(8.dp))
            FilaAccesoRapido()

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ─── Etiqueta de sección ──────────────────────────────────────────────────────
@Composable
fun EtiquetaSeccion(texto: String) {
    Text(
        text = texto.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        color = TextSecondary
    )
}

// ─── Tarjeta línea de crédito ─────────────────────────────────────────────────
@Composable
fun TarjetaLineaCredito(
    empleado: Empleado?,
    porcentajeLimite: Double,
    utilizado: Double
) {
    // 1. Convertimos salario a Double explícitamente para evitar el error en la multiplicación
    val salario = empleado?.salario?.toDouble() ?: 0.0
    val limiteCalculado = salario * porcentajeLimite

    // 2. Calculamos el porcentaje de la barra (0.0 a 1.0)
    val porcentajeBarra = if (limiteCalculado > 0.0) {
        (utilizado / limiteCalculado).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    val disponible = (limiteCalculado - utilizado).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Disponible",
                fontSize = 11.sp,
                color = SurfaceWhite.copy(alpha = 0.6f)
            )
            Text(
                text = "L. ${"%,.2f".format(disponible)}",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = SurfaceWhite,
                letterSpacing = (-0.3).sp,
                lineHeight = 34.sp
            )
            // 🔥 CORREGIDO: Usamos limiteCalculado en lugar de 'limite'
            Text(
                text = "de L. ${"%,.2f".format(limiteCalculado)} de límite",
                fontSize = 11.sp,
                color = SurfaceWhite.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                // 🔥 CORREGIDO: Usamos porcentajeBarra en lugar de 'porcentaje'
                progress = { porcentajeBarra },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GreenPrimary,
                trackColor = SurfaceWhite.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Utilizado  L. ${"%,.2f".format(utilizado)}",
                    fontSize = 11.sp,
                    color = SurfaceWhite.copy(alpha = 0.6f)
                )
                // 🔥 CORREGIDO: Usamos porcentajeBarra para el texto
                Text(
                    text = "${(porcentajeBarra * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = SurfaceWhite.copy(alpha = 0.45f)
                )
            }
        }
    }
}

// ─── Resumen de reservas ──────────────────────────────────────────────────────
@Composable
fun FilaResumenReservas(pendientes: Int, activas: Int) {
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
            icono     = Icons.Outlined.Schedule
        )
        CardConteo(
            modifier  = Modifier.weight(1f),
            cantidad  = activas,
            etiqueta  = "Activas",
            sublabel  = "En pago",
            iconoBg   = GreenContainer,
            iconoTint = GreenPrimary,
            icono     = Icons.Outlined.CheckCircle
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
    icono: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, BorderSubtle)
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
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconoTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = cantidad.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyPrimary,
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GreenContainer),
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
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smartphone Galaxy A54 aprobado",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GreenPrimary
                )
                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick = { },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text(
                        text = "Ver detalle →",
                        color = GreenPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = { },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    tint = GreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── Acceso rápido ────────────────────────────────────────────────────────────
@Composable
fun FilaAccesoRapido() {
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
            onClick   = { /* navegar a catálogo */ }
        )
        CardAcceso(
            modifier  = Modifier.weight(1f),
            etiqueta  = "Mis reservas",
            sublabel  = "Historial",
            iconoBg   = GreenContainer,
            iconoTint = GreenPrimary,
            icono     = Icons.Outlined.Description,
            onClick   = { /* navegar a reservas */ }
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
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, BorderSubtle),
        onClick = onClick
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
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconoTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = etiqueta,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyPrimary
            )
            Text(
                text = "$sublabel ↗",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

// ─── Header intacto ───────────────────────────────────────────────────────────
@Composable
fun HeaderInicio(
    empleado: Empleado?,
    onLogout: () -> Unit
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comisariato",
                    color = SurfaceWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceWhite.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = SurfaceWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onLogout() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceWhite.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = SurfaceWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Hola, ${empleado?.nombreCompleto ?: "Usuario"}",
                color = SurfaceWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bienvenido de vuelta.",
                color = SurfaceWhite.copy(alpha = 0.85f),
                fontSize = 13.sp
            )
        }
    }
}