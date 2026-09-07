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
    fun paymentDueOnlyForOverlappingUnpaidContracts() {
        assertTrue(contract().listItem(month, setOf(2L)).paymentDue)
        assertFalse(contract().listItem(month, setOf(1L)).paymentDue)
        assertFalse(contract().copy(startingDate = month.plusMonths(1).atDay(1)).listItem(month, emptySet()).paymentDue)
        assertFalse(contract().copy(finishingDate = month.atDay(1).minusDays(1)).listItem(month, emptySet()).paymentDue)
        assertTrue(contract().copy(startingDate = month.atEndOfMonth()).listItem(month, emptySet()).paymentDue)
    }

    @Test
    fun expiryIncludesThisAndNextMonthAcrossYearBoundary() {
        assertTrue(contract().listItem(month, emptySet()).expiresSoon)
        assertTrue(contract().copy(finishingDate = month.atDay(1)).listItem(month, emptySet()).expiresSoon)
        assertFalse(contract().copy(finishingDate = month.minusMonths(1).atEndOfMonth()).listItem(month, emptySet()).expiresSoon)
        assertFalse(contract().copy(finishingDate = month.plusMonths(2).atDay(1)).listItem(month, emptySet()).expiresSoon)
    }

    @Test
    fun hikeMatchesYearAndMonthEvenWhenAmountIsPending() {
        assertTrue(contract().copy(adjustments = listOf(Adjustment(month.atEndOfMonth()))).listItem(month, emptySet()).hikeThisMonth)
        assertFalse(contract().copy(adjustments = listOf(Adjustment(month.minusYears(1).atDay(1)))).listItem(month, emptySet()).hikeThisMonth)
        assertFalse(contract().listItem(month, emptySet()).hikeThisMonth)
    }
}
