package com.example.myapplication.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.myapplication.model.Tenant

@Entity(tableName = "Tenant")
data class TenantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val phone: String
) {
    fun toDomain(): Tenant {
        return Tenant(
            name = name,
            phone = phone,
            id = id
        )
    }
}

fun Tenant.toEntity(): TenantEntity {
    return TenantEntity(
        id = id,
        name = name,
        phone = phone
    )
}
