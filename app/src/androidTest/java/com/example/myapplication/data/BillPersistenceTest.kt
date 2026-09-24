package com.example.myapplication.data

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.ContractServiceCrossRef
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.ServiceEntity
import com.example.myapplication.data.entity.itemEntities
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.data.repository.BillRepository
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.model.Address
import com.example.myapplication.model.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class BillPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val month = YearMonth.of(2026, 12)

    @Test fun unpaidRentRecalculatesWhileServiceInputsSurviveReopening() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setQueryCoroutineContext(Dispatchers.IO.limitedParallelism(1))
            .build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val service = db.serviceDao().insert(ServiceEntity(name = "Water"))
            val id = db.contractDao().saveContract(
                contract(property),
                listOf(service),
                listOf(AdjustmentEntity(contractId = 0, time = month, amount = BigDecimal("0.10")))
            )
            db.billDao().createAll(
                db.contractDao().observeById(id).first()!!.toDomain().createBills().associate { it.toEntity() to it.itemEntities() }
            )
            val repository = BillRepository(db.billDao())
            val first = repository.open(id, month)
            assertEquals(24, db.billDao().list(id).size)
            val originalIds = repository.list(id).map { it.id }
            assertEquals(originalIds, repository.list(id).map { it.id })
            val contracts = ContractRepository(db)
            val created = contracts.insert(contracts.observeById(id).first()!!.copy(id = 0))
            assertEquals(24, db.billDao().list(created.id).size)
            assertEquals(0, BigDecimal("1100").compareTo(first.rent))
            assertEquals("Water", first.items.single().description)
            assertFalse(first.isValid)
            try {
                repository.markPaid(first, LocalDate.now())
                fail("Incomplete bill accepted payment")
            } catch (_: IllegalArgumentException) { }
            val complete = first.copy(items = first.items.map { it.copy(amount = BigDecimal.ONE) })
            try {
                repository.markPaid(complete, LocalDate.now().plusDays(1))
                fail("Future payment accepted")
            } catch (_: IllegalArgumentException) { }
            assertEquals(first, repository.open(id, month))
            repository.save(first.copy(items = first.items.map { it.copy(amount = null) }))
            db.contractDao().updateAdjustmentAmount(id, month, BigDecimal("0.50"))
            val extraService = db.serviceDao().insert(ServiceEntity(name = "Power"))
            db.contractDao().insertContractService(listOf(ContractServiceCrossRef(id, extraService)))

            val reopened = repository.open(id, month)
            assertEquals(first.id, reopened.id)
            assertEquals(0, BigDecimal("1500").compareTo(reopened.rent))
            assertNull(db.billDao().find(id, month)!!.bill.rent)
            assertNull(reopened.items.single().amount)
            assertTrue(db.billDao().observePaidMonths(id).first().isEmpty())
            assertFalse(db.contractDao().observeWithStatus(month).first().first { it.details.contract.id == id }.paymentDue)
            assertTrue(db.contractDao().observeWithStatus(month).first().first { it.details.contract.id == id }.billIncomplete)

            val next = repository.open(id, month.plusMonths(1))
            assertNotEquals(first.id, next.id)
            assertEquals(0, BigDecimal("1500").compareTo(next.rent))
            assertEquals(1, next.items.size)
            val otherId = contracts.insert(contracts.observeById(id).first()!!.copy(id = 0)).id
            assertEquals(otherId, repository.open(otherId, month).contract.id)
        } finally { db.close() }
    }

    @Test fun failedChildWriteRollsBackRentAndAllServiceChanges() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setQueryCoroutineContext(Dispatchers.IO.limitedParallelism(1))
            .build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val service = db.serviceDao().insert(ServiceEntity(name = "Water"))
            val id = db.contractDao().saveContract(contract(property), listOf(service), emptyList())
            db.billDao().createAll(
                db.contractDao().observeById(id).first()!!.toDomain().createBills().associate { it.toEntity() to it.itemEntities() }
            )
            val repository = BillRepository(db.billDao())
            val bill = repository.open(id, month)
            var failed = false
            try {
                db.billDao().save(bill.copy(rent = 9999.toBigDecimal()).toEntity(), bill.itemEntities() + bill.itemEntities())
            } catch (_: Exception) { failed = true }
            assertTrue(failed)
            assertEquals(bill, repository.open(id, month))
        } finally { db.close() }
    }

    @Test fun paidBillsAreFrozenAndStatusUsesBillMonth() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setQueryCoroutineContext(Dispatchers.IO.limitedParallelism(1)).build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val id = db.contractDao().saveContract(contract(property), emptyList(), emptyList())
            db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills().associate { it.toEntity() to it.itemEntities() })
            val repo = BillRepository(db.billDao())
            val billingMonth = YearMonth.of(2026, 1)
            val original = repo.open(id, billingMonth)
            val paymentDate = LocalDate.of(2026, 2, 5)
            val paid = repo.markPaid(original, paymentDate)
            assertEquals(paymentDate, repo.open(id, billingMonth).paidAt)
            assertFalse(db.contractDao().observeWithStatus(billingMonth)
                .first().single().paymentDue)
            assertTrue(db.contractDao().observeWithStatus(billingMonth.plusMonths(1))
                .first().single().paymentDue)
            assertEquals(listOf(billingMonth), db.billDao().observePaidMonths(id).first())
            try { repo.save(original.copy(rent = 999.toBigDecimal())); fail("Paid bill accepted a stale edit") }
            catch (_: IllegalStateException) { }
            try { repo.markPaid(original, paymentDate); fail("Duplicate payment accepted") }
            catch (_: IllegalStateException) { }
            assertEquals(paid, repo.open(id, billingMonth))
            assertEquals(1, db.billDao().observePaidMonths(id).first().size)

        } finally { db.close() }
    }

    @Test fun itemsAllowRepeatedDescriptionsAndPreserveOrderWithoutServiceReferences() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setQueryCoroutineContext(Dispatchers.IO.limitedParallelism(1))
            .build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val id = db.contractDao().saveContract(contract(property), emptyList(), emptyList())
            db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills().associate { it.toEntity() to it.itemEntities() })
            val repo = BillRepository(db.billDao())
            val original = repo.open(id, YearMonth.of(2026, 1))
            val items = listOf(
                com.example.myapplication.model.BillItem("Special charge", 10.toBigDecimal(), isExtra = true),
                com.example.myapplication.model.BillItem("Special charge", 20.toBigDecimal()),
                com.example.myapplication.model.BillItem("Adjustment", (-5).toBigDecimal(), isExtra = true)
            )
            repo.save(original.copy(items = items))
            assertEquals(items, repo.open(id, original.month).items)
            repo.save(original.copy(items = items.reversed()))
            assertEquals(items.reversed(), repo.open(id, original.month).items)
            val paid = repo.markPaid(repo.open(id, original.month), LocalDate.of(2026, 2, 1))
            assertTrue(paid.isPaid)
            assertEquals(items.reversed(), repo.open(id, original.month).items)
        } finally { db.close() }
    }

    @Test fun paymentRecalculatesRentAndFreezesOnlyItsMonthsAdjustment() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setQueryCoroutineContext(Dispatchers.IO.limitedParallelism(1)).build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val january = YearMonth.of(2026, 1)
            val july = YearMonth.of(2026, 7)
            val id = db.contractDao().saveContract(contract(property), emptyList(), listOf(
                AdjustmentEntity(0, january, null), AdjustmentEntity(0, january.plusMonths(1), null), AdjustmentEntity(0, july, null)
            ))
            db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills().associate { it.toEntity() to it.itemEntities() })
            val repo = BillRepository(db.billDao())
            val stale = repo.open(id, january.plusMonths(1))
            assertNull(db.billDao().find(id, stale.month)!!.bill.rent)
            db.contractDao().updateAdjustmentAmount(id, january, BigDecimal("0.20"))
            // Payment must use database adjustments, not the stale display or caller-supplied rent.
            val paid = repo.markPaid(stale.copy(rent = 9999.toBigDecimal()), LocalDate.of(2026, 2, 5))
            assertEquals(0, BigDecimal("1200").compareTo(paid.rent))
            assertEquals(0, BigDecimal("1200").compareTo(db.billDao().find(id, stale.month)!!.bill.rent))
            assertEquals(1, db.contractDao().updateAdjustmentAmount(id, january, BigDecimal("0.50")))
            try {
                ContractRepository(db)
                    .updateAdjustmentAmount(id, stale.month, BigDecimal("0.50"))
                fail("Locked adjustment reported success")
            } catch (_: IllegalStateException) { }
            assertEquals(BigDecimal("0.50"), db.contractDao().observeById(id).first()!!.adjustments.single { it.time == january }.amount)
            db.contractDao().updateAdjustmentAmount(id, july, BigDecimal("0.10"))
            assertEquals(0, BigDecimal("1650").compareTo(repo.open(id, july).rent))
            val reopenedPaid = repo.open(id, paid.month)
            // Contract adjustments stay live; the paid bill's own values remain frozen.
            assertEquals(paid.copy(contract = reopenedPaid.contract), reopenedPaid)
            assertNull(db.billDao().find(id, july)!!.bill.rent)
            assertEquals(0, BigDecimal("1650").compareTo(repo.open(id, july).rent))
        } finally { db.close() }
    }

    private fun contract(propertyId: Long) = ContractEntity(
        propertyId = propertyId, tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
        startingMonth = YearMonth.of(2026, 1), finishingMonth = YearMonth.of(2027, 12)
    )
}
