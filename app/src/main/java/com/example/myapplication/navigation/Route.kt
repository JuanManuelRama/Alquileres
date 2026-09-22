package com.example.myapplication.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey

@Serializable
data object PropertyList : Route

@Serializable
data object Settings : Route

@Serializable
data class ContractList(val isInactive: Boolean) : Route

@Serializable
data class ContractDetail(val contractId: Long, val isInactive: Boolean) : Route

@Serializable
data class RenewContract(val contractId: Long) : Route

@Serializable
data object AddPropiedad : Route

@Serializable
data class PrepareBill(val contractId: Long, val isInactive: Boolean = false) : Route
