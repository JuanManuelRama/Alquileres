package ar.controlalq.message

import androidx.test.platform.app.InstrumentationRegistry
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MessageSenderTest {
    private val util = PhoneNumberUtil.createInstance(InstrumentationRegistry.getInstrumentation().targetContext)

    @Test fun whatsappUsesInternationalDigitsForLocalAndCountryPrefixedNumbers() {
        assertEquals("16502530000", whatsappPhoneNumber("6502530000", "US", util))
        assertEquals("442079460018", whatsappPhoneNumber("442079460018", "US", util))
        assertEquals("5491123456789", whatsappPhoneNumber("+54 9 11 2345-6789", "AR", util))
        assertNull(whatsappPhoneNumber("123", "US", util))
        assertNull(whatsappPhoneNumber("", "US", util))
    }
}
