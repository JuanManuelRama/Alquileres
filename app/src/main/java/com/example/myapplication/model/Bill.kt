package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

data class BillItem(
    val description: String,
    val amount: String = ""
)

data class Bill(
    val id: Long,
    val contractId: Long,
    val month: YearMonth,
    val rentInput: String,
    val items: List<BillItem>,
    val updatedAt: Long,
    val paidAt: LocalDate? = null
) {
    val isPaid: Boolean get() = paidAt != null

    val isValid: Boolean get() = rentInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true &&
        items.all { it.amount.toBigDecimalOrNull() != null }

    val total: BigDecimal get() =
        rentInput.toBigDecimal() + items.sumOf { it.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO }
}
