package com.example.myapplication.ui.contract.list

import com.example.myapplication.model.Contract
import java.time.YearMonth

data class ContractListItem(
    val contract: Contract,
    val paymentDue: Boolean,
    val expiresSoon: Boolean,
    val hikeThisMonth: Boolean
)

internal fun Contract.listItem(month: YearMonth, paymentDue: Boolean, hikeThisMonth: Boolean): ContractListItem {
    val expiryMonth = YearMonth.from(finishingDate)
    return ContractListItem(
        contract = this,
        paymentDue = paymentDue,
        expiresSoon = expiryMonth == month || expiryMonth == month.plusMonths(1),
        hikeThisMonth = hikeThisMonth
    )
}
