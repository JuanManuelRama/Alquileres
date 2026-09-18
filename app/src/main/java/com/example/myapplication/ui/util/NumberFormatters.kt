package com.example.myapplication.ui.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Returns the currency symbol for the user's device region/country (e.g. "$", "€", "£").
 */
fun getCurrencySymbol(locale: Locale = Locale.getDefault()): String =
    try {
        Currency.getInstance(locale).getSymbol(locale)
    } catch (_: Exception) {
        "$"
    }

/**
 * Formats a [BigDecimal] as money with 2 decimal places, locale-aware separators, and currency symbol
 * (e.g. "$1,234.50" / "$1.234,50" / "€1.234,50").
 * Truncates extra decimals beyond 2 places without rounding up.
 */
fun BigDecimal.toMoney(locale: Locale = Locale.getDefault()): String {
    val truncated = setScale(2, RoundingMode.DOWN).stripTrailingZeros()
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
        isGroupingUsed = true
    }
    return formatter.format(truncated)
}

fun BigDecimal.toMoneyWithCurrency(locale: Locale = Locale.getDefault()): String =
    "${getCurrencySymbol(locale)} ${toMoney(locale)}"

/**
 * Formats a [BigDecimal] as a percentage value, truncating to max 4 decimal places (without rounding up),
 * stripping trailing zeroes, and using locale-aware separators (e.g. "12.5" / "12,5").
 */
fun BigDecimal.toPercentage(locale: Locale = Locale.getDefault()): String {
    val truncated = setScale(4, RoundingMode.DOWN).stripTrailingZeros()
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 4
        isGroupingUsed = true
    }
    return formatter.format(truncated)
}
