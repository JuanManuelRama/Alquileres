package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate

data class Contract(
    val property: Property,
    val tenant: Tenant,
    val initialRent: BigDecimal,
    val currentRent: BigDecimal,
    val startingDate: LocalDate,
    val finishingDate: LocalDate,
    val adjustmentTime: Long,
    val services: List<Service>
) {
    fun adjustmentMonths(): List<LocalDate> {
        return generateSequence(startingDate.plusMonths(adjustmentTime)) { it.plusMonths(adjustmentTime) }
            .takeWhile { it.isBefore(finishingDate) }
            .toList()
    }
}
