package com.example.myapplication.model

import java.math.BigDecimal
import java.time.YearMonth

data class BillDraftService(
    val serviceId: Long,
    val name: String,
    val amountInput: String = ""
)

data class BillDraft(
    val id: Long,
    val contractId: Long,
    val month: YearMonth,
    val rentInput: String,
    val services: List<BillDraftService>,
    val updatedAt: Long
) {
    val isValid: Boolean get() = rentInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true &&
        services.all { it.amountInput.toBigDecimalOrNull() != null }

    val total: BigDecimal get() =
        rentInput.toBigDecimal() + services.sumOf { it.amountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO }
}
