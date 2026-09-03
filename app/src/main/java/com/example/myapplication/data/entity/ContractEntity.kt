package com.example.myapplication.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.myapplication.model.Adjustment
import com.example.myapplication.model.Contract
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
    val id: Long = 0L,
    val propertyId: Long,
    val tenantId: Long,
    val rent: BigDecimal,
    val startingDate: LocalDate,
    val finishingDate: LocalDate,
)

fun Contract.toEntity(): ContractEntity = ContractEntity(
    id = id,
    propertyId = property.id,
    tenantId = tenant.id,
    rent = rent,
    startingDate = startingDate,
    finishingDate = finishingDate,
)

@Entity(
    tableName = "ContractServiceCrossRef",
    primaryKeys = ["contractId", "serviceId"],
    foreignKeys = [
        ForeignKey(
            entity = ContractEntity::class,
            parentColumns = ["id"],
            childColumns = ["contractId"]
        ),
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"]
        )
    ],
    indices = [Index("serviceId")]
)
data class ContractServiceCrossRef(
    val contractId: Long,
    val serviceId: Long
)

@Entity(
    tableName = "Adjustment",
    foreignKeys = [
        ForeignKey(
            entity = ContractEntity::class,
            parentColumns = ["id"],
            childColumns = ["contractId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contractId")]
)
data class AdjustmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val contractId: Long,
    val time: LocalDate,
    val amount: BigDecimal?
)

fun Adjustment.toEntity(): AdjustmentEntity = AdjustmentEntity(
    contractId = 0L,
    time = time,
    amount = amount
)

fun AdjustmentEntity.toDomain(): Adjustment = Adjustment(
    time = time,
    amount = amount
)
