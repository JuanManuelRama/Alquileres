package com.example.myapplication.navigation

import com.example.myapplication.model.Contract
import com.example.myapplication.model.Property

sealed interface Route
data object ContractList : Route
data object ListaPropiedades : Route
data class DetallePropiedad(val property: Property) : Route
data class ContractDetail(val contract: Contract) : Route
data class ContractPayments(val contract: Contract) : Route

data object AddPropiedad : Route

data class CreatePaymentScreen(val contract: Contract) : Route
