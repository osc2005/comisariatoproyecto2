package com.example.comisariatoproyecto.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.comisariatoproyecto.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpinarBottomSheet(
    productoNombre: String,
    productoImagenUrl: String,
    onDismiss: () -> Unit,
    onEnviar: (estrellas: Int, comentario: String) -> Unit
) {
    var estrellasSeleccionadas by remember { mutableIntStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NavyPrimary.copy(alpha = 0.12f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // ── Header: imagen + nombre del producto ─────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceBase)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = productoImagenUrl,
                    contentDescription = productoNombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyContainer)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "PRODUCTO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        productoNombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary,
                        lineHeight = 20.sp
                    )
                }
            }

            HorizontalDivider(color = NavyPrimary.copy(alpha = 0.08f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "¿Cómo calificas el producto?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val rellena = index < estrellasSeleccionadas
                        Icon(
                            imageVector = if (rellena) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = "${index + 1} estrella",
                            tint = if (rellena) YellowStars else TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { estrellasSeleccionadas = index + 1 }
                        )
                        if (index < 4) Spacer(Modifier.width(4.dp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Escribe tu comentario", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
                        Text("* Obligatorio", fontSize = 11.sp, color = Color.Red.copy(alpha = 0.7f))
                    }
                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { if (it.length <= 300) comentario = it },
                        placeholder = { Text("Cuéntanos tu experiencia...", fontSize = 13.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = NavyPrimary.copy(alpha = 0.15f),
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceBase
                        ),
                        maxLines = 5
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { onEnviar(estrellasSeleccionadas, comentario) },
                        enabled = estrellasSeleccionadas > 0 && comentario.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Enviar opinión", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            }
        }
    }
}