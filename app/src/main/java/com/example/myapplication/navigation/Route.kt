package com.example.myapplication.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey

@Serializable
data object ContractList : Route

@Serializable
data class ContractDetail(val contractId: Long) : Route

@Serializable
data object AddPropiedad : Route

@Serializable
data class PrepareBill(val contractId: Long) : Route
