package com.example.petcare.model

data class Reporte(
    val id: Int,
    val mascota: Mascota,
    val tipo: String, // "Perdido" o "Encontrado"
    val descripcion: String,
    val fechaPublicacion: String
)