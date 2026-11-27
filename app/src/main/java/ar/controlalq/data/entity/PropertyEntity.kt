package ar.controlalq.data.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import ar.controlalq.model.Address
import ar.controlalq.model.Property

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
    val id: Long = 0L,
    @Embedded(prefix = "addr_")
    val address: Address,
    val ownerId: Long,
    @ColumnInfo(defaultValue = "1") val isActive: Boolean = true

) {
    fun toDomain(owner: OwnerEntity): Property = Property(
        address = address,
        owner = owner.toDomain(),
        id = id,
    )
}

fun Property.toEntity(): PropertyEntity {
    return PropertyEntity(
        address = address,
        ownerId = owner.id,
        id = id,
    )
}
