package ar.controlalq.ui.components.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ValidationsTest {
    @Test
    fun commaDecimalsUseTheSameValidationRules() {
        assertEquals(Validation.Valid(BigDecimal("12.50")),
            Validate("12,50").isBigDecimal(1).isPositive(2).limitDecimals(2, 3))
        assertFalse(Validate("12,501").isBigDecimal(1).limitDecimals(2, 3).isValid)
        assertFalse(Validate("-12,50").isBigDecimal(1).isPositive(2).isValid)
        listOf("1,2,3", "1.234,50", "1,234.50", ",", "abc").forEach {
            assertFalse(Validate(it).isBigDecimal(1).isValid)
        }
    }

    val bigDecimalValidation = 1
    val positiveValidation = 2
    val invalidCharsValidation = 3
    val isLongValidation = 4
    @Test
    fun mapTransformsValidValue() {
        assertEquals(Validation.Valid(3), Validate("abc").map("bad") { it.length })
    }

    @Test
    fun mapTurnsNullIntoProvidedError() {
        assertEquals(Validation.Invalid("not a number"), Validate("abc").map("not a number") { it.toLongOrNull() })
    }

    @Test
    fun invalidResultShortCircuitsLaterOperations() {
        var invoked = false
        val result = Validate("bad")
            .isLong(isLongValidation)
            .map("unused") { invoked = true; it }

        assertEquals(Validation.Invalid(isLongValidation), result)
        assertFalse(invoked)
    }

    @Test
    fun decimalAndPositiveValidatorsCompose() {
        assertEquals(Validation.Valid(BigDecimal("12.50")), Validate("12.50").isBigDecimal(bigDecimalValidation).isPositive(positiveValidation))
        assertEquals(Validation.Invalid(positiveValidation), Validate("0").isBigDecimal(bigDecimalValidation).isPositive(positiveValidation))
        assertEquals(Validation.Invalid(bigDecimalValidation), Validate("abc").isBigDecimal(bigDecimalValidation).isPositive(positiveValidation))
    }

    @Test
    fun noInvalidCharsRejectsLineSkipsTabsAndControlChars() {
        assertEquals(Validation.Valid("John Doe"), Validate("John Doe").noInvalidChars(invalidCharsValidation))
        assertEquals(Validation.Invalid(invalidCharsValidation), Validate("Line 1\nLine 2").noInvalidChars(invalidCharsValidation))
        assertEquals(Validation.Invalid(invalidCharsValidation), Validate("Tab\tSpace").noInvalidChars(invalidCharsValidation))
        assertEquals(Validation.Invalid(invalidCharsValidation), Validate("Carriage\rReturn").noInvalidChars(invalidCharsValidation))
        assertEquals(Validation.Invalid(invalidCharsValidation), Validate("Zero\u200BWidth").noInvalidChars(invalidCharsValidation))
    }

    @Test
    fun foldAndIsValidExposeBothOutcomes() {
        val valid = Validate(7)
        val invalid: Validation<Int> = Validation.Invalid("broken")

        assertTrue(valid.isValid)
        assertFalse(invalid.isValid)
        assertEquals("value=7", valid.fold({ "value=$it" }, { "error=$it" }))
        assertEquals("error=broken", invalid.fold({ "value=$it" }, { "error=$it" }))
    }
}
