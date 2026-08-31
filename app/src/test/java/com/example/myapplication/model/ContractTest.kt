package com.example.myapplication.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class ContractTest {
    private val start = LocalDate.of(2025, 1, 1)
    private val finish = LocalDate.of(2026, 1, 1)

    @Test
    fun valueAfterDateAppliesOnlyAdjustmentsUpToAndIncludingDate() {
        val contract = contract(
            adjustments = listOf(
                Adjustment(LocalDate.of(2025, 4, 1), BigDecimal("0.10")),
                Adjustment(LocalDate.of(2025, 7, 1), BigDecimal("0.20"))
            )
        )

        assertMoney("1000.00", contract.valueAfterDate(LocalDate.of(2025, 3, 31)))
        assertMoney("1100.00", contract.valueAfterDate(LocalDate.of(2025, 4, 1)))
        assertMoney("1320.00", contract.valueAfterDate(LocalDate.of(2025, 7, 1)))
    }

    @Test
    fun nullAdjustmentAmountLeavesRentUnchanged() {
        val contract = contract(
            adjustments = listOf(
                Adjustment(LocalDate.of(2025, 4, 1), null),
                Adjustment(LocalDate.of(2025, 7, 1), BigDecimal("0.10"))
            )
        )

        assertMoney("1100.00", contract.valueAfterDate(LocalDate.of(2025, 12, 1)))
    }

    @Test
    fun generateAdjustmentsUsesIntervalsStrictlyInsideContract() {
        val adjustments = Contract.generateAdjustments(start, finish, adjustmentTime = 3, hike = BigDecimal("0.15"))

        assertEquals(
            listOf(
                Adjustment(LocalDate.of(2025, 4, 1), BigDecimal("0.15")),
                Adjustment(LocalDate.of(2025, 7, 1), BigDecimal("0.15")),
                Adjustment(LocalDate.of(2025, 10, 1), BigDecimal("0.15"))
            ),
            adjustments
        )
    }

    @Test
    fun generateAdjustmentsReturnsEmptyWhenFirstIntervalReachesFinish() {
        assertEquals(emptyList<Adjustment>(), Contract.generateAdjustments(start, finish, adjustmentTime = 12))
    }

    private fun contract(adjustments: List<Adjustment>) = Contract(
        property = Property(Address("Main", "1", null), Owner("Owner")),
        tenant = Tenant("Tenant", "123"),
        rent = BigDecimal("1000.00"),
        startingDate = start,
        finishingDate = finish,
        adjustments = adjustments,
        services = emptyList()
    )

    private fun assertMoney(expected: String, actual: BigDecimal) {
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }
}
