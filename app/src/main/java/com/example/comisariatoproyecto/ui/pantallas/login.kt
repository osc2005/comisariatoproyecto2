package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.comisariatoproyecto.autenticarConBiometria
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.utils.SessionPrefs
import kotlinx.coroutines.launch

private val ColorCafe = Color(0xFF8B5A2B)
private val ColorCrema = Color(0xFFFFE4C4)
private val ColorBlanco = Color.White
private val ColorGris = Color(0xFF6B7280)
private val ColorNegro = Color(0xFF111827)

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
    var nombreGuardado by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorCorreo by remember { mutableStateOf("") }
    var errorPassword by remember { mutableStateOf("") }
    var biometriaDisponible by remember { mutableStateOf(false) }
    var modoPrimeraVez by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        correo = sessionPrefs.obtenerCorreo()
            .lineSequence()
            .firstOrNull()
            ?.trim()
            .orEmpty()

        password = ""
        nombreGuardado = sessionPrefs.obtenerNombre()
        biometriaDisponible = sessionPrefs.biometriaHabilitada()

        modoPrimeraVez = !(
                sessionPrefs.hayUsuarioRegistrado() &&
                        biometriaDisponible &&
                        sessionPrefs.obtenerCorreo().isNotBlank() &&
                        sessionPrefs.obtenerPassword().isNotBlank()
                )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NavyPrimary
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .background(
                        color = NavyPrimary,
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (modoPrimeraVez) {
                        Text(
                            text = "Iniciar sesión",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorNegro
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Correo electrónico",
                            color = ColorNegro,
                            modifier = Modifier.fillMaxWidth()
                        )

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
                            Text(
                                errorCorreo,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Contraseña",
                            color = ColorNegro,
                            modifier = Modifier.fillMaxWidth()
                        )

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
                            Text(
                                errorPassword,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                errorCorreo = ""
                                errorPassword = ""
                                var valido = true

                                val correoLimpio = correo
                                    .lineSequence()
                                    .firstOrNull()
                                    ?.trim()
                                    .orEmpty()

                                if (correoLimpio.isBlank()) {
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
                                        val ok = repo.login(correoLimpio, password)

                                        if (ok) {
                                            val usuario = repo.obtenerMiUsuario()

                                            sessionPrefs.guardarCorreo(correoLimpio)
                                            sessionPrefs.guardarPassword(password)
                                            sessionPrefs.guardarNombre(usuario?.nombre ?: "")
                                            sessionPrefs.activarBiometria()
                                            sessionPrefs.marcarUsuarioRegistrado()

                                            biometriaDisponible = true
                                            nombreGuardado = usuario?.nombre ?: ""
                                            modoPrimeraVez = false

                                            onLoginSuccess()
                                        } else {
                                            errorPassword =
                                                "No se pudo iniciar sesión. Verifica tus datos."
                                        }
                                    } catch (e: Exception) {
                                        errorPassword =
                                            "Error al iniciar sesión: ${e.message ?: "Intenta nuevamente"}"
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
                                containerColor = NavyPrimary,
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
                                    color = ColorBlanco,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Bienvenido",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorNegro
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(56.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (nombreGuardado.isNotBlank()) nombreGuardado else "Usuario registrado",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorNegro
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = sessionPrefs.obtenerCorreo(),
                            fontSize = 13.sp,
                            color = ColorGris,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                activity?.let { act ->
                                    autenticarConBiometria(act) {
                                        val correoGuardado = sessionPrefs.obtenerCorreo()
                                        val passGuardado = sessionPrefs.obtenerPassword()

                                        if (correoGuardado.isEmpty() || passGuardado.isEmpty()) {
                                            errorPassword =
                                                "No hay sesión guardada. Inicia sesión manualmente."
                                            modoPrimeraVez = true
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
                                                    errorPassword =
                                                        "No se pudo autenticar con biometría."
                                                }
                                            } catch (e: Exception) {
                                                errorPassword =
                                                    "Error biométrico: ${e.message ?: "Intenta nuevamente"}"
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
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
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
                                    text = "Ingresar con biometría",
                                    color = ColorBlanco,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))



                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Ingresa Biometricamente",
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
}