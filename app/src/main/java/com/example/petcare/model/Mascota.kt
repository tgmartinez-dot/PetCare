package com.example.petcare.model

data class Mascota(
    val nombre: String,
    val ubicacion: String,
    val descripcion: String,
    val estado: String,
    val foto: String = "",
    val telefono: String = ""
)