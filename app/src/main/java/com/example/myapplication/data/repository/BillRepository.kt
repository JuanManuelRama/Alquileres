package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.BillDao
import com.example.myapplication.data.entity.itemEntities
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Bill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

interface BillStore {
    fun observePaidMonths(contractId: Long): Flow<Set<YearMonth>>
    suspend fun list(contractId: Long): List<Bill>
    suspend fun open(contractId: Long, month: YearMonth): Bill =
        list(contractId).first { it.month == month }
    suspend fun markPaid(bill: Bill, date: LocalDate): Bill
    suspend fun save(bill: Bill): Bill
}

class BillRepository(
    private val dao: BillDao
) : BillStore {
    override fun observePaidMonths(contractId: Long): Flow<Set<YearMonth>> =
        dao.observePaidMonths(contractId).map { months -> months.toSet() }

    override suspend fun list(contractId: Long): List<Bill> =
        dao.list(contractId).map { it.toDomain() }

    override suspend fun markPaid(bill: Bill, date: LocalDate): Bill =
        dao.markPaid(bill.copy(updatedAt = System.currentTimeMillis()), date).toDomain()

    override suspend fun save(bill: Bill): Bill {
        val saved = bill.copy(updatedAt = System.currentTimeMillis())
        dao.save(saved.toEntity(), saved.itemEntities())
        return checkNotNull(dao.find(saved.contractId, saved.month)).toDomain()
    }
}
