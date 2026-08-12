package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate

data class Contract(
    val property: Property,
    val tenant: Tenant,
    val initialCost: BigDecimal,
    val currentCost: BigDecimal,
    val startingDate: LocalDate,
    val finishingDate: LocalDate,
    val adjustmentTime: Long,
) {
    fun adjustmentMonths(): List<LocalDate> {
        return generateSequence(startingDate.plusMonths(adjustmentTime)) { it.plusMonths(adjustmentTime) }
            .takeWhile { it.isBefore(finishingDate) }
            .toList()
    }
}
