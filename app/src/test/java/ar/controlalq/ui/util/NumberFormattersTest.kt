package ar.controlalq.ui.util

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class NumberFormattersTest {

    @Test
    fun explicitCurrencyLocaleOverridesTheDefaultLocale() {
        val original = Locale.getDefault()
        val originalDisplay = Locale.getDefault(Locale.Category.DISPLAY)
        val originalFormat = Locale.getDefault(Locale.Category.FORMAT)
        try {
            Locale.setDefault(Locale.forLanguageTag("es-AR"))
            assertEquals("$", getCurrencySymbol(Locale.US))
            assertEquals("$1,234.5", BigDecimal("1234.50").toMoneyWithCurrency(Locale.US))
            assertEquals("$-10.25", BigDecimal("-10.25").toMoneyWithCurrency(Locale.US))
        } finally {
            Locale.setDefault(original)
            Locale.setDefault(Locale.Category.DISPLAY, originalDisplay)
            Locale.setDefault(Locale.Category.FORMAT, originalFormat)
        }
    }

    @Test
    fun toMoneyFormatsWithTwoDecimalsAndLocaleGrouping() {
        val amount = BigDecimal("1234.5")

        assertEquals("1,234.5", amount.toMoney(Locale.US))
        assertEquals("1.234,5", amount.toMoney(Locale.forLanguageTag("es")))
    }

    @Test
    fun toMoneyTruncatesWithoutRoundingUp() {
        val amount = BigDecimal("1234.5679")

        assertEquals("1,234.56", amount.toMoney(Locale.US))
        assertEquals("1.234,56", amount.toMoney(Locale.forLanguageTag("es")))
    }

    @Test
    fun toPercentageTruncatesToMaxFourDecimalsAndStripsTrailingZeroes() {
        val val1 = BigDecimal("12.5000")
        val val2 = BigDecimal("12.0000")
        val val3 = BigDecimal("12.345679")

        assertEquals("12.5", val1.toPercentage(Locale.US))
        assertEquals("12", val2.toPercentage(Locale.US))
        assertEquals("12.3456", val3.toPercentage(Locale.US))

        assertEquals("12,5", val1.toPercentage(Locale.forLanguageTag("es")))
        assertEquals("12,3456", val3.toPercentage(Locale.forLanguageTag("es")))
    }

    @Test
    fun numberVisualTransformationFormatsCleanNumbersAndPreservesInvalidTextUnchanged() {
        val transformationUS = NumberVisualTransformation(Locale.US)

        // Valid numbers get formatted with thousands separators
        assertEquals("1,234,567.89", transformationUS.filter(AnnotatedString("1234567.89")).text.text)

        // Invalid numbers with letters or multiple decimals are rendered as-is so nothing is hidden
        assertEquals("123a456", transformationUS.filter(androidx.compose.ui.text.AnnotatedString("123a456")).text.text)
        assertEquals("1.2.3", transformationUS.filter(androidx.compose.ui.text.AnnotatedString("1.2.3")).text.text)
    }
}
