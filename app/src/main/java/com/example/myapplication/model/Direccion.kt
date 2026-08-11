package com.example.myapplication.model

data class Direccion (
    val id: Int,
    val calle: String,
    val altura: String,
    val piso: String?,
    val departamento: String?,
) {
    override fun toString(): String {
        return listOfNotNull(calle, altura, piso, departamento).joinToString(" ")
    }
}
