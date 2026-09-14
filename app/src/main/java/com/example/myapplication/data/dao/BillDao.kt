package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.example.myapplication.data.entity.BillEntity
import com.example.myapplication.data.entity.BillItemEntity
import com.example.myapplication.data.entity.BillWithItems
import com.example.myapplication.data.entity.itemEntities
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Bill
import com.example.myapplication.model.Contract
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Dao
interface BillDao {
    @Query("SELECT billingMonth FROM Bill WHERE contractId = :contractId AND paidAt IS NOT NULL")
    fun observePaidMonths(contractId: Long): Flow<List<YearMonth>>

    @Transaction
    @Query("SELECT * FROM Bill WHERE contractId = :contractId ORDER BY billingMonth")
    suspend fun list(contractId: Long): List<BillWithItems>

    @Transaction
    suspend fun createAll(bills: List<Bill>) {
        bills.forEach { bill ->
            val id = insertBill(bill.toEntity())
            insertItems(bill.itemEntities().map { it.copy(billId = id) })
        }
    }

    @Transaction
    suspend fun createContract(contract: Contract, contracts: ContractDao): Long {
        val id = contracts.saveContract(contract.toEntity(), contract.services.map { it.id },
            contract.adjustments.map { it.toEntity() })
        createAll(contract.copy(id = id).createBills())
        return id
    }

    @Transaction
    @Query("SELECT * FROM Bill WHERE contractId = :contractId AND billingMonth = :month")
    suspend fun find(contractId: Long, month: YearMonth): BillWithItems?

    @Insert
    suspend fun insertBill(bill: BillEntity): Long

    @Insert
    suspend fun insertItems(items: List<BillItemEntity>)

    @Query("UPDATE Bill SET updatedAt = :updatedAt WHERE id = :id AND paidAt IS NULL AND EXISTS (SELECT 1 FROM Contract c WHERE c.id = Bill.contractId AND c.isFinished = 0)")
    suspend fun updateUnpaidBill(id: Long, updatedAt: Long): Int

    @Query("UPDATE Bill SET rent = :rent, paidAt = :date WHERE id = :id AND paidAt IS NULL AND EXISTS (SELECT 1 FROM Contract c WHERE c.id = Bill.contractId AND c.isFinished = 0)")
    suspend fun setPaidDate(id: Long, date: LocalDate, rent: BigDecimal): Int

    @Transaction
    suspend fun markPaid(bill: Bill, date: LocalDate): BillWithItems {
        require(!bill.isPaid) { "Complete all amounts before confirming payment" }
        require(!date.isAfter(LocalDate.now())) { "Payment date cannot be in the future" }
        save(bill.toEntity(), bill.itemEntities())
        val current = checkNotNull(find(bill.contract.id, bill.month)).toDomain(bill.contract)
        require(current.isValid) { "Complete all amounts before confirming payment" }
        check(setPaidDate(bill.id, date, current.rent) == 1) { "Bill is already paid" }
        return checkNotNull(find(bill.contract.id, bill.month))
    }

    @Query("DELETE FROM BillItem WHERE billId = :id")
    suspend fun deleteItems(id: Long)

    @Transaction
    suspend fun save(bill: BillEntity, items: List<BillItemEntity>) {
        require(bill.paidAt == null) { "Paid bills cannot be edited" }
        val stored = checkNotNull(find(bill.contractId, bill.billingMonth)) { "Bill no longer exists" }
        check(stored.bill.id == bill.id) { "Bill identity does not match" }
        check(updateUnpaidBill(bill.id, bill.updatedAt) == 1) { "Paid bills cannot be edited" }
        deleteItems(bill.id)
        insertItems(items)
    }
}
