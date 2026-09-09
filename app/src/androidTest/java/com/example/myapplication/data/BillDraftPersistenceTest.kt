package com.example.myapplication.data

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.entity.*
import com.example.myapplication.data.repository.BillDraftRepository
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

class BillDraftPersistenceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val month = YearMonth.of(2026, 12)

    @Test fun snapshotsSurviveReopeningAndDoNotCountAsPayments() = runTest {
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
            db.billDraftDao().createAll(
                db.contractDao().observeById(id).first()!!.toDomain().createBills()
            )
            val repository = BillDraftRepository(db.billDraftDao())
            val first = repository.open(id, month)
            assertEquals(24, db.billDraftDao().list(id).size)
            val originalIds = repository.list(id).map { it.id }
            assertEquals(originalIds, repository.list(id).map { it.id })
            val contracts = ContractRepository(db.contractDao(), db.billDraftDao())
            val created = contracts.insert(contracts.observeById(id).first()!!.copy(id = 0))
            assertEquals(24, db.billDraftDao().list(created.id).size)
            assertEquals(0, BigDecimal("1100").compareTo(first.rentInput.toBigDecimal()))
            assertEquals("Water", first.services.single().name)
            assertFalse(first.isValid)
            repository.save(first.copy(services = first.services.map { it.copy(amountInput = "-") }))
            db.contractDao().updateAdjustmentAmount(id, month.atDay(1), BigDecimal("0.50"))
            val extraService = db.serviceDao().insert(ServiceEntity(name = "Power"))
            db.contractDao().insertContractServiceCrossRefs(listOf(ContractServiceCrossRef(id, extraService)))

            val reopened = repository.open(id, month)
            assertEquals(first.id, reopened.id)
            assertEquals(first.rentInput, reopened.rentInput)
            assertEquals("-", reopened.services.single().amountInput)
            assertTrue(db.paymentDao().observePaymentDates(id).first().isEmpty())
            assertTrue(db.contractDao().observeWithStatus(month.atDay(1), month.plusMonths(1).atDay(1)).first().first { it.details.contract.id == id }.paymentDue)

            val next = repository.open(id, month.plusMonths(1))
            assertNotEquals(first.id, next.id)
            assertEquals(0, BigDecimal("1100").compareTo(next.rentInput.toBigDecimal()))
            assertEquals(1, next.services.size)
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
            db.billDraftDao().createAll(
                db.contractDao().observeById(id).first()!!.toDomain().createBills()
            )
            val repository = BillDraftRepository(db.billDraftDao())
            val draft = repository.open(id, month)
            var failed = false
            try {
                repository.save(draft.copy(rentInput = "9999", services = draft.services + draft.services))
            } catch (_: Exception) { failed = true }
            assertTrue(failed)
            assertEquals(draft, repository.open(id, month))
        } finally { db.close() }
    }

    private fun contract(propertyId: Long) = ContractEntity(
        propertyId = propertyId, tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
        startingDate = LocalDate.of(2026, 1, 1), finishingDate = LocalDate.of(2027, 12, 31)
    )
}
