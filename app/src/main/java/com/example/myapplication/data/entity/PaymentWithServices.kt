package com.example.myapplication.data.entity

import androidx.room3.Embedded
import androidx.room3.Relation
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Payment
import com.example.myapplication.model.ServicePayment

data class PaymentWithServices(
    @Embedded val payment: PaymentEntity,
    @Relation(
        entity = ServicePaymentEntity::class,
        parentColumns = ["id"],
        entityColumns = ["paymentId"]
    )
    val servicePayments: List<ServicePaymentWithService>
) {
    fun toDomain(contract: Contract): Payment = Payment(
        contract = contract,
        rent = payment.rent,
        services = servicePayments.map { it.toDomain() },
        id = payment.id
    )
}

data class ServicePaymentWithService(
    @Embedded val servicePayment: ServicePaymentEntity,
    @Relation(
        parentColumns = ["serviceId"],
        entityColumns = ["id"]
    )
    val service: ServiceEntity
) {
    fun toDomain(): ServicePayment = ServicePayment(
        service = service.toDomain(),
        amount = servicePayment.amount,
        id = servicePayment.id
    )
}
