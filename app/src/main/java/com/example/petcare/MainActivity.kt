package com.example.petcare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.petcare.model.Mascota
import com.example.petcare.model.Reporte
import com.example.petcare.viewmodel.PetCareViewModel

private val Morado = Color(0xFF813D91)
private val Rosa = Color(0xFFF3E4F4)
private val Fondo = Color(0xFFFFFBFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PetCareApp()
            }
        }
    }
}

@Composable
fun PetCareApp() {
    var pantalla by remember { mutableStateOf("inicio") }
    val petCareViewModel: PetCareViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Fondo
    ) {
        when (pantalla) {
            "inicio" -> PantallaInicio(
                onBuscar = { pantalla = "buscar" },
                onReportar = { pantalla = "reportar" }
            )

            "buscar" -> PantallaBuscar(
                reportes = petCareViewModel.reportes,
                onRegresar = { pantalla = "inicio" }
            )

            "reportar" -> PantallaReportar(
                onPublicar = { reporte ->
                    petCareViewModel.agregarReporte(reporte)
                    pantalla = "buscar"
                },
                onRegresar = { pantalla = "inicio" }
            )
        }
    }
}

@Composable
fun PantallaInicio(
    onBuscar: () -> Unit,
    onReportar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🐾 PetCare",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Morado
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Encuentra a tu mejor amigo",
            fontSize = 20.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onBuscar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Morado)
        ) {
            Text("Buscar mascota")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onReportar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Morado)
        ) {
            Text("Reportar mascota")
        }
    }
}

@Composable
fun PantallaBuscar(
    reportes: List<Reporte>,
    onRegresar: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("Todos") }

    val resultados = reportes.filter { reporte ->
        val mascota = reporte.mascota
        val coincideTexto =
            mascota.nombre.contains(busqueda, ignoreCase = true) ||
                    mascota.ubicacion.contains(busqueda, ignoreCase = true) ||
                    mascota.descripcion.contains(busqueda, ignoreCase = true)

        val coincideEstado =
            filtro == "Todos" || reporte.tipo == filtro

        coincideTexto && coincideEstado
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "🐾 PetCare",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Morado
        )

        Text(text = "Buscar mascotas", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Zona o descripción") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Todos", "Perdido", "Encontrado").forEach { opcion ->
                FilterChip(
                    selected = filtro == opcion,
                    onClick = { filtro = opcion },
                    label = {
                        Text(
                            when (opcion) {
                                "Perdido" -> "Perdidos"
                                "Encontrado" -> "Encontrados"
                                else -> "Todos"
                            }
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resultados.isEmpty()) {
            Text("No se encontraron mascotas.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resultados) { reporte ->
                    TarjetaMascota(reporte.mascota, reporte.tipo)
                }
            }
        }

        Button(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Morado)
        ) {
            Text("Regresar al inicio")
        }
    }
}

@Composable
fun PantallaReportar(
    onPublicar: (Reporte) -> Unit,
    onRegresar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Perdido") }
    var foto by remember { mutableStateOf<Uri?>(null) }
    var mensaje by remember { mutableStateOf("") }

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        foto = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🐾 Nuevo reporte",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Morado
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = estado == "Perdido",
                onClick = { estado = "Perdido" },
                label = { Text("Mascota perdida") }
            )

            FilterChip(
                selected = estado == "Encontrado",
                onClick = { estado = "Encontrado" },
                label = { Text("Mascota encontrada") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la mascota") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Última ubicación") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Características de la mascota") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono de WhatsApp") },
            supportingText = {
                Text("Incluye el código de país; México es 52.")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { selectorFoto.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Morado)
        ) {
            Text("📷 Agregar fotografía")
        }

        if (foto != null) {
            Spacer(modifier = Modifier.height(12.dp))

            AsyncImage(
                model = foto,
                contentDescription = "Fotografía de la mascota",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            TextButton(onClick = { foto = null }) {
                Text("Quitar fotografía")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val telefonoLimpio = telefono.filter { it.isDigit() }

                if (
                    nombre.isNotBlank() &&
                    ubicacion.isNotBlank() &&
                    descripcion.isNotBlank() &&
                    telefonoLimpio.length >= 10
                ) {
                    val mascota = Mascota(
                        nombre = nombre.trim(),
                        ubicacion = ubicacion.trim(),
                        descripcion = descripcion.trim(),
                        estado = estado,
                        foto = foto?.toString() ?: "",
                        telefono = telefonoLimpio
                    )

                    val reporte = Reporte(
                        id = System.currentTimeMillis().toInt(),
                        mascota = mascota,
                        tipo = estado,
                        descripcion = descripcion.trim(),
                        fechaPublicacion = ""
                    )

                    onPublicar(reporte)
                } else {
                    mensaje = "Completa los campos y verifica el teléfono."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Morado)
        ) {
            Text("Publicar reporte")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje, color = Morado)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al inicio")
        }
    }
}

@Composable
fun TarjetaMascota(
    mascota: Mascota,
    estadoReporte: String
) {
    val contexto = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Rosa)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (mascota.foto.isNotBlank()) {
                AsyncImage(
                    model = Uri.parse(mascota.foto),
                    contentDescription = "Fotografía de ${mascota.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐾 Sin fotografía", color = Morado)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mascota.nombre,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Morado
            )

            Text("📍 ${mascota.ubicacion}")
            Text("Estado: $estadoReporte")
            Text(mascota.descripcion)

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val numero = mascota.telefono.filter { it.isDigit() }
                    val texto = if (estadoReporte == "Perdido") {
                        "Hola, vi el reporte de ${mascota.nombre} en PetCare y quiero ayudar."
                    } else {
                        "Hola, vi tu reporte de una mascota encontrada en PetCare. Quisiera más información."
                    }

                    val enlace = Uri.parse(
                        "https://wa.me/$numero?text=${Uri.encode(texto)}"
                    )

                    contexto.startActivity(Intent(Intent.ACTION_VIEW, enlace))
                },
                enabled = mascota.telefono.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Morado)
            ) {
                Text(
                    if (estadoReporte == "Perdido") {
                        "Contactar por WhatsApp"
                    } else {
                        "Reportar avistamiento"
                    }
                )
            }
        }
    }
}