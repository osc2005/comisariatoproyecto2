package com.example.comisariatoproyecto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */

private val DarkColorScheme = darkColorScheme(
    primary = SurfaceWhite,        // En modo oscuro el texto/iconos resaltan en blanco
    secondary = TextSecondary,
    tertiary = NavyContainer,
    background = NavyPrimary,      // Fondo azul profundo
    surface = NavyContainer,       // Cards azul marino
    onPrimary = NavyPrimary,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

// Configuración para el tema claro (Tu paleta oficial)
private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,         // El azul de confianza
    onPrimary = Color.White,       // Texto sobre azul
    primaryContainer = NavyContainer,
    onPrimaryContainer = Color.White,
    secondary = TextSecondary,     // Subtítulos
    onSecondary = Color.White,
    background = SurfaceBase,      // Fondo gris azulado claro (8px grid)
    onBackground = NavyPrimary,    // Texto sobre fondo general
    surface = SurfaceWhite,        // Fondo de Cards y menús
    onSurface = NavyPrimary,       // Texto sobre Cards
    onSurfaceVariant = TextSecondary // Texto descriptivo
)

@Composable
fun ComisariatoProyectoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}