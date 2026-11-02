package com.example.petcare.model

data class Mascota(
    val nombre: String,
    val especie: String,
    val raza: String,
    val edad: String,
    val tamano: String,
    val caracteristicas: String,
    val ubicacion: String,
    val fecha: String,
    val estado: String,
    val foto: String = "",
    val telefono: String = ""
)