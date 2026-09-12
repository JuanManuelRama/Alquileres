package com.example.myapplication.ui.contract.bill

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

internal val BillMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
internal val BillDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

internal fun BigDecimal.toCurrencyString(): String =
    setScale(2, RoundingMode.HALF_UP).toPlainString()
