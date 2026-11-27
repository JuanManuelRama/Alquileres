package ar.controlalq.data.entity

import androidx.room3.Embedded
import androidx.room3.Relation
import ar.controlalq.model.Property

data class PropertyWithOwner(
    @Embedded
    val property: PropertyEntity,

    @Relation(
        parentColumns = ["ownerId"],
        entityColumns = ["id"]
    )
    val owner: OwnerEntity
) {
    fun toDomain(): Property = property.toDomain(owner)
}
