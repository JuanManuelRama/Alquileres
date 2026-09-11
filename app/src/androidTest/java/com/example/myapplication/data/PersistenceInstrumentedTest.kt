package com.example.myapplication.data

import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.ServiceEntity
import com.example.myapplication.model.Tenant
import com.example.myapplication.model.Address
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class PersistenceInstrumentedTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun adjustmentKeyRejectsDuplicateMonthsAndUpdatesOnlyTheMatchingEntry() = runTest {
        val ownerId = database.ownerDao().insert(OwnerEntity(name = "Owner"))
        val propertyId = database.propertyDao().insertProperty(
            PropertyEntity(address = Address("Main", "1", null), ownerId = ownerId)
        )
        val month = YearMonth.of(2026, 7)
        val contract = ContractEntity(propertyId = propertyId, tenant = Tenant("Tenant", "123"),
            rent = BigDecimal("1000"), startingMonth = month, finishingMonth = month.plusMonths(12))
        val firstId = database.contractDao().insertContract(contract)
        val secondId = database.contractDao().insertContract(contract)
        database.contractDao().insertAdjustments(listOf(
            AdjustmentEntity(firstId, month, null),
            AdjustmentEntity(firstId, month.plusMonths(1), null),
            AdjustmentEntity(secondId, month, null)
        ))
        val duplicate = runCatching {
            database.contractDao().insertAdjustments(listOf(AdjustmentEntity(firstId, month, BigDecimal("0.50"))))
        }
        assertTrue("Duplicate contract/month must be rejected", duplicate.isFailure)
        database.contractDao().updateAdjustmentAmount(firstId, month, BigDecimal("0.20"))
        val first = database.contractDao().observeById(firstId).first()!!.adjustments
        assertEquals(2, first.size)
        assertEquals(BigDecimal("0.20"), first.single { it.time == month }.amount)
        assertNull(first.single { it.time == month.plusMonths(1) }.amount)
        assertNull(database.contractDao().observeById(secondId).first()!!.adjustments.single().amount)
    }

    @Test
    fun basicEntitiesRoundTripThroughRoom() = runTest {
        database.ownerDao().insert(OwnerEntity(name = "Ada"))
        database.serviceDao().insert(ServiceEntity(name = "Water"))

        assertEquals("Ada", database.ownerDao().getAll().first().single().name)
        assertEquals("Water", database.serviceDao().getAll().first().single().name)
    }

    @Test
    fun detailQueriesFilterByIdAndReadUpdatedRelationsAndPaidBills() = runTest {
        val ownerId = database.ownerDao().insert(OwnerEntity(name = "Owner"))
        val propertyId = database.propertyDao().insertProperty(
            PropertyEntity(address = Address("Main", "1", null), ownerId = ownerId)
        )
        val date = YearMonth.of(2026, 7)
        val contract = ContractEntity(
            propertyId = propertyId, tenant = Tenant("Tenant", "123"),
            rent = BigDecimal("1000"), startingMonth = date.minusMonths(6), finishingMonth = date.plusMonths(6)
        )
        val firstId = database.contractDao().saveContract(
            contract, emptyList(), listOf(AdjustmentEntity(contractId = 0, time = date, amount = null))
        )
        val secondId = database.contractDao().insertContract(contract)
        assertNull(database.contractDao().observeById(-1L).first())
        assertEquals(firstId, database.contractDao().observeById(firstId).first()!!.contract.id)

        database.contractDao().updateAdjustmentAmount(firstId, date, BigDecimal("0.20"))
        val updated = database.contractDao().observeById(firstId).first()!!
        assertEquals(BigDecimal("0.20"), updated.adjustments.single().amount)
        assertEquals("Owner", updated.property.owner.name)
        assertTrue(database.contractDao().observeById(secondId).first()!!.adjustments.isEmpty())

        database.billDao().createAll(updated.toDomain().createBills())
        val bill = database.billDao().find(firstId, date)!!.toDomain()
        database.billDao().markPaid(bill, date.atDay(1))
        assertEquals(listOf(date), database.billDao().observePaidMonths(firstId).first())
        assertTrue(database.billDao().observePaidMonths(secondId).first().isEmpty())
        val statuses = database.contractDao().observeWithStatus(date).first()
            .associateBy { it.details.contract.id }
        assertFalse(statuses.getValue(firstId).paymentDue)
        val summary = statuses.getValue(firstId).details.toDomain()
        assertEquals("Owner", summary.property.owner.name)
        assertEquals(Address("Main", "1", null), summary.property.address)
        assertTrue(summary.adjustments.isEmpty())
        assertTrue(summary.services.isEmpty())
        assertTrue(statuses.getValue(firstId).hikeThisMonth)
        assertTrue(statuses.getValue(secondId).paymentDue)
        assertFalse(statuses.getValue(secondId).hikeThisMonth)
        val previousMonth = database.contractDao().observeWithStatus(date.minusMonths(1)).first()
        assertTrue(previousMonth.all { it.paymentDue && !it.hikeThisMonth })
    }

    @Test
    fun propertyRoundTripIncludesItsOwnerAndEmbeddedAddress() = runTest {
        val ownerId = database.ownerDao().insert(OwnerEntity(name = "Owner"))
        val address = Address("Oak", "430", "2B")
        val propertyId = database.propertyDao().insertProperty(
            PropertyEntity(address = address, ownerId = ownerId),
        )

        val result = database.propertyDao().getAllPropertiesWithOwners().first().single()
        assertEquals(propertyId, result.property.id)
        assertEquals(address, result.property.address)
        assertEquals("Owner", result.owner.name)
    }

    @Test
    fun contractTransactionPersistsServicesAdjustmentsAndDateConverters() = runTest {
        val ownerId = database.ownerDao().insert(OwnerEntity(name = "Owner"))
        val propertyId = database.propertyDao().insertProperty(
            PropertyEntity(address = Address("Main", "10", null), ownerId = ownerId),
        )
        val tenant = Tenant(name = "Tenant", phone = "123")
        val serviceId = database.serviceDao().insert(ServiceEntity(name = "Electricity"))
        val start = YearMonth.of(2026, 1)
        val finish = YearMonth.of(2027, 1)
        val adjustmentDate = YearMonth.of(2026, 7)

        database.contractDao().saveContract(
            contract = ContractEntity(
                propertyId = propertyId,
                tenant = tenant,
                rent = BigDecimal("1250.50"),
                startingMonth = start,
                finishingMonth = finish,
            ),
            serviceIds = listOf(serviceId),
            adjustments = listOf(
                AdjustmentEntity(contractId = 0, time = adjustmentDate, amount = BigDecimal("0.10")),
            ),
        )

        val result = database.contractDao().getAllContracts().first().single()
        assertNotNull(result.contract.id)
        assertEquals(BigDecimal("1250.50"), result.contract.rent)
        assertEquals(start, result.contract.startingMonth)
        assertEquals(finish, result.contract.finishingMonth)
        assertEquals(tenant, result.contract.tenant)
        assertEquals(tenant, result.toDomain().tenant)
        assertEquals("Electricity", result.services.single().name)
        assertEquals(adjustmentDate, result.adjustments.single().time)
        assertEquals(BigDecimal("0.10"), result.adjustments.single().amount)
    }

}
