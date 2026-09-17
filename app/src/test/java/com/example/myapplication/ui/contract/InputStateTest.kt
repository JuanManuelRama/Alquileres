package com.example.myapplication.ui.contract

import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.ui.contract.create.ContractInputs
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class InputStateTest {
    @Test
    fun contractInputsSerializeDefaultsAndAllNestedValues() {
        val defaults = ContractInputs.default()
        assertEquals(defaults, Json.decodeFromString<ContractInputs>(Json.encodeToString(defaults)))
        val inputs = defaults.copy(
            tenantName = "Tenant", tenantPhone = "123", rent = "55.", fixedHike = "10",
            adjustmentTime = "6", startMonth = YearMonth.of(2026, 9), endMonth = YearMonth.of(2027, 9),
            selectedProperty = Property(Address("Street", "12", "A"), Owner("Owner", 1), 2),
            selectedServices = listOf(Service("Water", 3))
        )
        assertEquals(inputs, Json.decodeFromString<ContractInputs>(Json.encodeToString(inputs)))
    }

    @Test
    fun contractEditsLeavePreviousInputsUnchanged() {
        val inputs = ContractInputs.default().copy(tenantName = "Before", rent = "100")
        val edited = inputs.copy(tenantName = "After", rent = "55.")

        assertEquals("Before", inputs.tenantName)
        assertEquals("100", inputs.rent)
        assertEquals("After", edited.tenantName)
        assertEquals("55.", edited.rent)
        assertEquals(inputs, edited.copy(tenantName = "Before", rent = "100"))
    }

}
