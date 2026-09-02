package com.example.myapplication.navigation

import com.example.myapplication.model.Contract

sealed interface Route
data object ContractList : Route
data class ContractDetail(val contract: Contract) : Route
data class ContractPayments(val contract: Contract) : Route

data object AddPropiedad : Route

data class CreatePaymentScreen(val contract: Contract) : Route
