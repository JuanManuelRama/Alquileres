package com.example.myapplication.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
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

/**
 * A [VisualTransformation] that formats numeric input strings (e.g., "1234.56" or "1234,56")
 * with locale-aware thousands grouping separators (e.g., "1,234.56" or "1.234,56").
 */
class NumberVisualTransformation(
    private val locale: Locale = Locale.getDefault()
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val symbols = DecimalFormatSymbols.getInstance(locale)
        val groupingSep = symbols.groupingSeparator
        val decimalSep = symbols.decimalSeparator
        val isNegative = raw.startsWith("-")

        val parts = raw.split('.', ',')
        val intPart = parts[0].removePrefix("-").filter { it.isDigit() }
        val fracPart = parts.getOrNull(1)

        val formatted = buildString {
            if (isNegative) append('-')

            intPart.forEachIndexed { index, digit ->
                val remainder = intPart.length - index
                if (index > 0 && remainder % 3 == 0) {
                    append(groupingSep)
                }
                append(digit)
            }

            if (parts.size > 1) {
                append(decimalSep)
                fracPart?.let { append(it) }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val originalSub = raw.take(offset)
                val isSubNegative = originalSub.startsWith("-")
                val digitsBefore = originalSub.count { it.isDigit() }
                val hasDecimal = originalSub.contains('.') || originalSub.contains(',')

                // Calculate groups added
                val groups = if (digitsBefore > 3) (digitsBefore - 1) / 3 else 0

                var transformedOffset = digitsBefore + groups
                if (isSubNegative) transformedOffset += 1
                if (hasDecimal) transformedOffset += 1 // for the decimal separator

                // Add characters belonging to the fractional part
                if (hasDecimal) {
                    val fracSub = originalSub.substringAfter('.').substringAfter(',')
                    transformedOffset += fracSub.length
                }

                return transformedOffset.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return formatted.take(offset).count { it.isDigit() || it == '-' || it == decimalSep }
                    .coerceAtMost(raw.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun rememberNumberVisualTransformation(locale: Locale = Locale.getDefault()): VisualTransformation {
    return remember(locale) { NumberVisualTransformation(locale) }
}
