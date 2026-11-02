package com.example.petcare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.petcare.model.Mascota
import com.example.petcare.model.Reporte

private val Morado = Color(0xFF813D91)
private val Fondo = Color(0xFFFFFBF5)
private val Rosa = Color(0xFFF3E4F4)

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

    var nombreUsuario by remember { mutableStateOf("") }
    var correoUsuario by remember { mutableStateOf("") }
    var telefonoUsuario by remember { mutableStateOf("") }
    var perfilGuardado by remember { mutableStateOf(false) }

    val reportes = remember { mutableStateListOf<Reporte>() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Fondo
    ) {
        when (pantalla) {
            "inicio" -> PantallaInicio(
                onBuscar = { pantalla = "buscar" },
                onReportar = { pantalla = "reportar" },
                onPerfil = { pantalla = "perfil" }
            )

            "buscar" -> PantallaBuscar(
                reportes = reportes,
                onRegresar = { pantalla = "inicio" }
            )

            "reportar" -> PantallaReportar(
                onPublicar = { reporte ->
                    reportes.add(reporte)
                    pantalla = "buscar"
                },
                onRegresar = { pantalla = "inicio" }
            )

            "perfil" -> PantallaPerfil(
                nombre = nombreUsuario,
                correo = correoUsuario,
                telefono = telefonoUsuario,
                guardado = perfilGuardado,
                onNombreChange = {
                    nombreUsuario = it
                    perfilGuardado = false
                },
                onCorreoChange = {
                    correoUsuario = it
                    perfilGuardado = false
                },
                onTelefonoChange = {
                    telefonoUsuario = it
                    perfilGuardado = false
                },
                onGuardar = { perfilGuardado = true },
                onRegresar = { pantalla = "inicio" }
            )
        }
    }
}

@Composable
fun PantallaInicio(
    onBuscar: () -> Unit,
    onReportar: () -> Unit,
    onPerfil: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🐾 PetCare",
            color = Morado,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text("Ayudemos a las mascotas a volver a casa.")

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onBuscar, modifier = Modifier.fillMaxWidth()) {
            Text("Buscar mascotas")
        }

        Button(onClick = onReportar, modifier = Modifier.fillMaxWidth()) {
            Text("Reportar mascota")
        }

        OutlinedButton(onClick = onPerfil, modifier = Modifier.fillMaxWidth()) {
            Text("Mi perfil")
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
                    mascota.raza.contains(busqueda, ignoreCase = true) ||
                    mascota.especie.contains(busqueda, ignoreCase = true)

        val coincideFiltro = filtro == "Todos" || reporte.tipo == filtro
        coincideTexto && coincideFiltro
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Buscar mascotas",
            color = Morado,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Nombre, raza o ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Perdido", "Encontrado").forEach { opcion ->
                if (filtro == opcion) {
                    Button(onClick = { filtro = opcion }) {
                        Text(opcion)
                    }
                } else {
                    OutlinedButton(onClick = { filtro = opcion }) {
                        Text(opcion)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resultados.isEmpty()) {
            Text("No hay reportes que coincidan con la búsqueda.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resultados) { reporte ->
                    TarjetaReporte(reporte)
                }
            }
        }

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al inicio")
        }
    }
}

@Composable
fun TarjetaReporte(reporte: Reporte) {
    val mascota = reporte.mascota
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Rosa)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (mascota.foto.isNotBlank()) {
                AsyncImage(
                    model = mascota.foto,
                    contentDescription = "Fotografía de ${mascota.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            } else {
                Text("🐾 Sin fotografía")
            }

            Text(
                text = mascota.nombre,
                color = Morado,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text("Estado: ${reporte.tipo}")
            Text("Especie: ${mascota.especie}")
            Text("Raza: ${mascota.raza}")
            Text("Edad: ${mascota.edad}")
            Text("Tamaño: ${mascota.tamano}")
            Text("Ubicación: ${mascota.ubicacion}")
            Text("Características: ${mascota.caracteristicas}")
            Text("Descripción: ${reporte.descripcion}")
            Text("Fecha: ${reporte.fechaPublicacion}")

            if (mascota.telefono.isNotBlank()) {
                Button(
                    onClick = {
                        val numero = mascota.telefono.filter { it.isDigit() }
                        val enlace = Uri.parse("https://wa.me/$numero")
                        val intent = Intent(Intent.ACTION_VIEW, enlace)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Contactar por WhatsApp")
                }
            } else {
                Text("No se proporcionó teléfono de contacto.")
            }
        }
    }
}

@Composable
fun PantallaReportar(
    onPublicar: (Reporte) -> Unit,
    onRegresar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var tamano by remember { mutableStateOf("") }
    var caracteristicas by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Perdido") }
    var descripcion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fotoSeleccionada by remember { mutableStateOf<Uri?>(null) }

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fotoSeleccionada = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Reportar mascota",
            color = Morado,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (estado == "Perdido") {
                        Button(onClick = { estado = "Perdido" }) {
                            Text("Perdido")
                        }
                    } else {
                        OutlinedButton(onClick = { estado = "Perdido" }) {
                            Text("Perdido")
                        }
                    }

                    if (estado == "Encontrado") {
                        Button(onClick = { estado = "Encontrado" }) {
                            Text("Encontrado")
                        }
                    } else {
                        OutlinedButton(onClick = { estado = "Encontrado" }) {
                            Text("Encontrado")
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { selectorFoto.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fotoSeleccionada == null) "Seleccionar fotografía" else "Cambiar fotografía")
                }
            }

            item {
                if (fotoSeleccionada != null) {
                    AsyncImage(
                        model = fotoSeleccionada,
                        contentDescription = "Fotografía seleccionada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            item { CampoTexto("Nombre de la mascota", nombre) { nombre = it } }
            item { CampoTexto("Especie", especie) { especie = it } }
            item { CampoTexto("Raza", raza) { raza = it } }
            item { CampoTexto("Edad", edad) { edad = it } }
            item { CampoTexto("Tamaño", tamano) { tamano = it } }
            item { CampoTexto("Características", caracteristicas) { caracteristicas = it } }
            item { CampoTexto("Ubicación", ubicacion) { ubicacion = it } }
            item { CampoTexto("Descripción", descripcion) { descripcion = it } }
            item { CampoTexto("Teléfono de contacto", telefono) { telefono = it } }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val mascota = Mascota(
                    nombre = nombre,
                    especie = especie,
                    raza = raza,
                    edad = edad,
                    tamano = tamano,
                    caracteristicas = caracteristicas,
                    ubicacion = ubicacion,
                    fecha = "Sin fecha",
                    estado = estado,
                    foto = fotoSeleccionada?.toString() ?: "",
                    telefono = telefono
                )

                val reporte = Reporte(
                    id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                    mascota = mascota,
                    tipo = estado,
                    descripcion = descripcion,
                    fechaPublicacion = "Hoy"
                )

                onPublicar(reporte)
            },
            enabled = nombre.isNotBlank() &&
                    especie.isNotBlank() &&
                    ubicacion.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publicar reporte")
        }

        TextButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al inicio")
        }
    }
}

@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun PantallaPerfil(
    nombre: String,
    correo: String,
    telefono: String,
    guardado: Boolean,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mi perfil",
            color = Morado,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Text("Información de la persona usuaria")

        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = onCorreoChange,
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = onTelefonoChange,
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar datos")
        }

        if (guardado) {
            Text("Datos del perfil guardados.")
        }

        OutlinedButton(
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Regresar al inicio")
        }
    }
}