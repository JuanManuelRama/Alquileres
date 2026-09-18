package com.example.myapplication.ui.components.form

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.test.platform.app.InstrumentationRegistry
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PhoneFormItemTest {
    @get:Rule val composeRule = createComposeRule()
    private val util = PhoneNumberUtil.createInstance(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test fun validatesLocalAndInternationalNumbers() {
        assertTrue(validatePhoneNumber("6411212616", "MX", util).isValid)
        assertFalse(validatePhoneNumber("6411212616", "US", util).isValid)
        assertTrue(validatePhoneNumber("+442079460018", "US", util).isValid)
        assertTrue(validatePhoneNumber("442079460018", "US", util).isValid)
        assertTrue(validatePhoneNumber("5491123456789", "AR", util).isValid)
        assertFalse(validatePhoneNumber("123", "MX", util).isValid)
        assertFalse(validatePhoneNumber("+", "MX", util).isValid)
        assertEquals("6411212616", phoneNumberDigits("(641) 121-2616"))
        assertEquals("442079460018", phoneNumberDigits("+44 20 7946 0018"))
    }

    @Test fun formatsEveryPrefixWithReversibleCursorPositions() {
        for (number in listOf("6502530000", "+442079460018", "442079460018", "5491123456789")) {
            for (length in 0..number.length) {
                val raw = number.take(length)
                val result = PhoneVisualTransformation(util, "US").filter(AnnotatedString(raw))
                assertEquals(phoneNumberDigits(raw), phoneNumberDigits(result.text.text))
                for (offset in 0..raw.length) {
                    val mapped = result.offsetMapping.originalToTransformed(offset)
                    assertTrue(mapped in 0..result.text.length)
                    assertEquals(offset, result.offsetMapping.transformedToOriginal(mapped))
                }
                for (offset in 0..result.text.length) {
                    assertTrue(result.offsetMapping.transformedToOriginal(offset) in 0..raw.length)
                }
            }
        }
        assertEquals("(650) 253-0000", PhoneVisualTransformation(util, "US")
            .filter(AnnotatedString("6502530000")).text.text)
    }

    @Test fun infersCountryCodeWithoutChangingLocalNumbersOrStoredDigits() {
        assertEquals("+442079460018", phoneNumberForParsing("442079460018", "US", util))
        assertEquals("+5491123456789", phoneNumberForParsing("5491123456789", "AR", util))
        assertEquals("6502530000", phoneNumberForParsing("6502530000", "US", util))
        assertEquals("123", phoneNumberForParsing("123", "US", util))
        val formatted = PhoneVisualTransformation(util, "US").filter(AnnotatedString("442079460018"))
        assertEquals("+44 20 7946 0018", formatted.text.text)
        assertEquals(0, formatted.offsetMapping.transformedToOriginal(0))
        assertEquals(0, formatted.offsetMapping.transformedToOriginal(1))
        assertEquals(Validation.Valid("442079460018"), validatePhoneNumber("442079460018", "US", util))
    }

    @Test fun invalidInputBlocksSubmitAndFormattedPasteKeepsRawDigits() {
        var phone by mutableStateOf("")
        composeRule.setContent {
            MaterialTheme {
                Form("Save", {}) {
                    PhoneItem("Phone", phone, region = "US", onValueChange = { phone = it })
                }
            }
        }
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.onNodeWithText("Phone *").performTextReplacement("123")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.onNodeWithText("Phone *").performTextReplacement("(650) 253-0000")
        composeRule.onNodeWithText("Save").assertIsEnabled()
        composeRule.runOnIdle { assertEquals("6502530000", phone) }
        composeRule.onNodeWithText("Phone *").performTextReplacement("442079460018")
        composeRule.onNodeWithText("Save").assertIsEnabled()
        composeRule.runOnIdle { assertEquals("442079460018", phone) }
        composeRule.onNodeWithText("Phone *").performTextReplacement("")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }
}
