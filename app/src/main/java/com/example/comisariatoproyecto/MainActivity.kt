package com.example.comisariatoproyecto

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.comisariatoproyecto.data.*
import com.example.comisariatoproyecto.ui.pantallas.*
import com.example.comisariatoproyecto.ui.theme.ComisariatoProyectoTheme
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.utils.SessionPrefs
import com.example.comisariatoproyecto.utils.detectarActividad
import com.example.comisariatoproyecto.utils.rememberInactivityTimer
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        crearCanalNotificaciones(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContent {
            ComisariatoProyectoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

fun crearCanalNotificaciones(activity: MainActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            "canal_id", "Canal General", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Notificaciones del sistema" }
        val manager = activity.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(canal)
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Repositorios
    val repoAuth = remember { r_permisos() }
    val repocat = remember { r_Categoria() }
    val repoprod = remember { r_Productos() }
    val repocreditos = remember { r_Creditos() }
    val repocuotas = remember { r_CuotaCredito() }
    val repoReseñas = remember { r_Reseñas() }
    val sessionPrefs = remember { SessionPrefs(context) }

    // Estados
    var startDestination by rememberSaveable { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<m_Productos?>(null) }
    var creditoSeleccionado by remember { mutableStateOf<m_CreditoDetalle?>(null) }
    var plazoSeleccionadoReserva by remember { mutableStateOf<Int?>(null) }
    var cantidadSeleccionadaReserva by remember { mutableIntStateOf(1) }
    var empleadoCargado by remember { mutableStateOf<Empleado?>(null) }
    var reservasEmpleado by remember { mutableStateOf<List<m_Creditos>>(emptyList()) }
    var mostrarSheetOpinar by remember { mutableStateOf(false) }
    var creditoParaOpinar by remember { mutableStateOf<m_CreditoDetalle?>(null) }
    var mostrarDialogoInactividad by remember { mutableStateOf(false) }

    // Verificación de sesión inicial
    LaunchedEffect(Unit) {
        try {
            if (repoAuth.isLogged() && sessionPrefs.obtenerCorreo().isNotBlank()) {
                val perfil = repoAuth.obtenerMiPerfilCompleto()
                empleadoCargado = perfil.second
                empleadoCargado?.let { emp ->
                    reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                }
                startDestination = "inicio"
            } else {
                startDestination = "login"
            }
        } catch (e: Exception) {
            startDestination = "login"
        }
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NavyPrimary)
        }
        return
    }

    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val usuarioLogueado = currentRoute != null && currentRoute != "login"
    val mostrarMenu = currentRoute in setOf("inicio", "catalogo", "credito", "perfil")

    // Lógica de Logout
    val hacerLogoutCompleto: () -> Unit = {
        repoAuth.logout()
        sessionPrefs.cerrarSesionPorInactividad()
        nav.navigate("login") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val resetTimer = rememberInactivityTimer(
        habilitado = usuarioLogueado && !mostrarDialogoInactividad,
        onTimeout = { mostrarDialogoInactividad = true }
    )

    // Diálogo de inactividad
    if (mostrarDialogoInactividad) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(NavyPrimary.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text("Sesión cerrada", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            },
            text = {
                Text(
                    "Por seguridad, tu sesión fue cerrada automáticamente.\nVuelve a iniciar sesión.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoInactividad = false
                        repoAuth.logout()
                        sessionPrefs.cerrarSesionPorInactividad()
                        nav.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Entendido", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .detectarActividad { resetTimer() },
        bottomBar = {
            if (mostrarMenu) {
                MenuInferiorComisariato(
                    itemSeleccionado = when (currentRoute) {
                        "inicio"  -> "Inicio"
                        "catalogo" -> "Catálogo"
                        "credito" -> "Mi Crédito"
                        "perfil"  -> "Perfil"
                        else      -> "Inicio"
                    },
                    onItemClick = { item ->
                        val ruta = when (item) {
                            "Inicio"     -> "inicio"
                            "Catálogo"   -> "catalogo"
                            "Mi Crédito" -> "credito"
                            "Perfil"     -> "perfil"
                            else         -> "inicio"
                        }
                        nav.navigate(ruta) {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = startDestination!!,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable("login") {
                LoginComisariatoScreen(
                    repo = repoAuth,
                    onLoginSuccess = {
                        nav.navigate("inicio") { popUpTo("login") { inclusive = true } }
                    }
                )
            }

            composable("inicio") {
                PantallaInicio(
                    repo = repoAuth,
                    repoCreditos = repocreditos,
                    onLogout = hacerLogoutCompleto,
                    onIrCatalogo = { nav.navigate("catalogo") },
                    onIrCredito = { nav.navigate("credito") }
                )
            }

            composable("catalogo") {
                ProductosCatalogo(
                    repoCategoria = repocat,
                    repoProducto = repoprod,
                    onBack = { nav.popBackStack() },
                    onVerProductos = { cat -> nav.navigate("productos/${cat.id}/${cat.nombre}") },
                    onVerDetalleProducto = { prod ->
                        productoSeleccionado = prod
                        nav.navigate("detalleProducto")
                    }
                )
            }

            composable("productos/{categoriaId}/{categoriaNombre}") { backStack ->
                val cid  = backStack.arguments?.getString("categoriaId") ?: ""
                val cnom = backStack.arguments?.getString("categoriaNombre") ?: ""
                ListaProductos(
                    categoria = m_Categoria(id = cid, nombre = cnom),
                    repo = repoprod,
                    onBack = { nav.popBackStack() },
                    onVerDetalle = { prod ->
                        productoSeleccionado = prod
                        nav.navigate("detalleProducto")
                    }
                )
            }

            composable("credito") {
                PantallaCredito(
                    onVerDetalle = { c ->
                        creditoSeleccionado = c
                        nav.navigate("detalleCredito")
                    },
                    onOpinar = { c ->
                        creditoParaOpinar = c
                        mostrarSheetOpinar = true
                    }
                )
            }

            composable("detalleCredito") {
                creditoSeleccionado?.let { c ->
                    PantallaDetalleCredito(
                        credito     = c,
                        empleadoId  = empleadoCargado?.codigoEmpleado ?: "",  // ← agregar
                        onBack      = { nav.popBackStack() },
                        onOpinar    = {
                            creditoParaOpinar = c
                            mostrarSheetOpinar = true
                        }
                    )
                }
            }

            composable("perfil") {
                PerfilScreen(
                    repo = repoAuth,
                    onNavegaListaDeseos = { nav.navigate("wishlist") },
                    OnLogout = hacerLogoutCompleto
                )
            }

            composable("wishlist") {
                PantallaWishlist(
                    repoWishlist = r_Wishlist(),
                    onBack = { nav.popBackStack() },
                    onVerProducto = { productoId ->
                        scope.launch {
                            // Buscamos el producto en Firebase antes de navegar
                            val prod = repoprod.obtenerProductoPorId(productoId)
                            if (prod != null) {
                                productoSeleccionado = prod
                                nav.navigate("detalleProducto")
                            }
                        }
                    },
                    OnLogout = hacerLogoutCompleto
                )
            }

            composable("detalleProducto") {
                // ← Refresca reservas al entrar para tener el estado actualizado
                LaunchedEffect(Unit) {
                    empleadoCargado?.let { emp ->
                        reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                    }
                }

                productoSeleccionado?.let { prod ->
                    DetalleProducto(
                        producto = prod,
                        repoCuotas = repocuotas,
                        repoCreditos = repocreditos,
                        repoReseñas = repoReseñas,
                        reservaPendiente = reservasEmpleado.find {
                            it.productoId == prod.id && it.estado == "Pendiente"
                        },
                        empleado = empleadoCargado,
                        onBack = { nav.popBackStack() },
                        onVerComentarios = { nav.navigate("comentariosProducto") },
                        onReservar = { p, pl, c ->
                            productoSeleccionado = p
                            plazoSeleccionadoReserva = pl
                            cantidadSeleccionadaReserva = c
                            nav.navigate("confirmarReserva")
                        },
                        onCancelarReserva = { id ->
                            scope.launch {
                                repocreditos.cancelarReserva(id)
                                reservasEmpleado = reservasEmpleado.map {
                                    if (it.id == id) it.copy(estado = "Cancelado") else it
                                }
                            }
                        },
                        repoWishlist = r_Wishlist()
                    )
                }
            }

            // ✅ CORREGIDO: llaves bien cerradas + onOpinar funcional
            composable("comentariosProducto") {
                // ← Refresca las reservas al entrar para tener datos frescos
                LaunchedEffect(Unit) {
                    empleadoCargado?.let { emp ->
                        reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                    }
                }

                productoSeleccionado?.let { prod ->
                    ComentariosProducto(
                        productoId  = prod.id,
                        empleadoId  = empleadoCargado?.codigoEmpleado ?: "",
                        repoReseñas = repoReseñas,
                        repoCreditos = repocreditos,
                        onBack      = { nav.popBackStack() },
                        onOpinar    = { creditoId ->
                            val credito = reservasEmpleado.find { it.id == creditoId }
                            if (credito != null) {
                                creditoParaOpinar = m_CreditoDetalle(
                                    id             = credito.id,
                                    productoId     = credito.productoId,
                                    productoNombre = credito.productoNombre,
                                    productoImgUrl = credito.productoImgUrl,
                                    estado         = credito.estado
                                )
                                mostrarSheetOpinar = true
                            }
                        }
                    )
                }
            }

            composable("confirmarReserva") {
                productoSeleccionado?.let { prod ->
                    if (empleadoCargado != null) {
                        ConfirmarReserva(
                            repoCreditos = repocreditos,
                            producto = prod,
                            plazoMeses = plazoSeleccionadoReserva,
                            cantidad = cantidadSeleccionadaReserva,
                            empleado = empleadoCargado!!,
                            onBack = { nav.popBackStack() },
                            onConfirmar = {
                                scope.launch {
                                    empleadoCargado?.let { emp ->
                                        reservasEmpleado =
                                            repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                                    }
                                }
                                nav.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        // BottomSheet de opinión (compartido entre PantallaCredito y ComentariosProducto)
        if (mostrarSheetOpinar && creditoParaOpinar != null) {
            OpinarBottomSheet(
                productoNombre    = creditoParaOpinar!!.productoNombre,
                productoImagenUrl = creditoParaOpinar!!.productoImgUrl,
                onDismiss         = { mostrarSheetOpinar = false },
                // En MainActivity.kt — dentro del OpinarBottomSheet
                onEnviar = { estrellas, comentario ->
                    scope.launch {
                        try {
                            repoReseñas.crearReseña(
                                creditoId         = creditoParaOpinar!!.id,
                                productoId        = creditoParaOpinar!!.productoId,
                                productoNombre    = creditoParaOpinar!!.productoNombre,
                                empleadoId        = empleadoCargado?.codigoEmpleado ?: "",
                                empleadoNombres   = empleadoCargado?.nombres ?: "",
                                empleadoApellidos = empleadoCargado?.apellidos ?: "",
                                estrellas         = estrellas,
                                comentario        = comentario,
                                visible           = true
                            )
                            mostrarSheetOpinar = false
                        } catch (e: Exception) {
                            // Si ya opinó o cualquier error — cerramos sin crash
                            mostrarSheetOpinar = false
                        }
                    }
                }
            )
        }
    }
}

fun autenticarConBiometria(activity: FragmentActivity, onExito: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(
        activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Handler(Looper.getMainLooper()).postDelayed({ onExito() }, 100)
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso de Seguridad")
        .setSubtitle("Usa tu huella o PIN")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    biometricPrompt.authenticate(promptInfo)
}