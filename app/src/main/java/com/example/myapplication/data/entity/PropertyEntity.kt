package com.example.myapplication.data.entity

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.myapplication.model.Address
import com.example.myapplication.model.Property

@Entity(tableName = "Property",
    foreignKeys = [
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
        )
    ],
    indices = [Index("ownerId")]
)
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @Embedded(prefix = "addr_")
    val address: Address,
    val ownerId: Long

) {
    fun toDomain(owner: OwnerEntity): Property = Property(
        address = address,
        owner = owner.toDomain()
    )
}

fun Property.toEntity(ownerId: Long): PropertyEntity {
    return PropertyEntity(
        address = address,
        ownerId = ownerId,
        id = id
    )
}