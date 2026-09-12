package com.example.myapplication.data.entity

import androidx.room3.Ignore
import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import com.example.myapplication.model.Contract

data class ContractWithRelations(
    @Embedded val contract: ContractEntity,
    @Relation(
        entity = PropertyEntity::class,
        parentColumns = ["propertyId"],
        entityColumns = ["id"]
    )
    val property: PropertyWithOwner,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            value = ContractServiceCrossRef::class,
            parentColumns = ["contractId"],
            entityColumns = ["serviceId"]
        )
    )
    val services: List<ServiceEntity>,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["contractId"]
    )
    val adjustments: List<AdjustmentEntity>
) {
    @delegate:Ignore
    private val domain: Contract by lazy {
        Contract(
            property = property.toDomain(),
            tenant = contract.tenant,
            rent = contract.rent,
            startingMonth = contract.startingMonth,
            finishingMonth = contract.finishingMonth,
            adjustments = adjustments.map { it.toDomain() },
            services = services.map { it.toDomain() },
            id = contract.id
        )
    }

    fun toDomain(): Contract = domain
}
