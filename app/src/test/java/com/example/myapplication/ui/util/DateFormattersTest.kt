package com.example.myapplication.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.util.Locale

class DateFormattersTest {
    @Test fun formatsEveryDateTypeInTheRequestedLanguage() {
        val english = DateFormatters(Locale.US)
        val spanish = DateFormatters(Locale.forLanguageTag("es-ES"))
        val month = YearMonth.of(2026, 1)
        assertEquals("January 2026", english.month(month))
        assertEquals("enero 2026", spanish.month(month))
        assertEquals("Jan 2026", english.shortMonth(month))
        assertEquals("ene 2026", spanish.shortMonth(month))
        assertEquals("Jan", english.monthName(Month.JANUARY))
        assertEquals("ene", spanish.monthName(Month.JANUARY))
        val date = LocalDate.of(2026, 1, 15)
        assertNotEquals(english.date(date), spanish.date(date))
        assertNotEquals(english.shortDate(date), spanish.shortDate(date))
    }

    @Test fun dateOrderFollowsTheLocaleRegion() {
        val date = LocalDate.of(2026, 1, 15)
        assertEquals("1/15/26", DateFormatters(Locale.US).shortDate(date))
        assertEquals("15/01/2026", DateFormatters(Locale.UK).shortDate(date))
    }
}
