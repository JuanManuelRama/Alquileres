package com.example.myapplication.message

import com.example.myapplication.model.Bill
import com.example.myapplication.ui.util.DateFormatters
import com.example.myapplication.ui.util.toMoneyWithCurrency
import java.util.Locale

/** Plain text shared by any message sender; presentation changes belong here. */
object BillMessageFormatter {
    fun format(
        bill: Bill,
        locale: Locale = Locale.getDefault(),
        numberLocale: Locale = locale,
    ): String {
        require(bill.isValid) { "Complete the bill before sending it" }
        val dates = DateFormatters(locale)
        return buildString {
            appendLine("Hello ${bill.contract.tenant.name},")
            appendLine()
            appendLine("Bill for ${dates.month(bill.month)}")
            appendLine("Property: ${bill.contract.property.address}")
            appendLine()
            appendLine("Rent: ${bill.rent.toMoneyWithCurrency(locale, numberLocale)}")
            bill.items.forEach { appendLine("${it.description}: ${it.amount!!.toMoneyWithCurrency(locale, numberLocale)}") }
            appendLine("Total: ${bill.total.toMoneyWithCurrency(locale, numberLocale)}")
            bill.paidAt?.let {
                appendLine("Paid on ${dates.date(it)}")
            }
        }.trimEnd()
    }
}
