package com.example.comisariatoproyecto

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.m_Categoria
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Categoria
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.data.r_CuotaCredito
import com.example.comisariatoproyecto.data.r_Productos
import com.example.comisariatoproyecto.data.r_permisos

import com.example.comisariatoproyecto.ui.pantallas.DetalleProducto
import com.example.comisariatoproyecto.ui.pantallas.ListaProductos
import com.example.comisariatoproyecto.ui.pantallas.LoginComisariatoScreen
import com.example.comisariatoproyecto.ui.pantallas.MenuInferiorComisariato

import com.example.comisariatoproyecto.ui.pantallas.PantallaCredito
import com.example.comisariatoproyecto.ui.pantallas.PantallaInicio
import com.example.comisariatoproyecto.ui.pantallas.PerfilScreen
import com.example.comisariatoproyecto.ui.pantallas.ProductosCatalogo

import com.example.comisariatoproyecto.ui.pantallas.ConfirmarReserva
import com.example.comisariatoproyecto.ui.theme.ComisariatoProyectoTheme
import com.example.comisariatoproyecto.ui.theme.NavyPrimary
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Firebase.initialize(context = this)
        val firebaseAppCheck = Firebase.appCheck
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // 🔥
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

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val repoAuth = remember { r_permisos() }
    val repocat = remember { r_Categoria() }
    val repoprod = remember { r_Productos() }
    val repocreditos = remember { r_Creditos() }
    val repocuotas = remember { r_CuotaCredito() }

    var startDestination by rememberSaveable { mutableStateOf<String?>(null) }

    var productoSeleccionado by remember { mutableStateOf<m_Productos?>(null) }


    var plazoSeleccionadoReserva by remember { mutableStateOf<Int?>(null) }
    var cantidadSeleccionadaReserva by remember { mutableIntStateOf(1) }

    var empleadoCargado by remember { mutableStateOf<Empleado?>(null) }

    LaunchedEffect(Unit) {
        try {
            val logged = repoAuth.isLogged()
            if (logged) {
                // Cargamos el perfil una sola vez aquí para TODA la app
                val perfil = repoAuth.obtenerMiPerfilCompleto()
                empleadoCargado = perfil.second
                startDestination = "inicio"
            } else {
                startDestination = "login"
            }
        } catch (e: Exception) {
            startDestination = "login"
        }
    }

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val navBackStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val mostrarMenu =
            currentRoute == "inicio" ||
                    currentRoute == "catalogo" ||
                    currentRoute == "credito" ||
                    currentRoute == "perfil"

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (mostrarMenu) {
                    MenuInferiorComisariato(
                        itemSeleccionado = when (currentRoute) {
                            "inicio" -> "Inicio"
                            "catalogo" -> "Catálogo"
                            "credito" -> "Mi Crédito"
                            "perfil" -> "Perfil"
                            else -> "Inicio"
                        },
                        onItemClick = { item ->
                            val ruta = when (item) {
                                "Inicio" -> "inicio"
                                "Catálogo" -> "catalogo"
                                "Mi Crédito" -> "credito"
                                "Perfil" -> "perfil"
                                else -> "inicio"
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
                modifier = Modifier.padding(innerPadding)
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
                        repo = repoAuth,
                        repoCreditos = repocreditos,

                        onLogout = {
                            nav.navigate("login") {
                                popUpTo("inicio") { inclusive = true }
                            }
                        }
                    )
                }

                composable("catalogo") {
                    ProductosCatalogo(
                        repoCategoria = repocat,
                        repoProducto = repoprod,
                        onBack = { nav.popBackStack() },
                        onVerProductos = { categoria ->
                            nav.navigate("productos/${categoria.id}/${categoria.nombre}")
                        },
                        onVerDetalleProducto = { prod ->
                            // 2. Guardamos el producto y navegamos
                            productoSeleccionado = prod
                            nav.navigate("detalleProducto")
                        }
                    )
                }

                composable("productos/{categoriaId}/{categoriaNombre}") { backStack ->
                    val categoriaId = backStack.arguments?.getString("categoriaId") ?: return@composable
                    val categoriaNombre = backStack.arguments?.getString("categoriaNombre") ?: ""

                    val categoria = remember(categoriaId) {
                        m_Categoria(id = categoriaId, nombre = categoriaNombre)
                    }

                    ListaProductos (
                        categoria = categoria,
                        repo = repoprod,
                        onBack = { nav.popBackStack() },
                        onVerDetalle = { producto ->
                            productoSeleccionado = producto
                            nav.navigate("detalleProducto")
                        }
                    )
                }



                composable("credito") {
                    PantallaCredito()
                }

                composable("perfil") {
                    PerfilScreen(
                        repo = repoAuth,
                    )

                }

                composable("detalleProducto") {
                    productoSeleccionado?.let { producto ->

                        DetalleProducto(
                            producto = producto,
                            repoCuotas = repocuotas,
                            repoCreditos = repocreditos,
                            empleado = empleadoCargado,
                            onBack = { nav.popBackStack() },
                            onReservar = { prod, plazo, cant ->
                                productoSeleccionado = prod
                                plazoSeleccionadoReserva = plazo
                                cantidadSeleccionadaReserva = cant
                                nav.navigate("confirmarReserva")
                            }
                        )
                    }
                }

                composable("confirmarReserva") {

                    productoSeleccionado?.let { producto ->
                        if (empleadoCargado != null) {
                            ConfirmarReserva(
                                producto = producto,
                                repoCreditos = repocreditos,
                                plazoMeses = plazoSeleccionadoReserva,
                                cantidad = cantidadSeleccionadaReserva,
                                empleado = empleadoCargado!!,
                                onBack = { nav.popBackStack() },
                                onConfirmar = {
                                    nav.navigate("inicio") {
                                        popUpTo("inicio") { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            // Mientras Firebase responde, mostramos carga
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
                // Usamos un Handler para dar un respiro de 100ms y que el diálogo se cierre bien
                Handler(Looper.getMainLooper()).postDelayed({
                    onExito()
                }, 100)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Loguear el error ayuda a saber por qué falló
                Log.e("BIOMETRIA", "Error: $errString")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Acceso de Seguridad")
        .setSubtitle("Usa tu PIN, Patrón o Rostro")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .setConfirmationRequired(false) // Esto hace que entre directo al validar
        .build()

    biometricPrompt.authenticate(promptInfo)
}








