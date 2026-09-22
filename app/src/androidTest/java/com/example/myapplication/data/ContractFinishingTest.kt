package com.example.myapplication.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.ServiceEntity
import com.example.myapplication.data.entity.itemEntities
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.data.repository.BillRepository
import com.example.myapplication.model.Address
import com.example.myapplication.model.Tenant
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
import java.util.UUID

class ContractFinishingTest {
    @Test fun renewalFinishesOriginalAndPreservesItsBills() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val id = seed(db)
            val repository = com.example.myapplication.data.repository.ContractRepository(db)
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
            val repository = com.example.myapplication.data.repository.ContractRepository(db)
            val original = repository.observeById(id).first()!!
            val failure = runCatching {
                repository.insert(original.copy(
                    id = 0,
                    services = listOf(com.example.myapplication.model.Service("Missing", 9999)),
                ), renewingContractId = id)
            }
            assertTrue(failure.isFailure)
            assertFalse(db.contractDao().observeById(id).first()!!.contract.isFinished)
            assertEquals(listOf(id), db.contractDao().getAllContracts().first().map { it.contract.id })
            assertEquals(2, db.billDao().list(id).size)
        } finally { db.close() }
    }

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun migrationKeepsExistingContractsAndBillsActive() = runTest {
        val name = "contract-migration-${UUID.randomUUID()}.db"
        try {
            val original = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            val id = try { seed(original) } finally { original.close() }
            // Reconstruct version 18: no finished flag and no service-link delete cascade.
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use {
                it.execSQL("ALTER TABLE Contract DROP COLUMN isFinished")
                it.execSQL("ALTER TABLE Property DROP COLUMN isActive")
                it.execSQL("DROP TABLE ContractServiceCrossRef")
                it.execSQL("""
                    CREATE TABLE ContractServiceCrossRef (
                        contractId INTEGER NOT NULL, serviceId INTEGER NOT NULL,
                        PRIMARY KEY(contractId, serviceId),
                        FOREIGN KEY(contractId) REFERENCES Contract(id) ON DELETE NO ACTION,
                        FOREIGN KEY(serviceId) REFERENCES Service(id) ON DELETE NO ACTION
                    )
                """.trimIndent())
                it.execSQL("CREATE INDEX index_ContractServiceCrossRef_serviceId ON ContractServiceCrossRef(serviceId)")
                it.execSQL("INSERT INTO Service (id, name) VALUES (1, 'Water')")
                it.execSQL("INSERT INTO ContractServiceCrossRef (contractId, serviceId) VALUES ($id, 1)")
                it.version = 18
            }
            val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20, AppDatabase.MIGRATION_20_21).build()
            try {
                assertEquals(id, migrated.contractDao().observeWithStatus(YearMonth.of(2026, 1)).first().single().details.contract.id)
                assertEquals(2, migrated.billDao().list(id).size)
                assertTrue(migrated.contractDao().observeInactive().first().isEmpty())
                assertEquals("Water", migrated.contractDao().observeById(id).first()!!.services.single().name)
                assertEquals(1, migrated.contractDao().deleteContract(id))
                assertTrue(migrated.billDao().list(id).isEmpty())
                assertEquals("Water", migrated.serviceDao().getAll().first().single().name)
            } finally { migrated.close() }
        } finally { context.deleteDatabase(name) }
    }

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
            com.example.myapplication.data.repository.ContractRepository(db).cancel(id, cancellationMonth = cancelMonth)

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
            val repository = com.example.myapplication.data.repository.ContractRepository(db)
            val originalId = seed(db)
            val original = repository.observeById(originalId).first()!!
            val first = repository.insert(original.copy(
                id = 0,
                services = listOf(
                    com.example.myapplication.model.Service("Water"),
                    com.example.myapplication.model.Service("Gas"),
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
