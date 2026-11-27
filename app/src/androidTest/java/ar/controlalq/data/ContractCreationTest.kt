package ar.controlalq.data

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import ar.controlalq.data.repository.ContractRepository
import ar.controlalq.model.Address
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Service
import ar.controlalq.model.Tenant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractCreationTest {
    @Test fun failedCreationRollsBackNewRelatedRecordsAndRetryCreatesOnlyOneSet() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = ContractRepository(db)
            val draft = Contract(
                Property(Address("Main", "1", null), Owner("New owner")),
                Tenant("Tenant", "16502530000"), BigDecimal("1000"),
                YearMonth.of(2026, 1), YearMonth.of(2026, 12), emptyList(),
                listOf(Service("New service"), Service("Missing service", 9999))
            )
            // The invalid service link fails after owner, property, service and contract inserts.
            assertTrue(runCatching { repository.insert(draft) }.isFailure)
            assertTrue(db.ownerDao().getAll().first().isEmpty())
            assertTrue(db.propertyDao().getAllPropertiesWithOwners().first().isEmpty())
            assertTrue(db.serviceDao().getAll().first().isEmpty())
            assertTrue(db.contractDao().getAllContracts().first().isEmpty())
            val saved = repository.insert(draft.copy(services = draft.services.take(1)))
            assertEquals(1, db.ownerDao().getAll().first().size)
            assertEquals(1, db.propertyDao().getAllPropertiesWithOwners().first().size)
            assertEquals(1, db.serviceDao().getAll().first().size)
            assertEquals(1, db.contractDao().getAllContracts().first().size)
            assertEquals(12, db.billDao().list(saved.id).size)
            assertTrue(saved.property.id > 0)
            assertTrue(saved.property.owner.id > 0)
            assertTrue(saved.services.single().id > 0)
        } finally { db.close() }
    }
}
