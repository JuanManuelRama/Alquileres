package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

data class BillItem(
    val description: String,
    val amount: BigDecimal?
)

data class Bill(
    val id: Long,
    val contractId: Long,
    val month: YearMonth,
    val rent: BigDecimal,
    val items: List<BillItem>,
    val updatedAt: Long,
    val paidAt: LocalDate? = null
) {
    val isPaid: Boolean get() = paidAt != null

    val isValid: Boolean get() =
        items.all { it.amount != null }

    val total: BigDecimal get() =
        rent + items.sumOf { it.amount ?: BigDecimal.ZERO }
}
