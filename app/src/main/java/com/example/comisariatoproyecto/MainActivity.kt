package com.example.comisariatoproyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.comisariatoproyecto.data.r_permisos
import com.example.comisariatoproyecto.ui.pantallas.LoginComisariatoScreen
import com.example.comisariatoproyecto.ui.theme.ComisariatoProyectoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComisariatoProyectoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val nav = rememberNavController()
                    val repoAuth = remember { r_permisos() }
                    var startDestination by remember { mutableStateOf<String?>(null) }


                    // Lógica de verificación de sesión
                    LaunchedEffect(Unit) {
                        try {
                            val logged = repoAuth.isLogged()
                            startDestination = if (logged) "inicio" else "login"
                        } catch (e: Exception) {
                            startDestination = "login"
                        }
                    }
                    if (startDestination == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        NavHost(
                            navController = nav,
                            startDestination = startDestination!!
                        ) {
                            composable("login") {
                                // Asegúrate de que la función Login esté definida
                                LoginComisariatoScreen(
                                    repo = repoAuth,
                                    onLoginSuccess = {
                                        nav.navigate("inicio")
                                    }
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}


