package com.example.comisariatoproyecto

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.m_Categoria
import com.example.comisariatoproyecto.data.m_CreditoDetalle
import com.example.comisariatoproyecto.data.m_Creditos
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Categoria
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_CuotaCredito
import com.example.comisariatoproyecto.data.r_Productos
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.pantallas.ConfirmarReserva
import com.example.comisariatoproyecto.ui.pantallas.DetalleProducto
import com.example.comisariatoproyecto.ui.pantallas.ListaProductos
import com.example.comisariatoproyecto.ui.pantallas.LoginComisariatoScreen
import com.example.comisariatoproyecto.ui.pantallas.MenuInferiorComisariato
import com.example.comisariatoproyecto.ui.pantallas.PantallaCredito
import com.example.comisariatoproyecto.ui.pantallas.PantallaDetalleCredito
import com.example.comisariatoproyecto.ui.pantallas.PantallaInicio
import com.example.comisariatoproyecto.ui.pantallas.PerfilScreen
import com.example.comisariatoproyecto.ui.pantallas.ProductosCatalogo
import com.example.comisariatoproyecto.ui.theme.ComisariatoProyectoTheme
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.example.comisariatoproyecto.utils.SessionPrefs
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import kotlinx.coroutines.launch
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.Notification
import com.example.comisariatoproyecto.ui.pantallas.ComentariosProducto
import com.example.comisariatoproyecto.ui.pantallas.OpinarBottomSheet


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Firebase.initialize(context = this)
        val firebaseAppCheck = Firebase.appCheck
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        super.onCreate(savedInstanceState)

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
            "canal_id",
            "Canal General",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones del sistema"
        }

        val manager = activity.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }
}

fun mostrarNotificacion(activity: MainActivity, titulo: String, mensaje: String) {
    val builder = NotificationCompat.Builder(activity, "canal_id")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setAutoCancel(true)

    val manager = activity.getSystemService(NotificationManager::class.java)
    manager.notify(System.currentTimeMillis().toInt(), builder.build())
}

@Composable
fun AppNavigation() {
    val nav          = rememberNavController()
    val repoAuth     = remember { r_permisos() }
    val repocat      = remember { r_Categoria() }
    val repoprod     = remember { r_Productos() }
    val repocreditos = remember { r_Creditos() }
    val repocuotas   = remember { r_CuotaCredito() }
    val context      = LocalContext.current
    val sessionPrefs = remember { SessionPrefs(context) }

    var startDestination by rememberSaveable { mutableStateOf<String?>(null) }

    var productoSeleccionado        by remember { mutableStateOf<m_Productos?>(null) }
    var creditoSeleccionado         by remember { mutableStateOf<m_CreditoDetalle?>(null) }
    var plazoSeleccionadoReserva    by remember { mutableStateOf<Int?>(null) }
    var cantidadSeleccionadaReserva by remember { mutableIntStateOf(1) }
    var empleadoCargado             by remember { mutableStateOf<Empleado?>(null) }
    var reservasEmpleado            by remember { mutableStateOf<List<m_Creditos>>(emptyList()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val firebaseLogged         = repoAuth.isLogged()
            val hayCredencialesLocales = sessionPrefs.hayUsuarioRegistrado() &&
                    sessionPrefs.obtenerCorreo().isNotBlank() &&
                    sessionPrefs.obtenerPassword().isNotBlank()

            if (firebaseLogged && hayCredencialesLocales) {
                val perfil      = repoAuth.obtenerMiPerfilCompleto()
                empleadoCargado = perfil.second

                empleadoCargado?.let { emp ->
                    reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                }

                startDestination = "inicio"
            } else {
                repoAuth.logout()
                startDestination = "login"
            }
        } catch (e: Exception) {
            repoAuth.logout()
            startDestination = "login"
        }
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val navBackStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // "detalleCredito" no está aquí a propósito → el menú se oculta automáticamente
        val mostrarMenu = currentRoute == "inicio"   ||
                currentRoute == "catalogo" ||
                currentRoute == "credito"  ||
                currentRoute == "perfil"

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (mostrarMenu) {
                    MenuInferiorComisariato(
                        itemSeleccionado = when (currentRoute) {
                            "inicio"   -> "Inicio"
                            "catalogo" -> "Catálogo"
                            "credito"  -> "Mi Crédito"
                            "perfil"   -> "Perfil"
                            else       -> "Inicio"
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
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->

            NavHost(
                navController    = nav,
                startDestination = startDestination!!,
                modifier         = Modifier.padding(innerPadding)
            ) {

                composable("login") {
                    LoginComisariatoScreen(
                        repo = repoAuth,
                        onLoginSuccess = {
                            nav.navigate("inicio") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }

                composable("inicio") {
                    PantallaInicio(
                        repo         = repoAuth,
                        repoCreditos = repocreditos,
                        onLogout     = {
                            nav.navigate("login") {
                                popUpTo("inicio") { inclusive = true }
                            }
                        },
                        onIrCatalogo = {
                            nav.navigate("catalogo") {
                                popUpTo("inicio") { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        onIrCredito  = {
                            nav.navigate("credito") {
                                popUpTo("inicio") { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }

                composable("catalogo") {
                    ProductosCatalogo(
                        repoCategoria        = repocat,
                        repoProducto         = repoprod,
                        onBack               = { nav.popBackStack() },
                        onVerProductos       = { categoria ->
                            nav.navigate("productos/${categoria.id}/${categoria.nombre}")
                        },
                        onVerDetalleProducto = { prod ->
                            productoSeleccionado = prod
                            nav.navigate("detalleProducto")
                        }
                    )
                }

                composable("productos/{categoriaId}/{categoriaNombre}") { backStack ->
                    val categoriaId     = backStack.arguments?.getString("categoriaId") ?: return@composable
                    val categoriaNombre = backStack.arguments?.getString("categoriaNombre") ?: ""
                    val categoria       = remember(categoriaId) {
                        m_Categoria(id = categoriaId, nombre = categoriaNombre)
                    }
                    ListaProductos(
                        categoria    = categoria,
                        repo         = repoprod,
                        onBack       = { nav.popBackStack() },
                        onVerDetalle = { producto ->
                            productoSeleccionado = producto
                            nav.navigate("detalleProducto")
                        }
                    )
                }

                // ── Mi Crédito ────────────────────────────────────────────────
                composable("credito") {
                    PantallaCredito(
                        onVerDetalle = { credito ->
                            creditoSeleccionado = credito
                            nav.navigate("detalleCredito")
                        }
                    )
                }

                // ── Detalle de crédito ────────────────────────────────────────
                composable("detalleCredito") {
                    creditoSeleccionado?.let { credito ->
                        PantallaDetalleCredito(
                            credito = credito,
                            onBack  = { nav.popBackStack() }
                        )
                    }
                }

                // ── Perfil ────────────────────────────────────────────────────
                composable("perfil") {
                    PerfilScreen(repo = repoAuth)
                }

                // ── Detalle de producto ───────────────────────────────────────
                composable("detalleProducto") {
                    productoSeleccionado?.let { producto ->
                        val reservaPendiente = reservasEmpleado.firstOrNull {
                            it.productoId == producto.id && it.estado == "Pendiente"
                        }
                        DetalleProducto(
                            producto          = producto,
                            repoCuotas        = repocuotas,
                            repoCreditos      = repocreditos,
                            reservaPendiente  = reservaPendiente,
                            empleado          = empleadoCargado,
                            onBack            = { nav.popBackStack() },
                            onCancelarReserva = { reservaId ->
                                scope.launch {
                                    repocreditos.cancelarReserva(reservaId)
                                    reservasEmpleado = reservasEmpleado.map {
                                        if (it.id == reservaId) it.copy(estado = "Cancelado") else it
                                    }
                                }
                            },
                            onVerComentarios = { id ->
                                nav.navigate("comentariosProducto")
                            },

                            onReservar = { prod, plazo, cant ->
                                productoSeleccionado          = prod
                                plazoSeleccionadoReserva      = plazo
                                cantidadSeleccionadaReserva   = cant
                                nav.navigate("confirmarReserva")
                            }
                        )
                    }
                }
// ── Comentarios de producto ───────────────────────────────────────
                // ── Comentarios de producto ───────────────────────────────────────
                composable("comentariosProducto") {
                    // Usamos el patrón de "let" igual que en detalleProducto
                    productoSeleccionado?.let { producto ->
                        var mostrarSheet by remember { mutableStateOf(false) }

                        ComentariosProducto(
                            productoId = producto.id,
                            onBack     = { nav.popBackStack() },
                            onOpinar   = { mostrarSheet = true }
                        )

                        // El BottomSheet se dispara con el estado local
                        if (mostrarSheet) {
                            OpinarBottomSheet(
                                productoNombre    = producto.nombre,
                                productoImagenUrl = producto.imagenUrl,
                                onDismiss         = { mostrarSheet = false },
                                onEnviar          = { estrellas, comentario ->
                                    // Aquí irá tu lógica de Firebase en el futuro
                                    mostrarSheet = false
                                }
                            )
                        }
                    }
                }
                // ── Confirmar reserva ─────────────────────────────────────────
                composable("confirmarReserva") {
                    productoSeleccionado?.let { producto ->
                        if (empleadoCargado != null) {
                            ConfirmarReserva(
                                producto     = producto,
                                repoCreditos = repocreditos,
                                plazoMeses   = plazoSeleccionadoReserva,
                                cantidad     = cantidadSeleccionadaReserva,
                                empleado     = empleadoCargado!!,
                                onBack       = { nav.popBackStack() },
                                onConfirmar  = {
                                    scope.launch {
                                        empleadoCargado?.let { emp ->
                                            reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                                        }
                                    }
                                    nav.popBackStack()
                                }
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = NavyPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun autenticarConBiometria(
    activity: FragmentActivity,
    onExito: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(
        activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Handler(Looper.getMainLooper()).postDelayed({ onExito() }, 100)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e("BIOMETRIA", "Error: $errString")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso de Seguridad")
        .setSubtitle("Usa tu PIN, Patrón o Rostro")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .setConfirmationRequired(false)
        .build()

    biometricPrompt.authenticate(promptInfo)
}