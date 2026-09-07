package com.example.myapplication.ui.contract.list

import com.example.myapplication.model.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class ContractListItemTest {
    private val month = YearMonth.of(2026, 12)
    private fun contract() = Contract(
        property = Property(Address("Main", "1", null), Owner("Owner")),
        tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
        startingDate = LocalDate.of(2026, 1, 1), finishingDate = LocalDate.of(2027, 1, 31),
        adjustments = emptyList(), services = emptyList(), id = 1L
    )

    @Test
    fun expiryIncludesThisAndNextMonthAcrossYearBoundary() {
        assertTrue(contract().listItem(month, false, false).expiresSoon)
        assertTrue(contract().copy(finishingDate = month.atDay(1)).listItem(month, false, false).expiresSoon)
        assertFalse(contract().copy(finishingDate = month.minusMonths(1).atEndOfMonth()).listItem(month, false, false).expiresSoon)
        assertFalse(contract().copy(finishingDate = month.plusMonths(2).atDay(1)).listItem(month, false, false).expiresSoon)
    }

}
