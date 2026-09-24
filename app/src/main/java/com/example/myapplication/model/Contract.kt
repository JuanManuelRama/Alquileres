package com.example.myapplication.model

import java.math.BigDecimal
import java.time.YearMonth
import java.time.temporal.ChronoUnit

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
    val valueMonths: List<BigDecimal> by lazy {
        val totalMonths = ChronoUnit.MONTHS.between(startingMonth, finishingMonth).toInt() + 1
        var currentMonth = startingMonth
        var currentRent = rent
        List(totalMonths) { _ ->
            adjustments.find { it.time == currentMonth }?.amount?.let {
                currentRent = currentRent.add(currentRent.multiply(it))
            }
            currentMonth = currentMonth.plusMonths(1.toLong())
            currentRent
        }
    }

    fun createBills(updatedAt: Long = System.currentTimeMillis()): List<Bill> =
        generateSequence(startingMonth) { it.plusMonths(1) }
            .takeWhile { it <= finishingMonth }
            .mapIndexed { i, month ->
                Bill(
                    id = 0,
                    contract = this,
                    month = month,
                    rent = valueAtIndex(i),
                    items = services.map { BillItem(it.name, null) },
                    updatedAt = updatedAt
                )
            }.toList()

    fun currentValue(): BigDecimal = valueAtMonth(YearMonth.now().coerceIn(startingMonth, finishingMonth))

    fun valueAtMonth(month: YearMonth): BigDecimal =
        valueMonths[ChronoUnit.MONTHS.between(startingMonth, month).toInt()]

    fun valueAtIndex(index: Int): BigDecimal = valueMonths[index]


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
                .map { Adjustment(
                    time = it,
                    amount = hike,
                    frozen = hike != null
                ) }
                .toList()
    }
}
