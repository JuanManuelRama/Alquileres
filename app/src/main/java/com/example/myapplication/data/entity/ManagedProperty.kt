package com.example.myapplication.data.entity

import androidx.room3.Embedded
import androidx.room3.ColumnInfo

data class ManagedProperty(
    @Embedded val details: PropertyWithOwner,
    @ColumnInfo(name = "propertyActive") val isActive: Boolean,
    val hasActiveContracts: Boolean,
)
