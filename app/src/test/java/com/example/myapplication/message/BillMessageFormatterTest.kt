package com.example.myapplication.message

import com.example.myapplication.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class BillMessageFormatterTest {
    private val month = YearMonth.of(2026, 9)
    private val contract = Contract(
        Property(Address("Street", "12", "A"), Owner("Owner", 1), 1),
        Tenant("Ada", "6502530000"), BigDecimal("1000"), month, month,
        emptyList(), emptyList(), 1
    )
    private val bill = Bill(1, contract, month, BigDecimal("1000"), listOf(
        BillItem("Water", BigDecimal("25.50")),
        BillItem("Refund", BigDecimal("-10.25"), true)
    ), 0)

    @Test fun includesTenantPropertyMonthItemsAndExactTotal() {
        assertEquals("""
            Hello Ada,

            Bill for September 2026
            Property: Street 12 A

            Rent: $ 1,000
            Water: $ 25.5
            Refund: $ -10.25
            Total: $ 1,015.25
        """.trimIndent(), BillMessageFormatter.format(bill, Locale.US))
    }

    @Test fun paidBillIncludesPaymentDate() {
        assertTrue(BillMessageFormatter.format(bill.copy(paidAt = LocalDate.of(2026, 9, 10)), Locale.US)
            .endsWith("Paid on Sep 10, 2026"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun incompleteBillCannotBeFormattedAsComplete() {
        BillMessageFormatter.format(bill.copy(items = listOf(BillItem("Water", null))))
    }
}
