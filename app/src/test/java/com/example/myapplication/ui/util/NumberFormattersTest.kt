package com.example.myapplication.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class NumberFormattersTest {

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
}
