package com.example.myapplication.data

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.entity.*
import com.example.myapplication.data.repository.BillRepository
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.model.Address
import com.example.myapplication.model.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class BillPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val month = YearMonth.of(2026, 12)

    @Test fun snapshotsSurviveReopeningAndRemainUnpaid() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setQueryCoroutineContext(Dispatchers.IO.limitedParallelism(1))
            .build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val service = db.serviceDao().insert(ServiceEntity(name = "Water"))
            val id = db.contractDao().saveContract(
                contract(property), listOf(service),
                listOf(AdjustmentEntity(contractId = 0, time = month.atDay(1), amount = BigDecimal("0.10")))
            )
            db.billDao().createAll(
                db.contractDao().observeById(id).first()!!.toDomain().createBills()
            )
            val repository = BillRepository(db.billDao())
            val first = repository.open(id, month)
            assertEquals(24, db.billDao().list(id).size)
            val originalIds = repository.list(id).map { it.id }
            assertEquals(originalIds, repository.list(id).map { it.id })
            val contracts = ContractRepository(db.contractDao(), db.billDao())
            val created = contracts.insert(contracts.observeById(id).first()!!.copy(id = 0))
            assertEquals(24, db.billDao().list(created.id).size)
            assertEquals(0, BigDecimal("1100").compareTo(first.rentInput.toBigDecimal()))
            assertEquals("Water", first.items.single().description)
            assertFalse(first.isValid)
            repository.save(first.copy(items = first.items.map { it.copy(amount = "-") }))
            db.contractDao().updateAdjustmentAmount(id, month.atDay(1), BigDecimal("0.50"))
            val extraService = db.serviceDao().insert(ServiceEntity(name = "Power"))
            db.contractDao().insertContractServiceCrossRefs(listOf(ContractServiceCrossRef(id, extraService)))

            val reopened = repository.open(id, month)
            assertEquals(first.id, reopened.id)
            assertEquals(first.rentInput, reopened.rentInput)
            assertEquals("-", reopened.items.single().amount)
            assertTrue(db.billDao().observePaidMonths(id).first().isEmpty())
            assertTrue(db.contractDao().observeWithStatus(month.atDay(1), month.plusMonths(1).atDay(1)).first().first { it.details.contract.id == id }.paymentDue)

            val next = repository.open(id, month.plusMonths(1))
            assertNotEquals(first.id, next.id)
            assertEquals(0, BigDecimal("1100").compareTo(next.rentInput.toBigDecimal()))
            assertEquals(1, next.items.size)
            val otherId = contracts.insert(contracts.observeById(id).first()!!.copy(id = 0)).id
            assertEquals(otherId, repository.open(otherId, month).contractId)
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
                db.contractDao().observeById(id).first()!!.toDomain().createBills()
            )
            val repository = BillRepository(db.billDao())
            val bill = repository.open(id, month)
            var failed = false
            try {
                db.billDao().save(bill.copy(rentInput = "9999").toEntity(), bill.itemEntities() + bill.itemEntities())
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
            db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills())
            val repo = BillRepository(db.billDao())
            val billingMonth = YearMonth.of(2026, 1)
            val original = repo.open(id, billingMonth)
            val paymentDate = LocalDate.of(2026, 2, 5)
            val paid = repo.markPaid(original, paymentDate)
            assertEquals(paymentDate, repo.open(id, billingMonth).paidAt)
            assertFalse(db.contractDao().observeWithStatus(billingMonth.atDay(1), billingMonth.plusMonths(1).atDay(1))
                .first().single().paymentDue)
            assertTrue(db.contractDao().observeWithStatus(billingMonth.plusMonths(1).atDay(1), billingMonth.plusMonths(2).atDay(1))
                .first().single().paymentDue)
            assertEquals(listOf(billingMonth.toString()), db.billDao().observePaidMonths(id).first())
            try { repo.save(original.copy(rentInput = "999")); fail("Paid bill accepted a stale edit") }
            catch (_: IllegalStateException) { }
            try { repo.markPaid(original, paymentDate); fail("Duplicate payment accepted") }
            catch (_: IllegalStateException) { }
            assertEquals(paid, repo.open(id, billingMonth))
            assertEquals(1, db.billDao().observePaidMonths(id).first().size)

        } finally { db.close() }
    }

    @Test fun itemsAllowRepeatedDescriptionsAndPreserveOrderWithoutServiceReferences() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val id = db.contractDao().saveContract(contract(property), emptyList(), emptyList())
            db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills())
            val repo = BillRepository(db.billDao())
            val original = repo.open(id, YearMonth.of(2026, 1))
            val items = listOf(
                com.example.myapplication.model.BillItem("Special charge", "10"),
                com.example.myapplication.model.BillItem("Special charge", "20"),
                com.example.myapplication.model.BillItem("Adjustment", "-5")
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

    private fun contract(propertyId: Long) = ContractEntity(
        propertyId = propertyId, tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
        startingDate = LocalDate.of(2026, 1, 1), finishingDate = LocalDate.of(2027, 12, 31)
    )
}
