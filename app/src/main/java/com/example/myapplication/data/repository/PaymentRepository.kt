package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.PaymentDao
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentRepository(private val dao: PaymentDao) {
    fun getPaymentsForContract(contract: Contract): Flow<List<Payment>> {
        return dao.getPaymentsForContract(contract.id!!).map { list ->
            list.map { it.toDomain(contract) }
        }
    }

    suspend fun save(payment: Payment) {
        dao.savePayment(
            payment = payment.toEntity(),
            servicePayments = payment.services.map { it.toEntity(0) } // paymentId is handled in DAO
        )
    }
}
