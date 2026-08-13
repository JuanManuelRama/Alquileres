package com.example.myapplication.navigation

import com.example.myapplication.model.Property

sealed interface Route
data object ListaPropiedades : Route
data class DetallePropiedad(val property: Property) : Route

data object AddPropiedad : Route