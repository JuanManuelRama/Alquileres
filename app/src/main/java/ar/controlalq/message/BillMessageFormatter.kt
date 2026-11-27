package ar.controlalq.message

import android.content.Context
import ar.controlalq.R
import ar.controlalq.model.Bill
import ar.controlalq.ui.util.DateFormatters
import ar.controlalq.ui.util.toMoneyWithCurrency
import java.util.Locale

/** Plain text shared by any message sender; presentation changes belong here. */
object BillMessageFormatter {
    fun format(
        context: Context,
        bill: Bill,
        locale: Locale = Locale.getDefault(),
        numberLocale: Locale = locale,
    ): String = buildString {
        appendLine(context.getString(R.string.msg_hello_tenant, bill.contract.tenant.name))
        appendLine()
        appendLine(context.getString(R.string.msg_bill_for_month, DateFormatters(locale).month(bill.month)))
        appendLine()
        appendLine(context.getString(R.string.msg_rent_item, bill.rent.toMoneyWithCurrency(locale, numberLocale)))
        bill.items.forEach {
            appendLine("${it.description}: ${it.amount!!.toMoneyWithCurrency(locale, numberLocale)}")
        }
        appendLine(context.getString(R.string.msg_total_item, bill.total.toMoneyWithCurrency(locale, numberLocale)))
    }
}
