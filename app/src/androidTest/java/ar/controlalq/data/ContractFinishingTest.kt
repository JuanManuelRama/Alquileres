package ar.controlalq.data

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import ar.controlalq.data.entity.AdjustmentEntity
import ar.controlalq.data.entity.ContractEntity
import ar.controlalq.data.entity.OwnerEntity
import ar.controlalq.data.entity.PropertyEntity
import ar.controlalq.data.entity.ServiceEntity
import ar.controlalq.data.entity.itemEntities
import ar.controlalq.data.entity.toEntity
import ar.controlalq.data.repository.BillRepository
import ar.controlalq.data.repository.ContractRepository
import ar.controlalq.model.Address
import ar.controlalq.model.Service
import ar.controlalq.model.Tenant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class ContractFinishingTest {
    @Test fun renewalFinishesOriginalAndPreservesItsBills() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val id = seed(db)
            val repository = ContractRepository(db)
            val original = repository.observeById(id).first()!!
            val bills = db.billDao().list(id)
            val renewed = repository.insert(original.copy(
                id = 0,
                startingMonth = original.finishingMonth.plusMonths(1),
                finishingMonth = original.finishingMonth.plusMonths(2),
                adjustments = emptyList(),
            ), renewingContractId = id)
            assertTrue(db.contractDao().observeById(id).first()!!.contract.isFinished)
            assertEquals(original, repository.observeById(id).first())
            assertEquals(bills.map { it.bill }, db.billDao().list(id).map { it.bill })
            assertFalse(db.contractDao().observeById(renewed.id).first()!!.contract.isFinished)
            assertEquals(2, db.billDao().list(renewed.id).size)
        } finally { db.close() }
    }

    @Test fun failedRenewalRollsBackOriginalFinishedFlagAndNewContract() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val id = seed(db)
            val repository = ContractRepository(db)
            val original = repository.observeById(id).first()!!
            val failure = runCatching {
                repository.insert(original.copy(
                    id = 0,
                    services = listOf(Service("Missing", 9999)),
                ), renewingContractId = id)
            }
            assertTrue(failure.isFailure)
            assertFalse(db.contractDao().observeById(id).first()!!.contract.isFinished)
            assertEquals(listOf(id), db.contractDao().getAllContracts().first().map { it.contract.id })
            assertEquals(2, db.billDao().list(id).size)
        } finally { db.close() }
    }

    private val context: Context = ApplicationProvider.getApplicationContext()

    private suspend fun seed(db: AppDatabase): Long {
        val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
        val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
        val month = YearMonth.of(2026, 1)
        val id = db.contractDao().saveContract(
            ContractEntity(propertyId = property, tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
                startingMonth = month, finishingMonth = month.plusMonths(1)),
            emptyList(), listOf(AdjustmentEntity(0, month, BigDecimal.ZERO))
        )
        db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills().associate { it.toEntity() to it.itemEntities() })
        return id
    }

    @Test fun finishingPreservesPaymentDates() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val id = seed(db)
            val repo = BillRepository(db.billDao())
            val bills = repo.list(id)
            val date = LocalDate.of(2026, 3, 1)
            repo.markPaid(bills.first(), date)
            repo.markPaid(bills.last(), date)
            assertEquals(1, db.contractDao().finish(id))
            assertTrue(db.contractDao().observeWithStatus(bills.first().month).first().isEmpty())
            assertEquals(id, db.contractDao().observeInactive().first().single().contract.id)
            assertTrue(repo.list(id).all { it.paidAt == date })
        } finally { db.close() }
    }

    @Test fun deletingContractCascadesToBillsItemsAdjustmentsAndCrossRefs() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val service = db.serviceDao().insert(ServiceEntity(name = "Water"))
            val month = YearMonth.of(2026, 1)
            val id = db.contractDao().saveContract(
                ContractEntity(propertyId = property, tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
                    startingMonth = month, finishingMonth = month.plusMonths(1)),
                listOf(service), listOf(AdjustmentEntity(0, month, BigDecimal.ZERO))
            )
            db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills().associate { it.toEntity() to it.itemEntities() })

            assertEquals(1, db.contractDao().deleteContract(id))
            assertNull(db.contractDao().observeById(id).first())
            assertTrue(db.billDao().list(id).isEmpty())
        } finally { db.close() }
    }

    @Test fun earlyCancellationDeletesFutureBillsAndUpdatesEndMonth() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val owner = db.ownerDao().insert(OwnerEntity(name = "Owner"))
            val property = db.propertyDao().insertProperty(PropertyEntity(address = Address("Main", "1", null), ownerId = owner))
            val startMonth = YearMonth.of(2026, 1)
            val finishMonth = YearMonth.of(2026, 12)
            val id = db.contractDao().saveContract(
                ContractEntity(propertyId = property, tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
                    startingMonth = startMonth, finishingMonth = finishMonth),
                emptyList(), emptyList()
            )
            val contractDomain = db.contractDao().observeById(id).first()!!.toDomain()
            db.billDao().createAll(contractDomain.createBills().associate { it.toEntity() to it.itemEntities() })
            assertEquals(12, db.billDao().list(id).size)

            val cancelMonth = YearMonth.of(2026, 3)
            ContractRepository(db).cancel(id, cancellationMonth = cancelMonth)

            val cancelled = db.contractDao().observeById(id).first()!!.toDomain()
            assertEquals(cancelMonth, cancelled.finishingMonth)
            val remainingBills = db.billDao().list(id)
            assertEquals(3, remainingBills.size)
            assertEquals(listOf(YearMonth.of(2026, 1), YearMonth.of(2026, 2), YearMonth.of(2026, 3)), remainingBills.map { it.bill.billingMonth })
        } finally { db.close() }
    }

    @Test fun repositoryDeletionRemovesAllUnreferencedServicesAndPreservesSharedServices() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = ContractRepository(db)
            val originalId = seed(db)
            val original = repository.observeById(originalId).first()!!
            val first = repository.insert(original.copy(
                id = 0,
                services = listOf(
                    Service("Water"),
                    Service("Gas"),
                ),
            ))
            val shared = first.services.first()
            val second = repository.insert(original.copy(id = 0, services = listOf(shared)))
            db.serviceDao().insert(ServiceEntity(name = "Unrelated"))
            val properties = db.propertyDao().getAllPropertiesWithOwners().first()
            val owners = db.ownerDao().getAll().first()

            // Even a finished contract keeps its service references alive.
            repository.finish(second.id)
            repository.delete(first.id)
            assertNull(repository.observeById(first.id).first())
            assertTrue(db.billDao().list(first.id).isEmpty())
            assertEquals(listOf(shared.id), db.serviceDao().getAll().first().map { it.id })
            assertEquals(listOf(shared), repository.observeById(second.id).first()!!.services)

            repository.delete(second.id)
            assertTrue(db.serviceDao().getAll().first().isEmpty())
            assertEquals(properties, db.propertyDao().getAllPropertiesWithOwners().first())
            assertEquals(owners, db.ownerDao().getAll().first())

            // Contracts without services still delete normally; missing contracts still fail.
            db.serviceDao().insert(ServiceEntity(name = "Unused"))
            repository.delete(originalId)
            assertTrue(db.serviceDao().getAll().first().isEmpty())
            val retained = db.serviceDao().insert(ServiceEntity(name = "Retained after failure"))
            assertTrue(runCatching { repository.delete(originalId) }.isFailure)
            assertEquals(listOf(retained), db.serviceDao().getAll().first().map { it.id })
        } finally { db.close() }
    }
}
