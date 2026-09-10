package com.example.myapplication.model

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class BillTest {
    private fun bill(items: List<BillItem>) = Bill(1, 1, YearMonth.of(2026, 1), "1000", items, 0)

    @Test fun totalIncludesEveryItemEvenWhenDescriptionsRepeat() {
        val bill = bill(listOf(BillItem("Charge", "20.50"), BillItem("Charge", "10"), BillItem("Credit", "-5")))
        assertTrue(bill.isValid)
        assertEquals(0, BigDecimal("1025.50").compareTo(bill.total))
    }

    @Test fun noItemsNeedsOnlyRentAndIncompleteItemsPreventConfirmation() {
        assertTrue(bill(emptyList()).isValid)
        assertEquals(0, BigDecimal("1000").compareTo(bill(emptyList()).total))
        assertFalse(bill(listOf(BillItem("Charge"))).isValid)
        assertFalse(bill(emptyList()).copy(rentInput = "0").isValid)
    }
}
