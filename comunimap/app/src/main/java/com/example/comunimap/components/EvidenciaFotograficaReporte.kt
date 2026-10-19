package com.example.comunimap.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.comunimap.utils.obtenerUrlImagenReporte

/**
 * Componente reutilizable para presentar la evidencia fotográfica de un reporte.
 *
 * REGLAS DE NEGOCIO:
 * - Si el reporte no tiene imagen (null o vacía), NO renderiza nada.
 * - Si tiene imagen, muestra el título "Evidencia fotográfica" y la imagen en formato estilizado.
 * - Durante la carga: muestra un indicador circular discreto.
 * - En caso de error al cargar: muestra un aviso visual discreto sin detalles técnicos ni excepciones.
 * - Al tocar la imagen: abre un diálogo modal para visualizarla en mayor tamaño.
 */
@Composable
fun SeccionEvidenciaFotografica(
    rutaImagen: String?,
    modifier: Modifier = Modifier,
    altura: Dp = 180.dp,
    tituloColor: Color = Color.White
) {
    val urlCompleta = obtenerUrlImagenReporte(rutaImagen) ?: return

    var mostrarDialogoAmpliada by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Evidencia fotográfica",
                color = tituloColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { mostrarDialogoAmpliada = true }
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Ampliar fotografía",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tocar para ampliar",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(altura)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(14.dp))
                .clickable { mostrarDialogoAmpliada = true }
                .testTag("evidencia_fotografica_box"),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = urlCompleta,
                contentDescription = "Evidencia fotográfica del reporte",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF38BDF8),
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .background(Color(0xFF0F172A)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No se pudo cargar la imagen",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )
        }
    }

    // Diálogo opcional para ampliar la fotografía al tocarla
    if (mostrarDialogoAmpliada) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAmpliada = false },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            title = {
                Text(
                    text = "Evidencia fotográfica",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A0F1D))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = urlCompleta,
                        contentDescription = "Evidencia fotográfica ampliada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No se pudo cargar la imagen",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp
                                )
                            }
                        },
                        success = {
                            SubcomposeAsyncImageContent()
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoAmpliada = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        )
    }
}
