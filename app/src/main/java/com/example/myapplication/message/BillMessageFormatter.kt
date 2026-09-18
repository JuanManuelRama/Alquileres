package com.example.myapplication.message

import com.example.myapplication.model.Bill
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Plain text shared by any message sender; presentation changes belong here. */
object BillMessageFormatter {
    fun format(bill: Bill, locale: Locale = Locale.getDefault()): String {
        require(bill.isValid) { "Complete the bill before sending it" }
        return buildString {
            appendLine("Hello ${bill.contract.tenant.name},")
            appendLine()
            appendLine("Bill for ${bill.month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))}")
            appendLine("Property: ${bill.contract.property.address}")
            appendLine()
            appendLine("Rent: $${bill.rent.money()}")
            bill.items.forEach { appendLine("${it.description}: $${checkNotNull(it.amount).money()}") }
            appendLine("Total: $${bill.total.money()}")
            bill.paidAt?.let {
                appendLine("Paid on ${it.format(DateTimeFormatter.ofPattern("dd MMM yyyy", locale))}")
            }
        }.trimEnd()
    }

    private fun BigDecimal.money(): String = setScale(2, RoundingMode.HALF_UP).toPlainString()
}
