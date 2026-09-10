package com.example.myapplication.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun yearMonthRoundTripsWithoutADayAndPreservesNull() {
        val month = YearMonth.of(2026, 12)
        assertEquals("2026-12", converters.monthToString(month))
        assertEquals(month, converters.monthFromString(converters.monthToString(month)))
        assertNull(converters.monthFromString(null))
        assertNull(converters.monthToString(null))
    }

    @Test
    fun localDateRoundTripsAndPreservesNull() {
        val date = LocalDate.of(2026, 8, 31)

        assertEquals(date, converters.fromTimestamp(converters.dateToTimestamp(date)))
        assertNull(converters.fromTimestamp(null))
        assertNull(converters.dateToTimestamp(null))
    }

    @Test
    fun bigDecimalRoundTripsAndRejectsMalformedInput() {
        val amount = BigDecimal("1234.5600")

        assertEquals(amount, converters.fromBigDecimal(converters.bigDecimalToString(amount)))
        assertNull(converters.fromBigDecimal("not-decimal"))
        assertNull(converters.fromBigDecimal(null))
        assertNull(converters.bigDecimalToString(null))
    }
}
