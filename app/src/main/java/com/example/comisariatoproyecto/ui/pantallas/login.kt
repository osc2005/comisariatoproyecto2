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
import androidx.compose.ui.tooling.preview.Preview
import okio.blackholeSink

private val AzulOscuro = Color(0xFF8B5A2B)
private val Cremaboton = Color(0xFFFFE4C4)
private val FondoPantalla = Color(0xFF8B5A2B)
private val Blanco = Color.White
private val GrisTexto = Color(0xFF6B7280)
private val GrisBorde = Color(0xFFD1D5DB)
private val NegroSuave = Color(0xFF111827)



@Preview(showBackground = true)
@Composable
fun LoginComisariatoScreenPreview() {
    LoginComisariatoScreen(
        repo = r_permisos()
    )
}



@Composable
fun LoginComisariatoScreen(
    onLoginSuccess: () -> Unit = {},
    repo: r_permisos
) {
    var codigoEmpleado by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    //  CONTROL DEL LOGIN
    var loginTrigger by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    //validaciones
    var errorCodigo by remember { mutableStateOf("") }
    var errorPassword by remember { mutableStateOf("") }
    //  LAUNCHED EFFECT (BIEN USADO)
    LaunchedEffect(loginTrigger) {
        if (loginTrigger) {
            isLoading = true

            val ok = repo.login(codigoEmpleado.trim(), password)

            isLoading = false
            loginTrigger = false

            if (ok) {
                onLoginSuccess()
            } else {
                errorPassword = "Credenciales incorrectas"
            }
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
                        modifier = Modifier.padding(top = 200.dp) // 🔥 CONTROLÁS LA POSICIÓN
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

                        Text("Correo electronico")

                        OutlinedTextField(
                            value = codigoEmpleado,
                            onValueChange = {
                                codigoEmpleado = it
                                errorCodigo = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: tuuser@gmail.com") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            },
                            singleLine = true,
                            isError = errorCodigo.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (errorCodigo.isNotEmpty()) {
                            Text(
                                text = errorCodigo,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Contraseña")

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorPassword = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("********") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null)
                            },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = errorPassword.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (errorPassword.isNotEmpty()) {
                            Text(
                                text = errorPassword,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                errorCodigo = ""
                                errorPassword = ""

                                var valido = true

                                if (codigoEmpleado.isEmpty()) {
                                    errorCodigo = "El correo es obligatorio"
                                    valido = false
                                }

                                if (password.isEmpty()) {
                                    errorPassword = "La contraseña es obligatoria"
                                    valido = false
                                }

                                if (valido) {
                                    loginTrigger = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cremaboton
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Ingresar",
                                color = NegroSuave,
                                fontWeight = FontWeight.Bold

                            )

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