package com.example.comisariatoproyecto

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(context = this)

        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

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
fun mostrarNotificacion(context: android.content.Context, titulo: String, mensaje: String) {
    val builder = androidx.core.app.NotificationCompat.Builder(context, "canal_id")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
    context.getSystemService(android.app.NotificationManager::class.java)
        ?.notify(System.currentTimeMillis().toInt(), builder.build())
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
    var rolUsuario by remember { mutableStateOf<String?>(null) } // Estado para guardar el rol    // Estados
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
//ficación de sesión inicial
    LaunchedEffect(Unit) {
        try {
            if (repoAuth.isLogged() && sessionPrefs.obtenerCorreo().isNotBlank()) {
                val perfil = repoAuth.obtenerMiPerfilCompleto()
                empleadoCargado = perfil.second
                empleadoCargado?.let { emp ->
                    reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                }
                // ✅ Cargar rol aquí también — no solo en onLoginSuccess
                rolUsuario = repoAuth.cargarRolUsuario()
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
    val rutasMenu = when (rolUsuario) {
        "Gestor Precios" -> setOf("inicio", "catalogo", "perfil")         // sin crédito
        "Administrador"  -> setOf("inicio", "catalogo", "credito", "perfil", "admin")
        else             -> setOf("inicio", "catalogo", "credito", "perfil")
    }
    val mostrarMenu = currentRoute in rutasMenu
    // Lógica de Logout
    val hacerLogoutCompleto: () -> Unit = {
        repoAuth.logout()
        sessionPrefs.cerrarSesionPorInactividad()
        empleadoCargado = null
        reservasEmpleado = emptyList()
        nav.navigate("login") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val resetTimer = rememberInactivityTimer(
        habilitado = usuarioLogueado && !mostrarDialogoInactividad,
        onTimeout = { mostrarDialogoInactividad = true }
    )

    LaunchedEffect(empleadoCargado?.codigoEmpleado) {
        val codigo = empleadoCargado?.codigoEmpleado ?: return@LaunchedEffect

        while (true) {
            try {
                val reservas = repocreditos.obtenerReservasParaNotificar(codigo)
                val yaNotificados = sessionPrefs.obtenerIdsNotificados().toMutableSet()

                reservas.forEach { reserva ->
                    if (reserva.id !in yaNotificados) {
                        val emoji = if (reserva.estado == "Aprobado") "✅" else "❌"
                        mostrarNotificacion(
                            context = context,
                            titulo  = "$emoji Reserva ${reserva.estado}",
                            mensaje = "Tu reserva de ${reserva.productoNombre} fue ${reserva.estado}."
                        )
                        yaNotificados.add(reserva.id)
                    }
                }
                sessionPrefs.guardarIdsNotificados(yaNotificados)

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e  // ✅ deja que Compose la cancele al hacer logout
            } catch (e: Exception) {
                Log.e("POLLING", "Error: ${e.message}")
            }

            delay(30_000L)  // ✅ espera 30s antes de volver a consultar
        }
    }


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
                        empleadoCargado = null
                        reservasEmpleado = emptyList()
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
                // 1. Creamos la lista basada estrictamente en el rol actual
                val itemsMenu = when (rolUsuario) {
                    "Gestor Precios" -> listOf("Inicio", "Catálogo", "Perfil")
                    else             -> listOf("Inicio", "Catálogo", "Mi Crédito", "Perfil")
                }

                MenuInferiorComisariato(
                    items            = itemsMenu,
                    itemSeleccionado = when (currentRoute) {
                        "inicio"   -> "Inicio"
                        "catalogo" -> "Catálogo"
                        "credito"  -> "Mi Crédito"
                        "perfil"   -> "Perfil"
                        else       -> "Inicio"
                    },
                    onItemClick = { item ->
                        // 2. Aquí está el truco: Solo navegamos si el ítem está en nuestra lista permitida
                        if (item in itemsMenu) {
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
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = nav,
                startDestination = startDestination!!,
                modifier = Modifier.fillMaxSize()
            ) {
                composable("login") {
                    LoginComisariatoScreen(
                        repo = repoAuth,
                        onLoginSuccess = {
                            scope.launch {
                                try {
                                    val perfil = repoAuth.obtenerMiPerfilCompleto()
                                    empleadoCargado = perfil.second
                                    empleadoCargado?.let { emp ->
                                        reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)
                                    }
                                    // ✅ Carga el rol en variable local antes de navegar
                                    val rol = repoAuth.cargarRolUsuario()
                                    rolUsuario = rol

                                    val nombre = empleadoCargado?.nombres ?: "Usuario"
                                    mostrarNotificacion(
                                        context = context,
                                        titulo  = "¡Bienvenido, $nombre $rol!  ",
                                        mensaje = "Sesión iniciada correctamente en Comisariato."
                                    )

                                    // ✅ Redirige según rol usando variable local (nunca null)
                                    val destino = when (rol) {
                                        "Gestor Precios" -> "gestion_precios"
                                        "Administrador"  -> "admin"
                                        else             -> "inicio"
                                    }
                                    nav.navigate(destino) { popUpTo("login") { inclusive = true } }

                                } catch (e: Exception) {
                                    nav.navigate("inicio") { popUpTo("login") { inclusive = true } }
                                }
                            }
                        }
                    )
                }

                composable("inicio") {
                    PantallaInicio(
                        repo = repoAuth,
                        repoCreditos = repocreditos,
                        onLogout = hacerLogoutCompleto,
                        onIrCatalogo = {
                            nav.navigate("catalogo") {
                                popUpTo("inicio") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        rolUsuario = rolUsuario,
                        onIrCredito = {
                            nav.navigate("credito") {
                                popUpTo("inicio") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
                        repoReseñas = repoReseñas,
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
                            empleadoId  = empleadoCargado?.codigoEmpleado ?: "",
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

                composable("comentariosProducto") {
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
                                            reservasEmpleado = repocreditos.obtenerReservasDeEmpleado(emp.codigoEmpleado)

                                            // ✅ Guardar IDs de reservas pendientes para no re-notificar
                                            val pendientes = reservasEmpleado
                                                .filter { it.estado == "Pendiente" }
                                                .map { it.id }
                                                .toSet()
                                            val yaNotificados = sessionPrefs.obtenerIdsNotificados().toMutableSet()
                                            yaNotificados.addAll(pendientes)
                                            sessionPrefs.guardarIdsNotificados(yaNotificados)
                                        }
                                    }
                                    mostrarNotificacion(
                                        context = context,
                                        titulo  = "Reserva enviada",
                                        mensaje = "Tu reserva de ${prod.nombre} fue registrada. Te avisaremos cuando sea revisada."
                                    )
                                    nav.popBackStack()
                                }
                            )
                        }
                    }
                }
            }

            if (mostrarSheetOpinar && creditoParaOpinar != null) {
                OpinarBottomSheet(
                    productoNombre    = creditoParaOpinar!!.productoNombre,
                    productoImagenUrl = creditoParaOpinar!!.productoImgUrl,
                    onDismiss         = { mostrarSheetOpinar = false },
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
                                mostrarSheetOpinar = false
                            }
                        }
                    }
                )
            }
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
