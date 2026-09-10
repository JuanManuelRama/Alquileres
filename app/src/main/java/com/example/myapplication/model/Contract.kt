package com.example.myapplication.model

import java.math.BigDecimal
import java.time.YearMonth

data class Contract(
    val property: Property,
    val tenant: Tenant,
    val rent: BigDecimal,
    val startingMonth: YearMonth,
    val finishingMonth: YearMonth,
    val adjustments: List<Adjustment>,
    val services: List<Service>,
    val id: Long = 0L
) {
    // Match the inclusive month boundaries used by payment status.
    fun createBills(updatedAt: Long = System.currentTimeMillis()): List<Bill> =
        generateSequence(startingMonth) { it.plusMonths(1) }
            .takeWhile { it <= finishingMonth }
            .map { month ->
                Bill(0, id, month, valueAtMonth(month).toPlainString(),
                    services.map { BillItem(it.name) }, updatedAt)
            }.toList()

    fun currentValue(): BigDecimal = valueAtMonth(YearMonth.now())

    fun valueAtMonth(month: YearMonth): BigDecimal {
        return adjustments.filter { it.time <= month }
            .fold(rent) { currentRent, adjustment ->
                adjustment.amount?.let { hike ->
                    currentRent.add(currentRent.multiply(hike))
                } ?: currentRent
        }
    }

    companion object {
        fun generateAdjustments(
            startingMonth: YearMonth,
            finishingMonth: YearMonth,
            adjustmentTime: Long? = null,
            hike: BigDecimal? = null
        ): List<Adjustment> =
            if (adjustmentTime == null) emptyList()
            else generateSequence(startingMonth.plusMonths(adjustmentTime))
            { it.plusMonths(adjustmentTime) }
                .takeWhile { it.isBefore(finishingMonth) }
                .map { Adjustment(it, hike) }
                .toList()
    }
}
