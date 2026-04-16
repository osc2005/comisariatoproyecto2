package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.comisariatoproyecto.autenticarConBiometria
import com.example.comisariatoproyecto.data.LoginResult
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.utils.SessionPrefs
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import com.example.comisariatoproyecto.MainActivity
import kotlinx.coroutines.CoroutineScope

// ── Paleta ───────────────────────────────────────────────────────────────────
private val Navy900       = Color(0xFF0B1D3A)
private val Navy800       = Color(0xFF112244)
private val Navy600       = NavyPrimary
private val CardBg        = Color(0xFFFAFBFF)
private val TextPrimary   = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val BorderColor   = Color(0xFFE2E8F0)
private val ErrorRed      = Color(0xFFEF4444)
private val White         = Color.White

private const val TELEFONO_RRHH = "tel:33039696"

@Composable
fun LoginComisariatoScreen(
    onLoginSuccess: () -> Unit = {},
    repo: r_permisos
) {
    val context      = LocalContext.current
    val activity     = context as? FragmentActivity
    val sessionPrefs = remember { SessionPrefs(context) }
    val scope        = rememberCoroutineScope()

    var correo              by remember { mutableStateOf("") }
    var password            by remember { mutableStateOf("") }
    var nombreGuardado      by remember { mutableStateOf("") }
    var isLoading           by remember { mutableStateOf(false) }
    var errorCorreo         by remember { mutableStateOf("") }
    var errorPassword       by remember { mutableStateOf("") }
    var biometriaDisponible by remember { mutableStateOf(false) }
    var modoPrimeraVez      by remember { mutableStateOf(true) }
    var passwordVisible     by remember { mutableStateOf(false) }
    var mostrarModoInactivo by remember { mutableStateOf(false) }
    // ← Bloquea el campo correo después de un intento con cuenta inactiva
    var correoBloquado      by remember { mutableStateOf(false) }

    // Define los estados posibles para la segunda fase
    var subModoAutenticacion by remember { mutableStateOf("SELECCION") } // "SELECCION", "OTP", "BIOMETRICO"
    var otpIngresado by remember { mutableStateOf("") }
    var otpGenerado by remember { mutableStateOf("") }
    var errorOtp by remember { mutableStateOf("") }

    // Función simple para generar OTP
    fun generarCodigoOTP(): String = (100000..999999).random().toString()

    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val circleOffset by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "circle"
    )

    LaunchedEffect(Unit) {
        val correoLocal  = sessionPrefs.obtenerCorreo()
        val nombreLocal  = sessionPrefs.obtenerNombre()
        val hayRegistro  = sessionPrefs.hayUsuarioRegistrado()
        val hayBiometria = sessionPrefs.biometriaHabilitada()

        correo              = correoLocal
        nombreGuardado      = nombreLocal
        biometriaDisponible = hayBiometria
        modoPrimeraVez      = !(hayRegistro && hayBiometria)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Navy900, Navy800, Color(0xFF162B52))))
            .drawBehind { drawDecorativeCircles(circleOffset) }
    ) {

        // ── HEADER ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(White.copy(alpha = 0.12f))
                    .border(1.dp, White.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("C", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = White)
            }
            Spacer(Modifier.height(16.dp))
            Text("Comisariato", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = White, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text("Sistema de Crédito Empresarial", fontSize = 13.sp, color = White.copy(alpha = 0.55f), letterSpacing = 0.3.sp)
        }

        // ── CARD ──────────────────────────────────────────────────────────────
        Card(
            modifier  = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            shape     = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors    = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            AnimatedContent(
                targetState    = Triple(modoPrimeraVez, biometriaDisponible, mostrarModoInactivo),
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { 20 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically(tween(200)) { -20 })
                },
                label = "login_mode"
            ) { (esPrimeraVez, _, esInactivo) ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .padding(top = 32.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── MODO INACTIVO ─────────────────────────────────────────
                    if (esInactivo) {
                        Box(
                            modifier = Modifier
                                .width(40.dp).height(4.dp)
                                .clip(CircleShape)
                                .background(BorderColor)
                        )
                        Spacer(Modifier.height(28.dp))

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2))
                                .border(1.5.dp, Color(0xFFFECACA), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Lock, null, tint = ErrorRed, modifier = Modifier.size(32.dp))
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "Cuenta inactiva",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tu cuenta ha sido desactivada.\nComunicate con Recursos Humanos para reactivarla.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(28.dp))

                        FieldLabel("Correo electrónico")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value         = correo,
                            onValueChange = {},
                            readOnly      = true,
                            enabled       = false,
                            modifier      = Modifier.fillMaxWidth(),
                            leadingIcon   = {
                                Icon(
                                    Icons.Outlined.Person, null,
                                    tint     = TextSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            shape  = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor      = BorderColor,
                                disabledContainerColor   = Color(0xFFF8FAFC),
                                disabledTextColor        = TextSecondary,
                                disabledLeadingIconColor = TextSecondary.copy(alpha = 0.4f)
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )

                        Spacer(Modifier.height(28.dp))

                        Button(
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_DIAL,
                                    android.net.Uri.parse(TELEFONO_RRHH)
                                )
                                context.startActivity(intent)
                            },
                            modifier  = Modifier.fillMaxWidth().height(52.dp),
                            shape     = RoundedCornerShape(14.dp),
                            colors    = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Llamar a Recursos Humanos", color = White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Vuelve al login normal pero con correo bloqueado
                        OutlinedButton(
                            onClick = {
                                mostrarModoInactivo = false
                                modoPrimeraVez      = true
                                errorPassword       = ""
                                // correoBloquado se mantiene en true — no se resetea
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(14.dp),
                            border   = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Volver al inicio", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        return@AnimatedContent
                    }

                    // ── MODO NORMAL ───────────────────────────────────────────
                    if (esPrimeraVez) {

                        Box(
                            modifier = Modifier
                                .width(40.dp).height(4.dp)
                                .clip(CircleShape)
                                .background(BorderColor)
                        )
                        Spacer(Modifier.height(24.dp))

                        Text("Bienvenido", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.fillMaxWidth())
                        Text("Ingresá tus credenciales para continuar", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))

                        Spacer(Modifier.height(28.dp))

                        // ── Campo correo: bloqueado si la cuenta estaba inactiva ──
                        FieldLabel("Correo electrónico")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value         = correo,
                            onValueChange = { if (!correoBloquado) { correo = it; errorCorreo = "" } },
                            readOnly      = correoBloquado,
                            enabled       = !correoBloquado,
                            modifier      = Modifier.fillMaxWidth(),
                            placeholder   = { Text("usuario@empresa.com", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                            leadingIcon   = {
                                Icon(
                                    Icons.Outlined.Person, null,
                                    tint     = if (correoBloquado) TextSecondary.copy(alpha = 0.4f)
                                    else if (correo.isNotEmpty()) Navy600
                                    else TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine  = true,
                            isError     = errorCorreo.isNotEmpty(),
                            shape       = RoundedCornerShape(14.dp),
                            colors      = if (correoBloquado) OutlinedTextFieldDefaults.colors(
                                disabledBorderColor      = BorderColor,
                                disabledContainerColor   = Color(0xFFF8FAFC),
                                disabledTextColor        = TextSecondary,
                                disabledLeadingIconColor = TextSecondary.copy(alpha = 0.4f)
                            ) else loginFieldColors(),
                            textStyle   = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary)
                        )
                        if (errorCorreo.isNotEmpty()) ErrorText(errorCorreo)

                        // Mensaje sutil si el correo está bloqueado
                        if (correoBloquado) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "El correo no puede modificarse mientras la cuenta está inactiva.",
                                fontSize  = 11.sp,
                                color     = ErrorRed.copy(alpha = 0.7f),
                                modifier  = Modifier.fillMaxWidth().padding(start = 4.dp),
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── Campo contraseña: siempre habilitado ──
                        FieldLabel("Contraseña")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value         = password,
                            onValueChange = { password = it; errorPassword = "" },
                            modifier      = Modifier.fillMaxWidth(),
                            placeholder   = { Text("••••••••", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                            leadingIcon   = {
                                Icon(
                                    Icons.Outlined.Lock, null,
                                    tint     = if (password.isNotEmpty()) Navy600 else TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        null,
                                        tint     = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            singleLine           = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError              = errorPassword.isNotEmpty(),
                            shape                = RoundedCornerShape(14.dp),
                            colors               = loginFieldColors(),
                            textStyle            = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary)
                        )
                        if (errorPassword.isNotEmpty()) ErrorText(errorPassword)

                        Spacer(Modifier.height(28.dp))

                        Button(
                            onClick = {
                                errorCorreo   = ""
                                errorPassword = ""
                                val correoLimpio = correo.lineSequence().firstOrNull()?.trim().orEmpty()
                                if (correoLimpio.isBlank()) { errorCorreo = "El correo es obligatorio"; return@Button }
                                if (password.isBlank())     { errorPassword = "La contraseña es obligatoria"; return@Button }
                                scope.launch {
                                    isLoading = true
                                    val resultado = repo.login(correoLimpio, password)
                                    isLoading = false
                                    when (resultado) {
                                        LoginResult.activo -> {
                                            val usuario = repo.obtenerMiUsuario()
                                            sessionPrefs.guardarCorreo(correoLimpio)
                                            sessionPrefs.guardarPassword(password)
                                            sessionPrefs.guardarNombre(usuario?.nombre ?: "")
                                            sessionPrefs.activarBiometria()
                                            sessionPrefs.marcarUsuarioRegistrado()
                                            biometriaDisponible = true
                                            nombreGuardado      = usuario?.nombre ?: ""
                                            modoPrimeraVez      = false
                                            correoBloquado      = false // ← resetea al ingresar exitosamente
                                            onLoginSuccess()
                                        }
                                        LoginResult.inactivo -> {
                                            mostrarModoInactivo = true
                                            correoBloquado      = true  // ← bloquea el correo
                                            errorPassword       = ""
                                        }
                                        LoginResult.ERROR_CREDENCIALES ->
                                            errorPassword = "Correo o contraseña incorrectos."
                                    }
                                }
                            },
                            modifier  = Modifier.fillMaxWidth().height(52.dp),
                            enabled   = !isLoading,
                            shape     = RoundedCornerShape(14.dp),
                            colors    = ButtonDefaults.buttonColors(
                                containerColor         = Navy600,
                                disabledContainerColor = Navy600.copy(alpha = 0.4f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                            } else {
                                Text("Ingresar", color = White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.2.sp)
                            }
                        }

                    } else {
                        // ── MODO AUTENTICACIÓN SECUNDARIA (Biometría u OTP) ──
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            // Cabecera común (Avatar y nombre)
                            HeaderUsuario(nombreGuardado, sessionPrefs.obtenerCorreo())

                            AnimatedContent(targetState = subModoAutenticacion) { estado ->
                                when (estado) {
                                    "SELECCION" -> {
                                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Elige un método para ingresar", fontSize = 14.sp, color = TextSecondary)
                                            Spacer(Modifier.height(24.dp))

                                            // Botón Biométrico
                                            OpcionAuthButton(
                                                texto = "Usar Huella Digital",
                                                icon = Icons.Outlined.Fingerprint,
                                                color = Navy600
                                            ) {
                                                // Ejecuta la lógica biométrica que ya tienes
                                                ejecutarBiometria(activity, repo, sessionPrefs, scope, onLoginSuccess)
                                            }

                                            Spacer(Modifier.height(12.dp))

                                            // Botón OTP
                                            OpcionAuthButton(
                                                texto = "Recibir código por notificación",
                                                icon = Icons.Outlined.Lock,
                                                color = Color(0xFF6366F1)
                                            ) {
                                                otpGenerado = generarCodigoOTP()
                                                MainActivity.enviarNotificacionOTP(
                                                    context,
                                                    "Su codigo de acceso de un solo uso",
                                                    "Su codigo es: ${otpGenerado}")
                                                subModoAutenticacion = "OTP"
                                            }
                                        }
                                    }

                                    "OTP" -> {
                                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Introduce el código enviado", fontSize = 14.sp, color = TextSecondary)
                                            Spacer(Modifier.height(16.dp))

                                            OutlinedTextField(
                                                value = otpIngresado,
                                                onValueChange = { if(it.length <= 6) otpIngresado = it },
                                                placeholder = { Text("000000") },
                                                modifier = Modifier.width(150.dp),
                                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, letterSpacing = 4.sp),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            if (errorOtp.isNotEmpty()) ErrorText(errorOtp, centered = true)

                                            Spacer(Modifier.height(20.dp))

                                            Button(
                                                onClick = {
                                                    if (otpIngresado == otpGenerado) {
                                                        // El código coincide, procedemos al login silencioso
                                                        ejecutarLoginSilencioso(repo, sessionPrefs, scope, onLoginSuccess)
                                                    } else {
                                                        errorOtp = "Código incorrecto"
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Text("Verificar e Ingresar")
                                            }

                                            TextButton(onClick = { subModoAutenticacion = "SELECCION"; errorOtp = "" }) {
                                                Text("Volver", color = TextSecondary)
                                            }
                                        }
                                    }
                                }
                            }

                            // El botón de "Usar otra cuenta" se mantiene al final
                            Spacer(Modifier.height(24.dp))
                            BotonUsarOtraCuenta { modoPrimeraVez = true }
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers privados ─────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(texto: String) {
    Text(
        text          = texto,
        fontSize      = 12.sp,
        fontWeight    = FontWeight.SemiBold,
        color         = TextSecondary,
        letterSpacing = 0.3.sp,
        modifier      = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ErrorText(texto: String, centered: Boolean = false) {
    Text(
        text      = texto,
        color     = ErrorRed,
        fontSize  = 12.sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, start = if (centered) 0.dp else 4.dp)
    )
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = NavyPrimary,
    unfocusedBorderColor    = BorderColor,
    errorBorderColor        = ErrorRed,
    focusedContainerColor   = White,
    unfocusedContainerColor = White,
    errorContainerColor     = Color(0xFFFFF5F5),
    cursorColor             = NavyPrimary
)

private fun DrawScope.drawDecorativeCircles(offset: Float) {
    drawCircle(
        color  = Color(0x14FFFFFF),
        radius = size.width * 0.65f,
        center = Offset(size.width * 0.85f, size.height * (0.08f + offset * 0.04f))
    )
    drawCircle(
        color  = Color(0x0AFFFFFF),
        radius = size.width * 0.45f,
        center = Offset(size.width * 0.1f, size.height * (0.18f - offset * 0.03f))
    )
    drawCircle(
        color  = Color(0x08FFFFFF),
        radius = size.width * 0.3f,
        center = Offset(size.width * 0.5f, size.height * (0.25f + offset * 0.02f))
    )
}

// Función genérica para el login final
private fun ejecutarLoginSilencioso(
    repo: r_permisos,
    sessionPrefs: SessionPrefs,
    scope: CoroutineScope,
    onSuccess: () -> Unit
) {
    val correo = sessionPrefs.obtenerCorreo()
    val pass = sessionPrefs.obtenerPassword()

    scope.launch {
        val resultado = repo.loginSilencioso(correo, pass)
        if (resultado == LoginResult.activo) onSuccess()
        else { /* Manejar error o cuenta inactiva */ }
    }
}

@Composable
fun OpcionAuthButton(texto: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(texto, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HeaderUsuario(nombre: String, correo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Navy600, Navy900)))
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(nombre.ifBlank { "Usuario" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(correo, fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun BotonUsarOtraCuenta(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
    ) {
        Text("Usar otra cuenta", fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private fun ejecutarBiometria(
    activity: FragmentActivity?,
    repo: r_permisos,
    sessionPrefs: SessionPrefs,
    scope: CoroutineScope,
    onLoginSuccess: () -> Unit
) {
    activity?.let { act ->
        autenticarConBiometria(act) {
            val correoGuardado = sessionPrefs.obtenerCorreo()
            val passGuardado = sessionPrefs.obtenerPassword()

            if (correoGuardado.isNotEmpty() && passGuardado.isNotEmpty()) {
                scope.launch {
                    val resultado = repo.loginSilencioso(correoGuardado, passGuardado)
                    if (resultado == LoginResult.activo) {
                        onLoginSuccess()
                    }
                }
            }
        }
    }
}