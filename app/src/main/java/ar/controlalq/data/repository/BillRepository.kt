package ar.controlalq.data.repository

import ar.controlalq.data.dao.BillDao
import ar.controlalq.data.entity.itemEntities
import ar.controlalq.data.entity.toEntity
import ar.controlalq.model.Bill
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

    override suspend fun markPaid(bill: Bill, date: LocalDate): Bill {
        require(!bill.isPaid) { "Bill is already paid" }
        require(!date.isAfter(LocalDate.now())) { "Payment date cannot be in the future" }
        require(bill.isValid) { "Complete all amounts and descriptions before confirming payment" }
        return dao.markPaid(bill.copy(updatedAt = System.currentTimeMillis()), date).toDomain()
    }

    override suspend fun save(bill: Bill): Bill {
        require(!bill.isPaid) { "Paid bills cannot be edited" }
        val saved = bill.copy(updatedAt = System.currentTimeMillis())
        dao.save(saved.toEntity(), saved.itemEntities())
        return checkNotNull(dao.find(saved.contract.id, saved.month)).toDomain(bill.contract)
    }
}
