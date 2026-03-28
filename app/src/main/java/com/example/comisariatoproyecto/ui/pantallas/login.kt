package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.compose.material3.ButtonDefaults

import com.example.comisariatoproyecto.autenticarConBiometria
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.utils.SessionPrefs
import kotlinx.coroutines.launch

private val ColorCafe   = Color(0xFF8B5A2B)
private val ColorCrema  = Color(0xFFFFE4C4)
private val ColorBlanco = Color.White
private val ColorGris   = Color(0xFF6B7280)
private val ColorNegro  = Color(0xFF111827)


@Composable
fun LoginComisariatoScreen(
    onLoginSuccess: () -> Unit = {},
    repo: r_permisos
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val sessionPrefs = remember { SessionPrefs(context) }
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf("") }
    var errorPassword by remember { mutableStateOf("") }
    var biometriaDisponible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        correo = sessionPrefs.obtenerCorreo()
        biometriaDisponible = sessionPrefs.biometriaHabilitada()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ColorCafe
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .background(
                        color = ColorCafe,
                        shape = RoundedCornerShape(
                            bottomStart = 30.dp,
                            bottomEnd = 30.dp
                        )
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 200.dp)
                ) {
                    Text(
                        text = "Comisariato",
                        color = ColorBlanco,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sistema de Crédito Empresarial",
                        color = ColorBlanco.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(top = 350.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ColorBlanco)
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
                        color = ColorNegro
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Correo electrónico", color = ColorNegro)
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {
                            correo = it
                            errorCorreo = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: tuuser@gmail.com") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null)
                        },
                        singleLine = true,
                        isError = errorCorreo.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorCorreo.isNotEmpty()) {
                        Text(errorCorreo, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Contraseña", color = ColorNegro)
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorPassword = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ej: 123456") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null)
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errorPassword.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorPassword.isNotEmpty()) {
                        Text(errorPassword, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            errorCorreo = ""
                            errorPassword = ""
                            var valido = true

                            if (correo.isBlank()) {
                                errorCorreo = "El correo es obligatorio"
                                valido = false
                            }

                            if (password.isBlank()) {
                                errorPassword = "La contraseña es obligatoria"
                                valido = false
                            }

                            if (!valido) return@Button

                            scope.launch {
                                isLoading = true
                                try {
                                    val ok = repo.login(correo.trim(), password)

                                    if (ok) {
                                        sessionPrefs.guardarCorreo(correo.trim())
                                        sessionPrefs.guardarPassword(password)
                                        sessionPrefs.activarBiometria()
                                        biometriaDisponible = true
                                        onLoginSuccess()
                                    } else {
                                        errorPassword = "No se pudo iniciar sesión. Verifica tus datos."
                                    }
                                } catch (e: Exception) {
                                    errorPassword = "Error al iniciar sesión: ${e.message ?: "Intenta nuevamente"}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorCrema,
                            disabledContainerColor = ColorCrema.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ColorNegro,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Ingresar",
                                color = ColorNegro,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (biometriaDisponible) {
                        OutlinedButton(
                            onClick = {
                                activity?.let { act ->
                                    autenticarConBiometria(act) {
                                        val correoGuardado = sessionPrefs.obtenerCorreo()
                                        val passGuardado = sessionPrefs.obtenerPassword()

                                        if (correoGuardado.isEmpty() || passGuardado.isEmpty()) {
                                            errorPassword = "No hay sesión guardada. Inicia sesión manualmente."
                                            return@autenticarConBiometria
                                        }

                                        scope.launch {
                                            isLoading = true
                                            try {
                                                val ok = repo.loginSilencioso(
                                                    correoGuardado,
                                                    passGuardado
                                                )

                                                if (ok) {
                                                    onLoginSuccess()
                                                } else {
                                                    errorPassword = "No se pudo autenticar con biometría."
                                                }
                                            } catch (e: Exception) {
                                                errorPassword = "Error biométrico: ${e.message ?: "Intenta nuevamente"}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Ingreso biométrico",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¿Olvidaste tu contraseña? Contacta a Recursos Humanos",
                        fontSize = 12.sp,
                        color = ColorGris,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}