package com.example.comisariatoproyecto.ui.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comisariatoproyecto.ui.theme.TextSecondary
import com.example.comisariatoproyecto.ui.theme.YellowStars

@Composable
fun RatingBar(
    promedio: Double,
    starSize: Dp = 14.dp,
    mostrarTexto: Boolean = true,
    modifier: Modifier = Modifier
) {
    val estrellasLlenas = promedio.toInt()
    val mediaEstrella   = (promedio - estrellasLlenas) >= 0.5

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(5) { index ->
            when {
                index < estrellasLlenas -> Icon(
                    imageVector        = Icons.Filled.Star,
                    contentDescription = null,
                    tint               = YellowStars,
                    modifier           = Modifier.size(starSize)
                )
                index == estrellasLlenas && mediaEstrella -> Icon(
                    imageVector        = Icons.AutoMirrored.Filled.StarHalf,
                    contentDescription = null,
                    tint               = YellowStars,
                    modifier           = Modifier.size(starSize)
                )
                else -> Icon(
                    imageVector        = Icons.Filled.StarOutline,
                    contentDescription = null,
                    tint               = TextSecondary.copy(alpha = 0.2f),
                    modifier           = Modifier.size(starSize)
                )
            }
        }

        if (mostrarTexto) {
            Spacer(Modifier.width(4.dp))
            Text(
                text       = String.format(java.util.Locale.US, "%.1f", promedio),
                fontSize   = (starSize.value * 0.9).sp,
                fontWeight = FontWeight.Bold,
                color      = TextSecondary
            )
        }
    }
}
