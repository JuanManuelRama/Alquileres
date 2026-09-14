package com.example.myapplication.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.entity.*
import com.example.myapplication.data.repository.BillRepository
import com.example.myapplication.model.Address
import com.example.myapplication.model.Tenant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class ContractFinishingTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun migrationKeepsExistingContractsAndBillsActive() = runTest {
        val name = "contract-migration-${UUID.randomUUID()}.db"
        try {
            val original = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            val id = try { seed(original) } finally { original.close() }
            // Reconstruct the version-18 shape; only the new flag differs from version 19.
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use {
                it.execSQL("ALTER TABLE Contract DROP COLUMN isFinished")
                it.version = 18
            }
            val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_18_19).build()
            try {
                assertEquals(id, migrated.contractDao().observeWithStatus(YearMonth.of(2026, 1)).first().single().details.contract.id)
                assertEquals(2, migrated.billDao().list(id).size)
                assertTrue(migrated.contractDao().observeInactive().first().isEmpty())
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
        db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills())
        return id
    }

    @Test fun cancellationFreezesUnpaidBillsAndRejectsStaleWrites() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val id = seed(db)
            val repo = BillRepository(db.billDao())
            val stale = repo.list(id).first()
            assertEquals(0, db.contractDao().finish(id, requireAllPaid = true))
            assertEquals(1, db.contractDao().finish(id, requireAllPaid = false))
            assertTrue(db.contractDao().observeWithStatus(stale.month).first().isEmpty())
            assertEquals(id, db.contractDao().observeInactive().first().single().contract.id)
            val frozen = repo.list(id)
            assertTrue(frozen.all { !it.isPaid })
            try { repo.save(stale); fail("Inactive bill accepted save") } catch (_: IllegalStateException) { }
            try { repo.markPaid(stale, LocalDate.of(2026, 2, 1)); fail("Inactive bill accepted payment") }
            catch (_: IllegalStateException) { }
            db.contractDao().updateAdjustmentAmount(id, stale.month, BigDecimal("0.50"))
            assertEquals(0, BigDecimal.ZERO.compareTo(db.contractDao().observeById(id).first()!!.adjustments.single().amount))
            assertEquals(frozen, repo.list(id))
        } finally { db.close() }
    }

    @Test fun finishingRequiresEveryBillToBePaidAndPreservesPaymentDates() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val id = seed(db)
            val repo = BillRepository(db.billDao())
            val bills = repo.list(id)
            val date = LocalDate.of(2026, 3, 1)
            repo.markPaid(bills.first(), date)
            assertEquals(0, db.contractDao().finish(id, requireAllPaid = true))
            repo.markPaid(bills.last(), date)
            assertEquals(1, db.contractDao().finish(id, requireAllPaid = true))
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
            db.billDao().createAll(db.contractDao().observeById(id).first()!!.toDomain().createBills())

            assertEquals(1, db.contractDao().deleteContract(id))
            assertNull(db.contractDao().observeById(id).first())
            assertTrue(db.billDao().list(id).isEmpty())
        } finally { db.close() }
    }
}
