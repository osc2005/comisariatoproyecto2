package com.example.comisariatoproyecto.ui.pantallas

import android.R.attr.onClick
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.Usuario
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.ui.theme.RedCancel
import com.example.comisariatoproyecto.ui.theme.SurfaceBase
import com.example.comisariatoproyecto.ui.theme.SurfaceWhite

// ── Colores locales ──────────────────────────────────────────────────────────
private val ColorGris      = Color(0xFF6B7280)
private val ColorNegro     = Color(0xFF111827)
private val ColorFondoIcon = Color(0xFFF1F5F9)
private val ColorBorde     = Color(0xFFE5E7EB)

// ── Tabs disponibles ─────────────────────────────────────────────────────────
private enum class PerfilTab(val label: String) {
    Personal("Personal"),
    Laboral("Laboral"),
    Cuenta("Cuenta")
}

@Composable
fun PerfilScreen(
    repo: r_permisos,
    onNavegaListaDeseos: () -> Unit = {},
    OnLogout: () -> Unit, // ← callback para navegar
) {
    var usuario    by remember { mutableStateOf<Usuario?>(null) }
    var empleado   by remember { mutableStateOf<Empleado?>(null) }
    var cargando   by remember { mutableStateOf(true) }
    var error      by remember { mutableStateOf("") }
    var reintentar by remember { mutableStateOf(0) }
    var tabActiva  by remember { mutableStateOf(PerfilTab.Personal) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(reintentar) {
        cargando = true; error = ""
        try {
            val (u, e) = repo.obtenerMiPerfilCompleto()
            if (u != null) { usuario = u; empleado = e }
            else error = "No se encontró tu perfil.\nVerifica que tu correo esté registrado."
        } catch (ex: Exception) {
            error = "Error: ${ex.localizedMessage ?: "Error desconocido"}"
        } finally { cargando = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBase)
    ) {
        when {
            // ── CARGANDO ─────────────────────────────────────────────────────
            cargando -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = NavyPrimary
            )

            // ── ERROR ─────────────────────────────────────────────────────────
            error.isNotEmpty() -> Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Warning, null,
                    tint = NavyPrimary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(error, color = ColorGris, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { reintentar++ },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) { Text("Reintentar", color = Color.White) }
            }

            // ── CONTENIDO ─────────────────────────────────────────────────────
            usuario != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── HEADER ────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = NavyPrimary,
                            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                        )
                        .padding(top = 48.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Avatar con inicial
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val inicial = empleado?.nombres
                                ?.firstOrNull()?.uppercaseChar()?.toString()
                                ?: usuario!!.nombre.firstOrNull()?.uppercaseChar()?.toString()
                                ?: "?"
                            Text(inicial, fontSize = 32.sp, fontWeight = FontWeight.Medium,
                                color = Color.White)
                        }

                        Spacer(Modifier.height(14.dp))

                        // Nombre completo
                        Text(
                            text = empleado?.nombreCompleto
                                ?: usuario!!.nombre.ifEmpty { "Sin nombre" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SurfaceWhite
                        )

                        Spacer(Modifier.height(2.dp))

                        // Código de empleado como subtítulo suave
                        Text(
                            text = empleado?.codigoEmpleado?.let { "# $it" } ?: "",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                }

                Spacer(Modifier.height(0.dp))

                // ── TABS ──────────────────────────────────────────────────────
                TabRow(
                    selectedTabIndex = tabActiva.ordinal,
                    containerColor   = Color.White,
                    contentColor     = NavyPrimary,
                    divider          = { HorizontalDivider(color = ColorBorde, thickness = 0.5.dp) },
                    indicator        = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabActiva.ordinal]),
                            height = 2.dp,
                            color  = NavyPrimary
                        )
                    }
                ) {
                    PerfilTab.entries.forEach { tab ->
                        Tab(
                            selected  = tabActiva == tab,
                            onClick   = { tabActiva = tab },
                            text      = {
                                Text(
                                    text       = tab.label,
                                    fontSize   = 13.sp,
                                    fontWeight = if (tabActiva == tab) FontWeight.SemiBold
                                    else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // ── CONTENIDO DE CADA TAB ─────────────────────────────────────
                AnimatedContent(
                    targetState    = tabActiva,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "tab_transition"
                ) { tab ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (tab) {

                            // ── TAB PERSONAL ──────────────────────────────────
                            PerfilTab.Personal -> {
                                SeccionLabel("Identificación")
                                TarjetaDato(Icons.Outlined.Badge,
                                    "Código de empleado", empleado?.codigoEmpleado ?: "—")
                                TarjetaDato(Icons.Outlined.AssignmentInd,
                                    "DNI", empleado?.dni ?: "—")
                                TarjetaDato(Icons.Outlined.Phone,
                                    "Teléfono", empleado?.telefono ?: "—")


                            }

                            // ── TAB LABORAL ───────────────────────────────────
                            PerfilTab.Laboral -> {
                              SeccionLabel("Datos laborales")
                                TarjetaDato(Icons.Outlined.MonetizationOn,
                                    "Salario", empleado?.salarioFormateado ?: "—")
                                TarjetaDato(Icons.Outlined.Work,
                                    "Departamento", empleado?.departamentoNombre?: "—")
                                  TarjetaDato(Icons.Outlined.DateRange,
                                    "Fecha de inicio", empleado?.fechaFormateada ?: "—")

                            }

                            // ── TAB CUENTA ────────────────────────────────────
                            PerfilTab.Cuenta -> {
                                SeccionLabel("Correos")
                                TarjetaDatoOculto(
                                    icono    = Icons.Outlined.Email,
                                    etiqueta = "Correo empresarial",
                                    valor    = usuario!!.correo
                                )
                                TarjetaDatoOculto(
                                    icono    = Icons.Outlined.AlternateEmail,
                                    etiqueta = "Correo personal",
                                    valor    = usuario!!.correoPersonal.ifEmpty { "—" }
                                )

                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── BOTÓN LISTA DE DESEOS ─────────────────────────────────────
                Button(
                    onClick = onNavegaListaDeseos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Outlined.Favorite, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Mi lista de deseos", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(5.dp))
                Button(
                    onClick = { mostrarDialogo = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancel)
                ) {
                    Icon(
                        Icons.Outlined.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Cerrar sesión", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

            }
        }
    }
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            containerColor   = Color.White,
            shape            = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFECEC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = null,
                        tint     = RedCancel,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text       = "¿Cerrar sesión?",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = Color(0xFF111827),
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text      = "Podés volver a ingresar en cualquier momento usando tu huella o PIN.",
                    fontSize  = 14.sp,
                    color     = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier  = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            mostrarDialogo = false
                            OnLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedCancel),
                        shape  = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cerrar sesión",
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                    }
                    OutlinedButton(
                        onClick = { mostrarDialogo = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape  = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text(
                            "Cancelar",
                            color      = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium,
                            fontSize   = 15.sp
                        )
                    }
                }
            }
        )
    }
}






// ── COMPOSABLES PRIVADOS ─────────────────────────────────────────────────────

@Composable
private fun SeccionLabel(texto: String) {
    Text(
        text       = texto,
        fontSize   = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color      = NavyPrimary,
        letterSpacing = 0.6.sp,
        modifier   = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun TarjetaDato(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorFondoIcon),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(etiqueta, fontSize = 11.sp, color = ColorGris)
                Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorNegro)
            }
        }
    }
}

/**
 * Tarjeta con botón para mostrar/ocultar el valor (correos).
 */
@Composable
private fun TarjetaDatoOculto(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    var visible by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de la izquierda
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ColorFondoIcon),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.width(14.dp))

            // Etiqueta + valor
            Column(modifier = Modifier.weight(1f)) {
                Text(etiqueta, fontSize = 11.sp, color = ColorGris)
                AnimatedContent(
                    targetState    = visible,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label          = "valor_correo"
                ) { mostrar ->
                    Text(
                        text       = if (mostrar) valor else "••••••••••••",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = if (mostrar) ColorNegro else ColorGris
                    )
                }
            }

            // Botón ojo
            IconButton(
                onClick  = { visible = !visible },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff
                    else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar" else "Mostrar",
                    tint     = ColorGris,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    etiqueta: String,
    valor: String
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = ColorFondoIcon),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(etiqueta, fontSize = 11.sp, color = ColorGris)
            Spacer(Modifier.height(4.dp))
            Text(valor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ColorNegro)
        }
    }
}

// Extensión de tabulación (necesaria para el indicador del TabRow)
private fun Modifier.tabIndicatorOffset(
    currentTabPosition: TabPosition
): Modifier = this.then(
    Modifier
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = currentTabPosition.left)
        .width(currentTabPosition.width)
)