package ar.controlalq.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class DecimalInputTest {
    @Test fun acceptsBothSeparatorsAndPreservesPrecision() {
        listOf("1234,50", "1234.50", "-0,25", ",5", "12,", "12345678901234567890,01").forEach { input ->
            val expected = BigDecimal(input.replace(',', '.'))
            assertEquals(expected, input.toInputBigDecimalOrNull())
            assertEquals(expected, input.toInputBigDecimal())
        }
    }

    @Test fun rejectsIncompleteAndAmbiguousInput() {
        listOf("", "-", ",", "1,2,3", "1.234,50", "1,234.50", "12abc").forEach {
            assertNull(it.toInputBigDecimalOrNull())
        }
    }
}
