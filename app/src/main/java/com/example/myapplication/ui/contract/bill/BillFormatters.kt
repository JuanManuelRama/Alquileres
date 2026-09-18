package com.example.myapplication.ui.contract.bill

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal val BillMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/**
 * Formats a [LocalDate] for displaying the "Paid on" status on a bill.
 * Uses [FormatStyle.MEDIUM] for locale-aware representation (e.g., "Jan 15, 2026" / "15 ene 2026").
 */
internal fun LocalDate.formatPaidAtDate(locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

/**
 * Formats a [LocalDate] for displaying the selected date in form date inputs.
 * Uses [FormatStyle.SHORT] for locale-aware short representation (e.g., "1/15/26" / "15/01/2026").
 */
internal fun LocalDate.formatSelectedDate(locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale))

internal fun BigDecimal.toCurrencyString(): String =
    setScale(2, RoundingMode.HALF_UP).toPlainString()
