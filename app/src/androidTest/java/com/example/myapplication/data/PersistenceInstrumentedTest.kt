package com.example.myapplication.data

import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PaymentEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.ServiceEntity
import com.example.myapplication.data.entity.ServicePaymentEntity
import com.example.myapplication.model.Tenant
import com.example.myapplication.model.Address
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.LocalDate

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
    fun basicEntitiesRoundTripThroughRoom() = runTest {
        database.ownerDao().insert(OwnerEntity(name = "Ada"))
        database.serviceDao().insert(ServiceEntity(name = "Water"))

        assertEquals("Ada", database.ownerDao().getAll().first().single().name)
        assertEquals("Water", database.serviceDao().getAll().first().single().name)
    }

    @Test
    fun detailQueriesFilterByIdAndReadUpdatedRelationsAndPaymentDates() = runTest {
        val ownerId = database.ownerDao().insert(OwnerEntity(name = "Owner"))
        val propertyId = database.propertyDao().insertProperty(
            PropertyEntity(address = Address("Main", "1", null), ownerId = ownerId)
        )
        val date = LocalDate.of(2026, 7, 1)
        val contract = ContractEntity(
            propertyId = propertyId, tenant = Tenant("Tenant", "123"),
            rent = BigDecimal("1000"), startingDate = date.minusMonths(6), finishingDate = date.plusMonths(6)
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

        database.paymentDao().insertPayment(PaymentEntity(contractId = firstId, rent = BigDecimal("1000"), date = date))
        assertEquals(listOf(date), database.paymentDao().observePaymentDates(firstId).first())
        assertTrue(database.paymentDao().observePaymentDates(secondId).first().isEmpty())
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
        val start = LocalDate.of(2026, 1, 1)
        val finish = LocalDate.of(2027, 1, 1)
        val adjustmentDate = LocalDate.of(2026, 7, 1)

        database.contractDao().saveContract(
            contract = ContractEntity(
                propertyId = propertyId,
                tenant = tenant,
                rent = BigDecimal("1250.50"),
                startingDate = start,
                finishingDate = finish,
            ),
            serviceIds = listOf(serviceId),
            adjustments = listOf(
                AdjustmentEntity(contractId = 0, time = adjustmentDate, amount = BigDecimal("0.10")),
            ),
        )

        val result = database.contractDao().getAllContracts().first().single()
        assertNotNull(result.contract.id)
        assertEquals(BigDecimal("1250.50"), result.contract.rent)
        assertEquals(start, result.contract.startingDate)
        assertEquals(finish, result.contract.finishingDate)
        assertEquals(tenant, result.contract.tenant)
        assertEquals(tenant, result.toDomain().tenant)
        assertEquals("Electricity", result.services.single().name)
        assertEquals(adjustmentDate, result.adjustments.single().time)
        assertEquals(BigDecimal("0.10"), result.adjustments.single().amount)
    }

    @Test
    fun paymentTransactionPersistsServiceAmountsForItsContract() = runTest {
        val ownerId = database.ownerDao().insert(OwnerEntity(name = "Owner"))
        val propertyId = database.propertyDao().insertProperty(
            PropertyEntity(address = Address("Main", "10", null), ownerId = ownerId),
        )
        val serviceId = database.serviceDao().insert(ServiceEntity(name = "Gas"))
        val contractId = database.contractDao().insertContract(
            ContractEntity(
                propertyId = propertyId,
                tenant = Tenant(name = "Tenant", phone = "123"),
                rent = BigDecimal("900.00"),
                startingDate = LocalDate.of(2026, 1, 1),
                finishingDate = LocalDate.of(2027, 1, 1),
            ),
        )

        database.paymentDao().savePayment(
            payment = PaymentEntity(
                contractId = contractId,
                rent = BigDecimal("900.00"),
                date = LocalDate.of(2026, 2, 1),
            ),
            servicePayments = listOf(
                ServicePaymentEntity(paymentId = 0, serviceId = serviceId, amount = BigDecimal("45.75")),
            ),
        )

        val result = database.paymentDao().getPaymentsForContract(contractId).first().single()
        assertEquals(BigDecimal("900.00"), result.payment.rent)
        assertEquals("Gas", result.servicePayments.single().service.name)
        assertEquals(BigDecimal("45.75"), result.servicePayments.single().servicePayment.amount)
    }
}
