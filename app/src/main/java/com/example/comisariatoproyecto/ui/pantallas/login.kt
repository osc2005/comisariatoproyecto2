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

// ── Paleta ───────────────────────────────────────────────────────────────────
private val Navy900   = Color(0xFF0B1D3A)
private val Navy800   = Color(0xFF112244)
private val Navy600   = NavyPrimary
private val Accent    = Color(0xFF4F8EF7)
private val CardBg    = Color(0xFFFAFBFF)
private val TextPrimary   = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val BorderColor   = Color(0xFFE2E8F0)
private val ErrorRed      = Color(0xFFEF4444)
private val White         = Color.White

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

    // Animación del círculo decorativo
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val circleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy900, Navy800, Color(0xFF162B52))
                )
            )
            .drawBehind {
                drawDecorativeCircles(circleOffset)
            }
    ) {

        // ── HEADER ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo mark
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

            Text(
                text       = "Comisariato",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = White,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text      = "Sistema de Crédito Empresarial",
                fontSize  = 13.sp,
                color     = White.copy(alpha = 0.55f),
                letterSpacing = 0.3.sp
            )
        }

        // ── CARD ──────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape  = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            AnimatedContent(
                targetState    = modoPrimeraVez,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { if (targetState) -40 else 40 }) togetherWith
                            (fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { if (targetState) 40 else -40 })
                },
                label = "login_mode"
            ) { esPrimeraVez ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .padding(top = 32.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (esPrimeraVez) {
                        // ── MODO NORMAL ───────────────────────────────────────

                        // Pill indicador
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(BorderColor)
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text       = "Bienvenido",
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                            modifier   = Modifier.fillMaxWidth()
                        )
                        Text(
                            text     = "Ingresá tus credenciales para continuar",
                            fontSize = 13.sp,
                            color    = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(28.dp))

                        // Campo correo
                        FieldLabel("Correo electrónico")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value         = correo,
                            onValueChange = { correo = it; errorCorreo = "" },
                            modifier      = Modifier.fillMaxWidth(),
                            placeholder   = { Text("usuario@empresa.com", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                            leadingIcon   = {
                                Icon(Icons.Outlined.Person, null,
                                    tint = if (correo.isNotEmpty()) Navy600 else TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp))
                            },
                            singleLine  = true,
                            isError     = errorCorreo.isNotEmpty(),
                            shape       = RoundedCornerShape(14.dp),
                            colors      = loginFieldColors(),
                            textStyle   = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary)
                        )
                        if (errorCorreo.isNotEmpty()) ErrorText(errorCorreo)

                        Spacer(Modifier.height(16.dp))

                        // Campo contraseña
                        FieldLabel("Contraseña")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value         = password,
                            onValueChange = { password = it; errorPassword = "" },
                            modifier      = Modifier.fillMaxWidth(),
                            placeholder   = { Text("••••••••", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                            leadingIcon   = {
                                Icon(Icons.Outlined.Lock, null,
                                    tint = if (password.isNotEmpty()) Navy600 else TextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        null,
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            singleLine            = true,
                            visualTransformation  = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError               = errorPassword.isNotEmpty(),
                            shape                 = RoundedCornerShape(14.dp),
                            colors                = loginFieldColors(),
                            textStyle             = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary)
                        )
                        if (errorPassword.isNotEmpty()) ErrorText(errorPassword)

                        Spacer(Modifier.height(28.dp))

                        // Botón ingresar
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
                                            onLoginSuccess()
                                        }
                                        LoginResult.inactivo      -> errorPassword = "Tu cuenta está inactiva. Contactá a Recursos Humanos."
                                        LoginResult.ERROR_CREDENCIALES -> errorPassword = "Correo o contraseña incorrectos."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isLoading,
                            shape   = RoundedCornerShape(14.dp),
                            colors  = ButtonDefaults.buttonColors(
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
                        // ── MODO BIOMETRÍA ────────────────────────────────────

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(BorderColor)
                        )

                        Spacer(Modifier.height(32.dp))

                        // Avatar con inicial
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(listOf(Navy600, Navy900))
                                )
                                .border(3.dp, White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = nombreGuardado.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color      = White
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text       = nombreGuardado.ifBlank { "Usuario" },
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                            textAlign  = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text      = sessionPrefs.obtenerCorreo(),
                            fontSize  = 13.sp,
                            color     = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(32.dp))

                        // Área del ícono de huella con efecto pulsante
                        val pulseAnim by rememberInfiniteTransition(label = "pulse").animateFloat(
                            initialValue  = 0.85f,
                            targetValue   = 1f,
                            animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
                            label         = "pulse_scale"
                        )

                        Box(contentAlignment = Alignment.Center) {
                            // Anillo exterior pulsante
                            Box(
                                modifier = Modifier
                                    .size((72 * pulseAnim).dp)
                                    .clip(CircleShape)
                                    .background(Navy600.copy(alpha = 0.08f))
                            )
                            // Botón biometría
                            Button(
                                onClick = {
                                    activity?.let { act ->
                                        autenticarConBiometria(act) {
                                            val correoGuardado = sessionPrefs.obtenerCorreo()
                                            val passGuardado   = sessionPrefs.obtenerPassword()
                                            if (correoGuardado.isEmpty() || passGuardado.isEmpty()) {
                                                errorPassword  = "No hay sesión guardada."
                                                modoPrimeraVez = true
                                                return@autenticarConBiometria
                                            }
                                            scope.launch {
                                                isLoading = true
                                                val resultado = repo.loginSilencioso(correoGuardado, passGuardado)
                                                isLoading = false
                                                when (resultado) {
                                                    LoginResult.activo -> onLoginSuccess()
                                                    LoginResult.inactivo -> {
                                                        sessionPrefs.desactivarBiometria()
                                                        sessionPrefs.limpiarSesionLocal()
                                                        biometriaDisponible = false
                                                        modoPrimeraVez      = true
                                                        errorPassword = "Tu cuenta está inactiva. Contactá a Recursos Humanos."
                                                    }
                                                    LoginResult.ERROR_CREDENCIALES -> {
                                                        errorPassword = "No se pudo autenticar. Iniciá sesión manualmente."
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier  = Modifier.size(60.dp),
                                enabled   = !isLoading,
                                shape     = CircleShape,
                                colors    = ButtonDefaults.buttonColors(
                                    containerColor         = Navy600,
                                    disabledContainerColor = Navy600.copy(alpha = 0.4f)
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Outlined.Fingerprint, null, tint = White, modifier = Modifier.size(28.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text      = "Tocá para autenticarte",
                            fontSize  = 13.sp,
                            color     = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        if (errorPassword.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            ErrorText(errorPassword, centered = true)
                        }

                        Spacer(Modifier.height(24.dp))

                        // Separador
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                            Text("  o  ", fontSize = 12.sp, color = TextSecondary)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                        }
                        Spacer(Modifier.height(24.dp))
                        Spacer(Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { modoPrimeraVez = true; errorPassword = "" },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape  = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Usar otra cuenta", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
        text       = texto,
        fontSize   = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TextSecondary,
        letterSpacing = 0.3.sp,
        modifier   = Modifier.fillMaxWidth()
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
    focusedBorderColor   = NavyPrimary,
    unfocusedBorderColor = BorderColor,
    errorBorderColor     = ErrorRed,
    focusedContainerColor   = White,
    unfocusedContainerColor = White,
    errorContainerColor     = Color(0xFFFFF5F5),
    cursorColor = NavyPrimary
)

private fun DrawScope.drawDecorativeCircles(offset: Float) {
    val navy = Navy900
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