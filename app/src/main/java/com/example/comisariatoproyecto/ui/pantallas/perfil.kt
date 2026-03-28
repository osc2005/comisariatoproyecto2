package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.AssignmentInd
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
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

private val ColorCafe  = Color(0xFF8B5A2B)
private val ColorCrema = Color(0xFFFFF8F0)
private val ColorGris  = Color(0xFF6B7280)
private val ColorNegro = Color(0xFF111827)
private val VerdeOk    = Color(0xFF22C55E)

@Composable
fun PerfilScreen(repo: r_permisos) {

    var usuario  by remember { mutableStateOf<Usuario?>(null) }
    var empleado by remember { mutableStateOf<Empleado?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error    by remember { mutableStateOf("") }
    var reintentar by remember { mutableStateOf(0) }

    LaunchedEffect(reintentar) {
        cargando = true
        error = ""
        try {
            val (u, e) = repo.obtenerMiPerfilCompleto()
            if (u != null) {
                usuario  = u
                empleado = e
            } else {
                error = "No se encontró tu perfil.\nVerifica que tu correo esté registrado."
            }
        } catch (ex: Exception) {
            error = "Error: ${ex.localizedMessage ?: "Error desconocido"}"
        } finally {
            cargando = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorCrema)
    ) {
        when {
            // ── CARGANDO ────────────────────────────────────────────────────
            cargando -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ColorCafe
                )
            }

            // ── ERROR ────────────────────────────────────────────────────────
            error.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = ColorCafe,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = ColorGris,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { reintentar++ },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorCafe)
                    ) {
                        Text("Reintentar", color = Color.White)
                    }
                }
            }

            // ── CONTENIDO ────────────────────────────────────────────────────
            usuario != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // HEADER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = ColorCafe,
                                shape = RoundedCornerShape(
                                    bottomStart = 28.dp,
                                    bottomEnd   = 28.dp
                                )
                            )
                            .padding(top = 48.dp, bottom = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            // Avatar con inicial
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val inicial = empleado?.nombres
                                    ?.firstOrNull()?.uppercaseChar()?.toString()
                                    ?: usuario!!.nombre.firstOrNull()
                                        ?.uppercaseChar()?.toString()
                                    ?: "?"
                                Text(
                                    text = inicial,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Nombre completo desde empleados
                            Text(
                                text = empleado?.nombreCompleto
                                    ?: usuario!!.nombre.ifEmpty { "Sin nombre" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Rol desde usuarios
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = usuario!!.rolNombre.ifEmpty { "Sin rol" },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 4.dp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Estado desde empleados (con fallback a usuarios)
                            val activo = empleado?.estaActivo ?: usuario!!.estaActivo
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (activo) VerdeOk.copy(alpha = 0.2f)
                                else Color.Red.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (activo) "● Activo" else "● Inactivo",
                                    color = if (activo) Color(0xFF86EFAC)
                                    else Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 4.dp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // SECCIÓN 1: Datos personales (de empleados)
                        TextoSeccion("Datos personales")

                        TarjetaDato(
                            icono    = Icons.Outlined.Badge,
                            etiqueta = "Código de empleado",
                            valor    = empleado?.codigoEmpleado ?: "—"
                        )
                        TarjetaDato(
                            icono    = Icons.Outlined.AssignmentInd,
                            etiqueta = "DNI",
                            valor    = empleado?.dni ?: "—"
                        )
                        TarjetaDato(
                            icono    = Icons.Outlined.Phone,
                            etiqueta = "Teléfono",
                            valor    = empleado?.telefono ?: "—"
                        )
                        TarjetaDato(
                            icono    = Icons.Outlined.CalendarMonth,
                            etiqueta = "Fecha de ingreso",
                            valor    = empleado?.fechaFormateada ?: "—"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // SECCIÓN 2: Datos laborales (de empleados)
                        TextoSeccion("Datos laborales")

                        TarjetaDato(
                            icono    = Icons.Outlined.Business,
                            etiqueta = "Departamento",
                            valor    = empleado?.departamentoNombre ?: "—"
                        )
                        TarjetaDato(
                            icono    = Icons.Outlined.AttachMoney,
                            etiqueta = "Salario",
                            valor    = empleado?.salarioFormateado ?: "—"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // SECCIÓN 3: Información de cuenta (de usuarios)
                        TextoSeccion("Información de cuenta")

                        TarjetaDato(
                            icono    = Icons.Outlined.Email,
                            etiqueta = "Correo institucional",
                            valor    = usuario!!.correo
                        )
                        TarjetaDato(
                            icono    = Icons.Outlined.AlternateEmail,
                            etiqueta = "Correo personal",
                            valor    = usuario!!.correoPersonal.ifEmpty { "—" }
                        )
                        TarjetaDato(
                            icono    = Icons.Outlined.AdminPanelSettings,
                            etiqueta = "Rol",
                            valor    = usuario!!.rolNombre.ifEmpty { "—" }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ── COMPOSABLES PRIVADOS ─────────────────────────────────────────────────────

@Composable
private fun TextoSeccion(texto: String) {
    Text(
        text = texto,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = ColorCafe,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun TarjetaDato(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = ColorCafe,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = etiqueta, fontSize = 11.sp, color = ColorGris)
                Text(
                    text = valor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorNegro
                )
            }
        }
    }
}