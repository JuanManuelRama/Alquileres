package com.example.myapplication.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.myapplication.model.Payment
import com.example.myapplication.model.ServicePayment
import java.math.BigDecimal
import java.time.LocalDate

@Entity(
    tableName = "Payment",
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
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val contractId: Long,
    val rent: BigDecimal,
    val date: LocalDate
)

fun Payment.toEntity(): PaymentEntity = PaymentEntity(
    id = id,
    contractId = contract.id!!,
    rent = rent,
    date = date
)

@Entity(
    tableName = "ServicePayment",
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"]
        )
    ],
    indices = [Index("paymentId"), Index("serviceId")]
)
data class ServicePaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val paymentId: Long,
    val serviceId: Long,
    val amount: BigDecimal
)

fun ServicePayment.toEntity(paymentId: Long): ServicePaymentEntity = ServicePaymentEntity(
    id = id,
    paymentId = paymentId,
    serviceId = service.id!!,
    amount = amount
)
