package com.example.myapplication.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class PaymentAndAddressTest {
    @Test
    fun addressOmitsMissingUnitAndIncludesPresentUnit() {
        assertEquals("Oak 430", Address("Oak", "430", null).toString())
        assertEquals("Oak 430 B", Address("Oak", "430", "B").toString())
    }

    @Test
    fun propertyUsesItsAddressAsDisplayText() {
        val property = Property(Address("Oak", "430", "B"), Owner("Ada"))

        assertEquals("Oak 430 B", property.toString())
    }

    @Test
    fun paymentTotalCombinesRentAndEveryServiceAmount() {
        val payment = Payment(
            contract = contract(),
            services = listOf(
                ServicePayment(Service("Water"), BigDecimal("12.50")),
                ServicePayment(Service("Power"), BigDecimal("30.25"))
            ),
            rent = BigDecimal("1000.00"),
            date = LocalDate.of(2025, 2, 1)
        )

        assertEquals(0, BigDecimal("1042.75").compareTo(payment.total))
    }

    @Test
    fun paymentWithoutServicesEqualsRent() {
        val payment = Payment(contract(), emptyList(), BigDecimal("1000.00"), LocalDate.of(2025, 2, 1))

        assertEquals(0, payment.rent.compareTo(payment.total))
    }

    private fun contract() = Contract(
        Property(Address("Main", "1", null), Owner("Owner")),
        Tenant("Tenant", "123"),
        BigDecimal("1000.00"),
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2026, 1, 1),
        emptyList(),
        emptyList()
    )
}
