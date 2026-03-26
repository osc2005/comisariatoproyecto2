package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.r_permisos

private val ColorCafe  = Color(0xFF8B5A2B)
private val ColorCrema = Color(0xFFFFF8F0)
private val ColorGris  = Color(0xFF6B7280)

val RusticText = Color.Black

@Composable
fun PerfilScreen(repo: r_permisos,
onLogout: () -> Unit
) {


    var empleado by remember { mutableStateOf<Empleado?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error    by remember { mutableStateOf("") }
    var reintentar by remember { mutableStateOf(0) }

    LaunchedEffect(reintentar) {
        cargando = true

        error    = ""
        try {
            val resultado = repo.obtenerMiPerfilEmpleado()
            if (resultado != null) {
                empleado = resultado
            } else {
                error = "No se encontró tu perfil.\nVerifica que tu correo esté registrado en el sistema."
            }
        } catch (e: Exception) {
            error = "Error al cargar perfil: ${e.localizedMessage}"
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
            cargando -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ColorCafe
                )
            }


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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

            empleado != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header con avatar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = ColorCafe,
                                shape = RoundedCornerShape(
                                    bottomStart = 28.dp,
                                    bottomEnd = 28.dp
                                )
                            )
                            .padding(top = 48.dp, bottom = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Avatar con iniciales
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = empleado!!.nombres
                                        .firstOrNull()
                                        ?.uppercaseChar()
                                        ?.toString() ?: "?",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = empleado!!.nombreCompleto,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Badge de estado
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (empleado!!.estaActivo)
                                    Color(0xFF22C55E).copy(alpha = 0.2f)
                                else
                                    Color.Red.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (empleado!!.estaActivo) "● Activo" else "● Inactivo",
                                    color = if (empleado!!.estaActivo)
                                        Color(0xFF86EFAC)
                                    else
                                        Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 4.dp
                                    )
                                )
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(20.dp))

                    // Sección de datos
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextoSeccion("Información personal")

                        TarjetaDato(
                            icono = Icons.Outlined.Email,
                            etiqueta = "Correo",
                            valor = empleado!!.correo
                        )
                        TarjetaDato(
                            icono = Icons.Outlined.Badge,
                            etiqueta = "Código de empleado",
                            valor = empleado!!.codigoEmpleado
                        )
                        TarjetaDato(
                            icono = Icons.Outlined.CreditCard,
                            etiqueta = "DNI",
                            valor = empleado!!.dni
                        )
                        TarjetaDato(
                            icono = Icons.Outlined.Phone,
                            etiqueta = "Teléfono",
                            valor = empleado!!.telefono
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        TextoSeccion("Información laboral")

                        TarjetaDato(
                            icono = Icons.Outlined.AttachMoney,
                            etiqueta = "Salario",
                            valor = empleado!!.salarioFormateado
                        )
                        TarjetaDato(
                            icono = Icons.Outlined.AttachMoney,
                            etiqueta = "Credito",
                            valor = "------"
                        )
                        TarjetaDato(
                            icono = Icons.Outlined.CalendarMonth,
                            etiqueta = "Fecha de registro",
                            valor = empleado!!.fechaFormateada
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onLogout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),

                        border = BorderStroke(1.dp, ColorCafe),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorCafe
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cerrar sesión",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

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
                Text(
                    text = etiqueta,
                    fontSize = 11.sp,
                    color = ColorGris
                )
                Text(
                    text = valor.ifEmpty { "—" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
            }
        }
    }
}