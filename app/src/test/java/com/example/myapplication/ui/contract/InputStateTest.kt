package com.example.myapplication.ui.contract

import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.ui.contract.create.ContractInputs
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.example.myapplication.ui.components.picker.PickerInputs
import com.example.myapplication.ui.property.PropertyInputs
import com.example.myapplication.ui.owner.OwnerInputs
import com.example.myapplication.ui.service.ServiceInputs
import org.junit.Test
import java.time.YearMonth

class InputStateTest {
    @Test
    fun pickerDraftsAndOpenCreatorsDifferFromDefaults() {
        val empty = ContractInputs()
        assertEquals(ContractInputs(), empty)
        assertTrue(empty.copy(propertyPicker = PickerInputs(isCreating = true),
            propertyDraft = PropertyInputs(ownerPicker = PickerInputs(isCreating = true)),
            servicePicker = PickerInputs(isCreating = true)) != ContractInputs())
        assertTrue(empty.copy(propertyPicker = PickerInputs(query = "Street")) != ContractInputs())
        assertTrue(empty.copy(servicePicker = PickerInputs(query = "Water")) != ContractInputs())
        assertTrue(empty.copy(propertyDraft = PropertyInputs(street = "Street")) != ContractInputs())
        assertTrue(empty.copy(propertyDraft = PropertyInputs(ownerPicker = PickerInputs(query = "Owner"))) != ContractInputs())
        assertTrue(empty.copy(propertyDraft = PropertyInputs(ownerDraft = OwnerInputs("Owner"))) != ContractInputs())
        assertTrue(empty.copy(serviceDraft = ServiceInputs("Water")) != ContractInputs())
    }

    @Test
    fun nestedDraftsAndSelectedEntityIdsSurviveSerialization() {
        val owner = Owner("Existing owner", 41)
        val property = Property(Address("Street", "12", "A"), owner, 42)
        val inputs = ContractInputs().copy(
            selectedProperty = property,
            selectedServices = listOf(Service("Water", 43)),
            propertyPicker = PickerInputs("Search", true),
            propertyDraft = PropertyInputs("Draft street", "9", "B", owner,
                PickerInputs("Owner query", true), OwnerInputs("Draft owner")),
            servicePicker = PickerInputs("Service query", true),
            serviceDraft = ServiceInputs("Draft service")
        )
        val restored = Json.decodeFromString<ContractInputs>(Json.encodeToString(inputs))
        assertEquals(inputs, restored)
        assertEquals(42L, restored.selectedProperty!!.id)
        assertEquals(41L, restored.selectedProperty.owner.id)
        assertEquals(41L, restored.propertyDraft.selectedOwner!!.id)
        assertEquals(43L, restored.selectedServices.single().id)
        assertTrue(restored != ContractInputs())
    }

    @Test
    fun contractInputsSerializeDefaultsAndAllNestedValues() {
        val defaults = ContractInputs()
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
        val inputs = ContractInputs().copy(tenantName = "Before", rent = "100")
        val edited = inputs.copy(tenantName = "After", rent = "55.")

        assertEquals("Before", inputs.tenantName)
        assertEquals("100", inputs.rent)
        assertEquals("After", edited.tenantName)
        assertEquals("55.", edited.rent)
        assertEquals(inputs, edited.copy(tenantName = "Before", rent = "100"))
    }

}
