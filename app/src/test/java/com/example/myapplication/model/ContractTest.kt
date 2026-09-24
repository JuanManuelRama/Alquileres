package com.example.myapplication.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractTest {
    private val start = YearMonth.of(2025, 1)
    private val finish = YearMonth.of(2026, 1)

    @Test
    fun valueAtMonthAppliesOnlyAdjustmentsUpToAndIncludingMonth() {
        val contract = contract(
            adjustments = listOf(
                Adjustment(YearMonth.of(2025, 4), BigDecimal("0.10")),
                Adjustment(YearMonth.of(2025, 7), BigDecimal("0.20"))
            )
        )

        assertMoney("1000.00", contract.valueAtMonth(YearMonth.of(2025, 3)))
        assertMoney("1100.00", contract.valueAtMonth(YearMonth.of(2025, 4)))
        assertMoney("1320.00", contract.valueAtMonth(YearMonth.of(2025, 7)))
    }

    @Test
    fun nullAdjustmentAmountLeavesRentUnchanged() {
        val contract = contract(
            adjustments = listOf(
                Adjustment(YearMonth.of(2025, 4), null),
                Adjustment(YearMonth.of(2025, 7), BigDecimal("0.10"))
            )
        )

        assertMoney("1100.00", contract.valueAtMonth(YearMonth.of(2025, 12)))
    }

    @Test
    fun generateAdjustmentsUsesIntervalsStrictlyInsideContract() {
        val adjustments = Contract.generateAdjustments(start, finish, adjustmentTime = 3, hike = BigDecimal("0.15"))

        assertEquals(
            listOf(
                Adjustment(YearMonth.of(2025, 4), BigDecimal("0.15"), frozen = true),
                Adjustment(YearMonth.of(2025, 7), BigDecimal("0.15"), frozen = true),
                Adjustment(YearMonth.of(2025, 10), BigDecimal("0.15"), frozen = true)
            ),
            adjustments
        )
    }

    @Test
    fun variableAdjustmentsStartUnfrozen() {
        val adjustments = Contract.generateAdjustments(start, finish, adjustmentTime = 3)
        assertEquals(3, adjustments.size)
        assertEquals(true, adjustments.all { !it.frozen && it.amount == null })
    }

    @Test
    fun generateAdjustmentsReturnsEmptyWhenFirstIntervalReachesFinish() {
        assertEquals(emptyList<Adjustment>(), Contract.generateAdjustments(start, finish, adjustmentTime = 12))
    }

    @Test
    fun billsCoverEveryContractMonthWithScheduledRent() {
        val bills = contract(listOf(Adjustment(YearMonth.of(2025, 4), BigDecimal("0.10"))))
            .copy(id = 42, startingMonth = YearMonth.of(2025, 1)).createBills(123)
        assertEquals(13, bills.size)
        assertEquals(YearMonth.of(2025, 1), bills.first().month)
        assertEquals(YearMonth.of(2026, 1), bills.last().month)
        assertEquals(bills.size, bills.map { it.month }.distinct().size)
        assertEquals(42L, bills.last().contract.id)
        assertMoney("1000", bills[2].rent)
        assertMoney("1100", bills[3].rent)
    }

    @Test
    fun singleMonthContractCreatesOneBill() {
        val bills = contract(emptyList()).copy(finishingMonth = start).createBills()
        assertEquals(listOf(start), bills.map { it.month })
    }

    private fun contract(adjustments: List<Adjustment>) = Contract(
        property = Property(Address("Main", "1", null), Owner("Owner")),
        tenant = Tenant("Tenant", "123"),
        rent = BigDecimal("1000.00"),
        startingMonth = start,
        finishingMonth = finish,
        adjustments = adjustments,
        services = emptyList()
    )

    private fun assertMoney(expected: String, actual: BigDecimal) {
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }
}
