package com.example.myapplication.data.entity

import androidx.room3.Embedded
import com.example.myapplication.model.Contract

/** Listing projection. Load the full contract by ID before editing or creating payments. */
data class BasicContract(
    @Embedded val contract: ContractEntity,
    @Embedded(prefix = "property_") val property: PropertyEntity,
    @Embedded(prefix = "owner_") val owner: OwnerEntity
) {
    fun toDomain(): Contract = Contract(
        id = contract.id,
        property = property.toDomain(owner),
        tenant = contract.tenant,
        rent = contract.rent,
        startingMonth = contract.startingMonth,
        finishingMonth = contract.finishingMonth,
        services = emptyList(),
        adjustments = emptyList()
    )
}
