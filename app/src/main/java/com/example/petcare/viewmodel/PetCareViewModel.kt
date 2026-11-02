package com.example.petcare.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.petcare.model.Mascota
import com.example.petcare.model.Reporte

class PetCareViewModel : ViewModel() {

    private val _reportes = mutableStateListOf<Reporte>()
    val reportes: List<Reporte> = _reportes

    fun agregarReporte(reporte: Reporte) {
        _reportes.add(reporte)
    }

    fun reportesPerdidos(): List<Reporte> {
        return _reportes.filter { it.tipo == "Perdido" }
    }

    fun reportesEncontrados(): List<Reporte> {
        return _reportes.filter { it.tipo == "Encontrado" }
    }
}