package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate

data class Contract(
    val property: Property,
    val tenant: Tenant,
    val rent: BigDecimal,
    val startingDate: LocalDate,
    val finishingDate: LocalDate,
    val adjustments: List<Adjustment>,
    val services: List<Service>
) {
    fun currentValue(): BigDecimal = valueAfterDate(LocalDate.now())

    fun valueAfterDate(date: LocalDate): BigDecimal {
        return adjustments.filter { it.time.isBefore(date) || it.time.isEqual(date) }
            .fold(rent) { rent, adjustment ->
                adjustment.amount?.let {
                    rent + rent * it
                } ?: rent
        }
    }

    companion object {
        fun generateAdjustments(
            startingDate: LocalDate,
            finishingDate: LocalDate,
            adjustmentTime: Long,
            hike: BigDecimal? = null
        ): List<Adjustment> {
            return generateSequence(startingDate.plusMonths(adjustmentTime)) { it.plusMonths(adjustmentTime) }
                .takeWhile { it.isBefore(finishingDate) }
                .map { Adjustment(it, hike) }
                .toList()
        }
    }
}
