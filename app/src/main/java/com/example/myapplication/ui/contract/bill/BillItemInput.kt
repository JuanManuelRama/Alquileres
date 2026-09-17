package com.example.myapplication.ui.contract.bill

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BillItemInput(
    val description: String,
    val amount: String,
    val isExtra: Boolean,
    val key: String = UUID.randomUUID().toString()
)
