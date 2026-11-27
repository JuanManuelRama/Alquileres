package ar.controlalq.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import ar.controlalq.model.Owner

@Entity(tableName = "Owner")
data class OwnerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String
) {
    fun toDomain(): Owner = Owner(
        name = name,
        id = id
    )
}

fun Owner.toEntity(): OwnerEntity {
    return OwnerEntity(
        id = id,
        name = name
    )
}
