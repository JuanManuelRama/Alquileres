package com.example.myapplication.navigation

sealed interface Route
data object ListaPropiedades : Route
data class DetallePropiedad(val id: Int) : Route

data object AddPropiedad : Route