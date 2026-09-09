package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

data class Contract(
    val property: Property,
    val tenant: Tenant,
    val rent: BigDecimal,
    val startingDate: LocalDate,
    val finishingDate: LocalDate,
    val adjustments: List<Adjustment>,
    val services: List<Service>,
    val id: Long = 0L
) {
    // Match the inclusive month boundaries used by payment status.
    fun createBills(updatedAt: Long = System.currentTimeMillis()): List<BillDraft> =
        generateSequence(YearMonth.from(startingDate)) { it.plusMonths(1) }
            .takeWhile { it <= YearMonth.from(finishingDate) }
            .map { month ->
                BillDraft(0, id, month, valueAfterDate(month.atDay(1)).toPlainString(),
                    services.map { BillDraftService(it.id, it.name) }, updatedAt)
            }.toList()

    fun currentValue(): BigDecimal = valueAfterDate(LocalDate.now())

    fun valueAfterDate(date: LocalDate): BigDecimal {
        return adjustments.filter { it.time.isBefore(date) || it.time.isEqual(date) }
            .fold(rent) { currentRent, adjustment ->
                adjustment.amount?.let { hike ->
                    currentRent.add(currentRent.multiply(hike))
                } ?: currentRent
        }
    }

    companion object {
        fun generateAdjustments(
            startingDate: LocalDate,
            finishingDate: LocalDate,
            adjustmentTime: Long? = null,
            hike: BigDecimal? = null
        ): List<Adjustment> =
            if (adjustmentTime == null) emptyList()
            else generateSequence(startingDate.plusMonths(adjustmentTime))
            { it.plusMonths(adjustmentTime) }
                .takeWhile { it.isBefore(finishingDate) }
                .map { Adjustment(it, hike) }
                .toList()
    }
}
