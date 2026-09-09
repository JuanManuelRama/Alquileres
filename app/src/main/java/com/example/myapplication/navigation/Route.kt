package com.example.myapplication.navigation

import com.example.myapplication.model.Contract

sealed interface Route
data object ContractList : Route
data class ContractDetail(val contractId: Long) : Route
data class ContractPayments(val contract: Contract) : Route

data object AddPropiedad : Route

data class PrepareBill(val contractId: Long) : Route
