package ar.controlalq.ui.util

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

fun BigDecimal.toMoneyWithCurrency(
    locale: Locale = Locale.getDefault(),
    numberLocale: Locale = locale,
): String =
    getCurrencySymbol(locale) + toMoney(numberLocale)

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
 * A [VisualTransformation] that formats valid numeric input strings (e.g., "1234.56" or "1234,56")
 * with locale-aware thousands grouping separators (e.g., "1,234.56" or "1.234,56").
 * If the input contains invalid non-numeric characters, multiple decimals, or misplaced signs,
 * the raw text is rendered as-is so nothing is hidden from the user.
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

        // Render raw text as-is if it contains invalid characters, multiple decimals, or misplaced minus
        val decimalCount = raw.count { it == '.' || it == ',' }
        val hasInvalidChars = raw.any { !it.isDigit() && it != '.' && it != ',' && it != '-' }
        val misplacedMinus = raw.lastIndexOf('-') > 0

        if (hasInvalidChars || decimalCount > 1 || misplacedMinus) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val isNegative = raw.startsWith("-")
        val cleanRaw = if (isNegative) raw.substring(1) else raw

        val decimalIndex = cleanRaw.indexOfFirst { it == '.' || it == ',' }
        val intPart = if (decimalIndex == -1) cleanRaw else cleanRaw.substring(0, decimalIndex)
        val fracPart = if (decimalIndex == -1) null else cleanRaw.substring(decimalIndex + 1)
        val typedDecimalSep = if (decimalIndex != -1) cleanRaw[decimalIndex] else null

        val formattedInt = buildString {
            intPart.forEachIndexed { index, digit ->
                val remainder = intPart.length - index
                if (index > 0 && remainder % 3 == 0) {
                    append(groupingSep)
                }
                append(digit)
            }
        }

        val formatted = buildString {
            if (isNegative) append('-')
            append(formattedInt)
            if (typedDecimalSep != null) {
                append(decimalSep)
                fracPart?.let { append(it) }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val sub = raw.take(offset.coerceIn(0, raw.length))
                val subIsNeg = sub.startsWith("-")
                val subHasDec = sub.contains('.') || sub.contains(',')

                val subIntDigits = if (subHasDec) {
                    val subClean = if (subIsNeg) sub.substring(1) else sub
                    subClean.substringBefore('.').substringBefore(',').count { it.isDigit() }
                } else sub.count { it.isDigit() }

                val groups = if (subIntDigits > 3) (subIntDigits - 1) / 3 else 0

                var transformedOffset = sub.count { it.isDigit() } + groups
                if (subIsNeg) transformedOffset += 1
                if (subHasDec) transformedOffset += 1

                return transformedOffset.coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val subFmt = formatted.take(offset.coerceIn(0, formatted.length))
                val subFmtDigits = subFmt.count { it.isDigit() }
                val subFmtIsNeg = subFmt.startsWith("-")
                val subFmtHasDec = subFmt.contains(decimalSep)

                var orig = subFmtDigits
                if (subFmtIsNeg) orig += 1
                if (subFmtHasDec) orig += 1
                return orig.coerceAtMost(raw.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun rememberNumberVisualTransformation(
    locale: Locale = LocalNumberLocale.current
): VisualTransformation {
    return remember(locale) { NumberVisualTransformation(locale) }
}
