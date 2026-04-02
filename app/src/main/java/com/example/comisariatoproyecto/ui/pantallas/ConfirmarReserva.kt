package com.example.comisariatoproyecto.ui.pantallas
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.comisariatoproyecto.data.Empleado
import com.example.comisariatoproyecto.data.m_Productos
import com.example.comisariatoproyecto.data.r_Creditos
import com.example.comisariatoproyecto.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmarReserva(
    repoCreditos: r_Creditos,
    producto: m_Productos?,
    plazoMeses: Int?,
    cantidad: Int,
    empleado: Empleado,
    onBack: () -> Unit,
    onConfirmar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enviando by remember { mutableStateOf(false) }

    fun formatLps(valor: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("es", "HN"))
        fmt.minimumFractionDigits = 2
        fmt.maximumFractionDigits = 2
        return "L. ${fmt.format(valor)}"
    }

    val esCredito = plazoMeses != null
    val precioBase = if (esCredito) producto?.precioCredito ?: 0.0 else producto?.precioContado ?: 0.0
    val totalFinal = precioBase * cantidad
    val cuotaMensual = if (esCredito && (plazoMeses ?: 0) > 0) totalFinal / plazoMeses!! else null

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Reserva", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            if (producto != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                enviando = true
                                try {
                                    repoCreditos.crearReserva(producto, cantidad, plazoMeses, empleado)
                                    Toast.makeText(context, "Reserva enviada con éxito", Toast.LENGTH_SHORT).show()
                                    onConfirmar()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    enviando = false
                                }
                            }
                        },
                        enabled = !enviando,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        if (enviando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("CONFIRMAR SOLICITUD", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (producto == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))

            // -- Card de Producto --
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, NavyPrimary.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(producto.nombre, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        Text(if (esCredito) "Crédito a $plazoMeses meses" else "Pago al Contado", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text(formatLps(totalFinal), fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // -- Card de Resumen --
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite)
                    .border(0.5.dp, NavyPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("RESUMEN DE PAGO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))

                FilaTexto("Cantidad:", "$cantidad ${if(cantidad==1) "unidad" else "unidades"}")
                FilaTexto("Precio unitario:", formatLps(precioBase))

                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = SurfaceBase)

                if (esCredito && cuotaMensual != null) {
                    FilaTexto("Cuota mensual:", formatLps(cuotaMensual), esNegrita = true)
                } else {
                    FilaTexto("Total a pagar:", formatLps(totalFinal), esNegrita = true)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Banner Info
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(NavyPrimary.copy(alpha = 0.05f)).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Info, null, Modifier.size(18.dp), tint = NavyPrimary.copy(alpha = 0.5f))
                Text(
                    "Tu reserva sera evaluada por un asesor. El estado de tu reserva se mostrara en el inicio.",
                    fontSize = 12.sp, color = NavyPrimary.copy(alpha = 0.7f), lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun FilaTexto(label: String, valor: String, esNegrita: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(valor, fontSize = 14.sp, fontWeight = if (esNegrita) FontWeight.Bold else FontWeight.Medium, color = NavyPrimary)
    }
}