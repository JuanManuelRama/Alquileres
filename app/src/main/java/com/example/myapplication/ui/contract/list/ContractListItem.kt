package com.example.myapplication.ui.contract.list

import com.example.myapplication.model.Contract
import java.time.YearMonth

data class ContractListItem(
    val contract: Contract,
    val paymentDue: Boolean,
    val expiresSoon: Boolean,
    val hikeThisMonth: Boolean
)

internal fun Contract.listItem(month: YearMonth, paidContractIds: Set<Long>): ContractListItem {
    val overlapsMonth = !startingDate.isAfter(month.atEndOfMonth()) &&
        !finishingDate.isBefore(month.atDay(1))
    val expiryMonth = YearMonth.from(finishingDate)
    return ContractListItem(
        contract = this,
        paymentDue = overlapsMonth && id !in paidContractIds,
        expiresSoon = expiryMonth == month || expiryMonth == month.plusMonths(1),
        hikeThisMonth = adjustments.any { YearMonth.from(it.time) == month }
    )
}
