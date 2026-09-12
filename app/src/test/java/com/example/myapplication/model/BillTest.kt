package com.example.myapplication.model

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class BillTest {
    private val contract = Contract(
        property = Property(Address("Street", "12", null), Owner("Owner", 1), 1),
        tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
        startingMonth = YearMonth.of(2026, 1), finishingMonth = YearMonth.of(2026, 3),
        adjustments = emptyList(), services = emptyList(), id = 1
    )
    private fun bill(items: List<BillItem>) = Bill(1, contract, YearMonth.of(2026, 1), 1000.toBigDecimal(), items, 0)

    @Test fun totalIncludesEveryItemEvenWhenDescriptionsRepeat() {
        val bill = bill(listOf(
            BillItem("Charge", 20.50.toBigDecimal()),
            BillItem("Charge", 10.toBigDecimal()),
            BillItem("Credit", (-5).toBigDecimal()
        )))
        assertTrue(bill.isValid)
        assertEquals(0, BigDecimal("1025.50").compareTo(bill.total))
    }

    @Test fun noItemsNeedsOnlyRentAndIncompleteItemsPreventConfirmation() {
        assertTrue(bill(emptyList()).isValid)
        assertEquals(0, BigDecimal("1000").compareTo(bill(emptyList()).total))
        assertFalse(bill(listOf(BillItem("Charge", null))).isValid)
        assertTrue(bill(emptyList()).copy(rent = BigDecimal.ZERO).isValid)
    }
}
