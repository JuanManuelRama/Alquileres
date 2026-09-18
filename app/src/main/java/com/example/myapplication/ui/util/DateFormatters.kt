
package com.example.myapplication.ui.util

import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** Display formats only; persisted dates keep their existing ISO representation. */
class DateFormatters(val locale: Locale) {
    private val monthFormat = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    private val shortMonthFormat = DateTimeFormatter.ofPattern("MMM yyyy", locale)
    private val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    private val shortDateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)

    fun month(value: YearMonth): String = value.format(monthFormat)
    fun shortMonth(value: YearMonth): String = value.format(shortMonthFormat)
    fun date(value: LocalDate): String = value.format(dateFormat)
    fun shortDate(value: LocalDate): String = value.format(shortDateFormat)
    fun monthName(value: Month): String = value.getDisplayName(TextStyle.SHORT, locale)
}
