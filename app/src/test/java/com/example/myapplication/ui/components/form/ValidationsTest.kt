package com.example.myapplication.ui.components.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ValidationsTest {
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
            .isLong("invalid")
            .map("unused") { invoked = true; it }

        assertEquals(Validation.Invalid("invalid"), result)
        assertFalse(invoked)
    }

    @Test
    fun decimalAndPositiveValidatorsCompose() {
        assertEquals(Validation.Valid(BigDecimal("12.50")), Validate("12.50").isBigDecimal("must be a valid decimal number").isPositive("must be positive"))
        assertEquals(Validation.Invalid("must be positive"), Validate("0").isBigDecimal("must be a valid decimal number").isPositive("must be positive"))
        assertEquals(Validation.Invalid("must be a valid decimal number"), Validate("abc").isBigDecimal("must be a valid decimal number").isPositive("must be positive"))
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
