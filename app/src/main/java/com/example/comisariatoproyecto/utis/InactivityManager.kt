package com.example.comisariatoproyecto.utils

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

// Tiempo de inactividad en milisegundos — 60 segundos
const val INACTIVIDAD_MS = 60_000L

@Composable
fun rememberInactivityTimer(
    habilitado: Boolean,        // solo activo cuando el usuario está logueado
    onTimeout: () -> Unit
): () -> Unit {

    var ultimaActividad by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Función para resetear el timer — se llama en cada toque
    val resetTimer: () -> Unit = {
        ultimaActividad = System.currentTimeMillis()
    }
    LaunchedEffect(habilitado) {
        if (!habilitado) return@LaunchedEffect

        // ✅ Resetear al activarse — evita falsos positivos si el usuario
        // tardó mucho en el login screen
        ultimaActividad = System.currentTimeMillis()

        while (true) {
            delay(1_000L)
            val inactivoMs = System.currentTimeMillis() - ultimaActividad
            if (inactivoMs >= INACTIVIDAD_MS) {
                onTimeout()
                break
            }
        }
    }

    return resetTimer
}

// Modifier para detectar cualquier toque y avisar al timer
fun Modifier.detectarActividad(onActividad: () -> Unit): Modifier =
    this.pointerInput(Unit) {
        detectTapGestures(
            onPress   = { onActividad() },
            onTap     = { onActividad() }
        )
    }