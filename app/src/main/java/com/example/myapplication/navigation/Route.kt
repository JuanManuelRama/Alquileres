package com.example.myapplication.navigation


sealed interface Route
data object ContractList : Route
data class ContractDetail(val contractId: Long) : Route

data object AddPropiedad : Route

data class PrepareBill(val contractId: Long) : Route
