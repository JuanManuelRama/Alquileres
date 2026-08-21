package com.example.myapplication.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "Contract",
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"]
        ),
        ForeignKey(
            entity = TenantEntity::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"]
        )
    ],
    indices = [Index("propertyId"), Index("tenantId")]
)
data class ContractEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val propertyId: Long,
    val tenantId: Long,
    val initialRent: BigDecimal,
    val currentRent: BigDecimal,
    val startingDate: LocalDate,
    val finishingDate: LocalDate,
    val adjustmentTime: Long
)

@Entity(
    tableName = "ContractServiceCrossRef",
    primaryKeys = ["contractId", "serviceId"],
    indices = [Index("serviceId")]
)
data class ContractServiceCrossRef(
    val contractId: Long,
    val serviceId: Long
)