package com.example.myapplication.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID

class PropertyManagementTest {
    @Test fun insertSavesNewOwnersReusesExistingOwnersAndRollsBackFailures() = runTest {
        val db = database()
        try {
            val properties = PropertyRepository(db)
            val first = properties.insert(Property(Address("Main", "1", null), Owner("Owner")))
            assertTrue(first.id > 0)
            assertTrue(first.owner.id > 0)
            assertEquals(first, properties.getAll().first().single())
            val second = properties.insert(Property(Address("Other", "2", null), first.owner))
            assertEquals(first.owner, second.owner)
            assertEquals(1, db.ownerDao().getAll().first().size)
            // A duplicate property ID fails after inserting its draft owner; both writes must roll back.
            assertTrue(runCatching { properties.insert(first.copy(owner = Owner("Rollback"))) }.isFailure)
            assertEquals(1, db.ownerDao().getAll().first().size)
            assertEquals(2, properties.getAll().first().size)
            properties.update(second.copy(owner = first.owner, address = Address("Updated", "3", null)))
            assertEquals(1, db.ownerDao().getAll().first().size)
            assertEquals("Updated", properties.getAll().first().single { it.id == second.id }.address.street)
        } finally { db.close() }
    }

    private val context: Context = ApplicationProvider.getApplicationContext()
    private fun database() = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    private fun draft(street: String = "Main") = Contract(
        Property(Address(street, "1", null), Owner("Owner")), Tenant("Tenant", "123"),
        BigDecimal("1000"), YearMonth.of(2020, 1), YearMonth.of(2020, 2), emptyList(),
        listOf(Service("Water")),
    )

    @Test fun unfinishedContractsBlockDeactivationRegardlessOfDatesAndFinishedHistoryRemains() = runTest {
        val db = database()
        try {
            val contracts = ContractRepository(db)
            val properties = PropertyRepository(db)
            val first = contracts.insert(draft())
            val second = contracts.insert(first.copy(id = 0))
            assertTrue(properties.observeManagement().first().single().hasActiveContracts)
            assertTrue(runCatching { properties.setActive(first.property.id, false) }.isFailure)
            contracts.finish(first.id)
            assertTrue(runCatching { properties.setActive(first.property.id, false) }.isFailure)
            contracts.finish(second.id)
            properties.setActive(first.property.id, false)
            assertTrue(properties.getActive().first().isEmpty())
            assertFalse(properties.observeManagement().first().single().isActive)
            assertEquals(2, contracts.observeInactive().first().size)
            assertEquals(2, db.billDao().list(first.id).size)
            assertFalse(properties.observeManagement().first().single().hasActiveContracts)
            properties.setActive(first.property.id, true)
            assertEquals(first.property.id, properties.getActive().first().single().id)
        } finally { db.close() }
    }

    @Test fun editsUpdateExistingContractRelationsWithoutChangingIdentityOrActivation() = runTest {
        val db = database()
        try {
            val contracts = ContractRepository(db)
            val properties = PropertyRepository(db)
            val contract = contracts.insert(draft())
            contracts.finish(contract.id)
            properties.setActive(contract.property.id, false)
            properties.update(contract.property.copy(address = Address("Corrected", "42", "B"), owner = Owner("New owner")))
            val edited = contracts.observeById(contract.id).first()!!.property
            assertEquals(contract.property.id, edited.id)
            assertEquals(Address("Corrected", "42", "B"), edited.address)
            assertEquals("New owner", edited.owner.name)
            assertFalse(properties.observeManagement().first().single().isActive)
            assertEquals(2, db.ownerDao().getAll().first().size)
            assertEquals(edited, contracts.observeInactive().first().single().property)
            assertTrue(runCatching { properties.update(edited.copy(id = -1, owner = Owner("Rollback"))) }.isFailure)
            assertEquals(2, db.ownerDao().getAll().first().size)
        } finally { db.close() }
    }

    @Test fun staleContractSelectionsCannotUseInactiveOrDeletedProperties() = runTest {
        val db = database()
        try {
            val contracts = ContractRepository(db)
            val properties = PropertyRepository(db)
            val original = contracts.insert(draft())
            contracts.finish(original.id)
            properties.setActive(original.property.id, false)
            assertTrue(runCatching { contracts.insert(original.copy(id = 0)) }.isFailure)
            assertEquals(1, contracts.getAll().first().size)
            properties.setActive(original.property.id, true)
            assertTrue(contracts.insert(original.copy(id = 0)).id > 0)
            properties.delete(original.property.id)
            assertTrue(runCatching { contracts.insert(original.copy(id = 0)) }.isFailure)
            assertTrue(properties.getAll().first().isEmpty())
            assertTrue(contracts.getAll().first().isEmpty())
        } finally { db.close() }
    }

    @Test fun deletionRemovesAllContractsAndDependentsButPreservesUnrelatedData() = runTest {
        val db = database()
        try {
            val contracts = ContractRepository(db)
            val properties = PropertyRepository(db)
            val first = contracts.insert(draft())
            val second = contracts.insert(first.copy(id = 0, services = first.services + Service("Gas")))
            contracts.finish(second.id)
            val other = contracts.insert(draft("Other").copy(services = first.services))
            db.contractDao().insertAdjustments(listOf(AdjustmentEntity(first.id, first.startingMonth, BigDecimal.ZERO)))
            val owners = db.ownerDao().getAll().first()
            properties.delete(first.property.id)
            assertEquals(listOf(other.id), contracts.getAll().first().map { it.id })
            assertTrue(db.billDao().list(first.id).isEmpty())
            assertTrue(db.billDao().list(second.id).isEmpty())
            assertEquals(2, db.billDao().list(other.id).size)
            assertEquals(listOf(other.property.id), properties.getAll().first().map { it.id })
            assertEquals(first.services.map { it.id }, db.serviceDao().getAll().first().map { it.id })
            assertEquals(owners, db.ownerDao().getAll().first())
            assertTrue(runCatching { properties.delete(first.property.id) }.isFailure)
        } finally { db.close() }
    }

    @Test fun migrationFrom20PreservesPropertiesContractsAndBills() = runTest {
        val name = "property-migration-${UUID.randomUUID()}.db"
        try {
            val original = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            val contract = try { ContractRepository(original).insert(draft()) } finally { original.close() }
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use {
                it.execSQL("ALTER TABLE Property DROP COLUMN isActive")
                it.version = 20
            }
            val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_20_21).build()
            try {
                assertEquals(contract.property, PropertyRepository(migrated).getActive().first().single())
                assertEquals(contract.id, ContractRepository(migrated).getAll().first().single().id)
                assertEquals(2, migrated.billDao().list(contract.id).size)
            } finally { migrated.close() }
        } finally { context.deleteDatabase(name) }
    }
}
