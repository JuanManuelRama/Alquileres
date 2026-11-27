package ar.controlalq.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AddressTest {
    @Test
    fun addressOmitsMissingUnitAndIncludesPresentUnit() {
        assertEquals("Oak 430", Address("Oak", "430", null).toString())
        assertEquals("Oak 430 B", Address("Oak", "430", "B").toString())
    }

    @Test
    fun propertyUsesItsAddressAsDisplayText() {
        val property = Property(Address("Oak", "430", "B"), Owner("Ada"))

        assertEquals("Oak 430 B", property.toString())
    }

}
