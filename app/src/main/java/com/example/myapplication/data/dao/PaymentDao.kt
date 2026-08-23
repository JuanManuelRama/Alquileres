package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.example.myapplication.data.entity.PaymentEntity
import com.example.myapplication.data.entity.PaymentWithServices
import com.example.myapplication.data.entity.ServicePaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Transaction
    @Query("SELECT * FROM Payment WHERE contractId = :contractId")
    fun getPaymentsForContract(contractId: Long): Flow<List<PaymentWithServices>>

    @Insert
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert
    suspend fun insertServicePayments(servicePayments: List<ServicePaymentEntity>)

    @Transaction
    suspend fun savePayment(payment: PaymentEntity, servicePayments: List<ServicePaymentEntity>) {
        val paymentId = insertPayment(payment)
        val withId = servicePayments.map { it.copy(paymentId = paymentId) }
        insertServicePayments(withId)
    }
}
