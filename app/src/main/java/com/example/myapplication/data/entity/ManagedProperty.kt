package com.example.myapplication.data.entity

import androidx.room3.Embedded

data class ManagedProperty(
    @Embedded val details: PropertyWithOwner,
    val contractCount: Int,
    val activeContractCount: Int,
)
