package com.example.myapplication.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.myapplication.model.Owner

@Entity(tableName = "Owner")
data class OwnerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String
) {
    fun toDomain(): Owner = Owner(
        name = name,
        id = id
    )
}

fun Owner.toEntity(): OwnerEntity {
    return OwnerEntity(
        name = name
    )
}