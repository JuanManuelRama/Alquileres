package com.example.myapplication.data.entity

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
        parentColumns = ["tenantId"],
        entityColumns = ["id"]
    )
    val tenant: TenantEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            value = ContractServiceCrossRef::class,
            parentColumns = ["contractId"],
            entityColumns = ["serviceId"]
        )
    )
    val services: List<ServiceEntity>
) {
    fun toDomain(): Contract = Contract(
        property = property.toDomain(),
        tenant = tenant.toDomain(),
        initialRent = contract.initialRent,
        currentRent = contract.currentRent,
        startingDate = contract.startingDate,
        finishingDate = contract.finishingDate,
        adjustmentTime = contract.adjustmentTime,
        services = services.map { it.toDomain() }
    )
}