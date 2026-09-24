package com.example.myapplication.data.entity

import androidx.room3.Embedded

data class ContractWithStatus(
    @Embedded val details: BasicContract,
    val paymentDue: Boolean,
    val hikeThisMonth: Boolean,
    val billIncomplete: Boolean = false
)
