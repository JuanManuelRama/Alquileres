package com.example.myapplication.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "Tenant")
data class TenantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val phone: String
)