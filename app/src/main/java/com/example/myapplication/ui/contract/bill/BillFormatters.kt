package com.example.myapplication.ui.contract.bill

import com.example.myapplication.ui.util.toMoney
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

internal val BillMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

internal fun BigDecimal.toCurrencyString(): String = toMoney()
