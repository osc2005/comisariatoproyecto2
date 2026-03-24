package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.data.r_permisos
import androidx.compose.ui.text.style.TextAlign

private val AzulOscuro = Color(0xFF132B5C)
private val VerdeBoton = Color(0xFF12C48B)
private val FondoPantalla = Color(0xFFF3F4F6)
private val Blanco = Color.White
private val GrisTexto = Color(0xFF6B7280)
private val GrisBorde = Color(0xFFD1D5DB)
private val NegroSuave = Color(0xFF111827)

@Composable
fun LoginComisariatoScreen(
    onLoginSuccess: () -> Unit = {},
    repo: r_permisos
) {
    var codigoEmpleado by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 🔥 CONTROL DEL LOGIN
    var loginTrigger by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // 🔥 LAUNCHED EFFECT (BIEN USADO)
    LaunchedEffect(loginTrigger) {
        if (loginTrigger) {
            isLoading = true

            // 🔥 AQUÍ VA TU LOGIN (Firebase después)
            kotlinx.coroutines.delay(1500)

            isLoading = false
            loginTrigger = false

            // Simulamos éxito
            onLoginSuccess()
            val ok = repo.login(codigoEmpleado, password)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FondoPantalla
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // 🔵 HEADER
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                // 🔵 HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                        .background(
                            color = AzulOscuro,
                            shape = RoundedCornerShape(
                                bottomStart = 30.dp,
                                bottomEnd = 30.dp
                            )
                        ),
                    contentAlignment = Alignment.TopCenter // 🔥 CAMBIO
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 120.dp) // 🔥 CONTROLÁS LA POSICIÓN
                    ) {

                        Text(
                            text = "Comisariato",
                            color = Blanco,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Sistema de Crédito Empresarial",
                            color = Blanco.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
                // ⚪ CARD (encima del header)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .height(1500.dp)
                        .padding(top = 350.dp), // 🔥 controla qué tanto baja
                    shape = RoundedCornerShape(24.dp),

                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {

                        Text(
                            text = "Iniciar sesión",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = NegroSuave
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Código de empleado")

                        OutlinedTextField(
                            value = codigoEmpleado,
                            onValueChange = { codigoEmpleado = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: EMP-2024-1542") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Contraseña")

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("********") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null)
                            },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { loginTrigger = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeBoton
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Ingresar")

                        }

                        Spacer(modifier = Modifier.height(12.dp))


                        Text(
                            text = "¿Olvidaste tu contraseña? Contacta a Recursos Humanos",
                            fontSize = 12.sp,
                            color = GrisTexto,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 12.dp),

                        )
                    }
                }
            }
        }
        }
}