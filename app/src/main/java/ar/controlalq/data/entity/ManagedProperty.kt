package ar.controlalq.data.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded

data class ManagedProperty(
    @Embedded val details: PropertyWithOwner,
    @ColumnInfo(name = "propertyActive") val isActive: Boolean,
    val hasActiveContracts: Boolean,
)
