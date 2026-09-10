package com.example.myapplication.data.entity

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.myapplication.model.Adjustment
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Tenant
import java.math.BigDecimal
import java.time.YearMonth

@Entity(
    tableName = "Contract",
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"]
        )
    ],
    indices = [Index("propertyId")]
)
data class ContractEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val propertyId: Long,
    @Embedded(prefix = "tenant_") val tenant: Tenant,
    val rent: BigDecimal,
    val startingMonth: YearMonth,
    val finishingMonth: YearMonth,
)

fun Contract.toEntity(): ContractEntity = ContractEntity(
    id = id,
    propertyId = property.id,
    tenant = tenant,
    rent = rent,
    startingMonth = startingMonth,
    finishingMonth = finishingMonth,
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
    val time: YearMonth,
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
