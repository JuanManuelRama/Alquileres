package com.example.myapplication.ui.contract

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.ui.components.picker.PickerInputs
import com.example.myapplication.ui.contract.create.ContractCreator
import com.example.myapplication.ui.contract.create.ContractInputs
import com.example.myapplication.ui.owner.OwnerInputs
import com.example.myapplication.ui.property.PropertyInputs
import com.example.myapplication.ui.service.ServiceInputs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ContractDraftRestorationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun pickerQueryRestoresAndSelectingExistingPropertyKeepsIds() {
        val property = Property(Address("Main", "12", null), Owner("Owner", 41), 42)
        lateinit var current: ContractInputs
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            var inputs by rememberSerializable { mutableStateOf(ContractInputs()) }
            current = inputs
            MaterialTheme {
                ContractCreator(listOf(property), listOf(property.owner), emptyList(), {},
                    inputs, { inputs = it }, modifier = Modifier.verticalScroll(rememberScrollState()))
            }
        }
        composeRule.onAllNodes(hasSetTextAction())[0].performTextReplacement("Main")
        composeRule.runOnIdle { assertTrue(current != ContractInputs()) }
        restoration.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { assertEquals("Main", current.propertyPicker.query) }
        composeRule.onAllNodes(hasSetTextAction())[0].performClick()
        composeRule.onNodeWithText(property.toString()).performClick()
        composeRule.runOnIdle {
            assertEquals(property, current.selectedProperty)
            assertEquals(PickerInputs(), current.propertyPicker)
        }
        restoration.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle {
            assertEquals(42L, current.selectedProperty!!.id)
            assertEquals(41L, current.selectedProperty!!.owner.id)
        }
    }

    @Test fun nestedCreatorsRestoreTheirFieldsAndOpenState() {
        val draft = ContractInputs().copy(
            propertyPicker = PickerInputs(isCreating = true),
            propertyDraft = PropertyInputs(street = "Draft street", number = "27",
                ownerPicker = PickerInputs(isCreating = true), ownerDraft = OwnerInputs("Draft owner")),
            servicePicker = PickerInputs(isCreating = true), serviceDraft = ServiceInputs("Draft service")
        )
        lateinit var current: ContractInputs
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            var inputs by rememberSerializable { mutableStateOf(draft) }
            current = inputs
            MaterialTheme {
                ContractCreator(emptyList(), emptyList(), emptyList(), {}, inputs, { inputs = it },
                    modifier = Modifier.verticalScroll(rememberScrollState()))
            }
        }
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Draft street").assertExists()
        composeRule.onNodeWithText("Draft owner").assertExists()
        composeRule.onNodeWithText("Draft service").assertExists()
        composeRule.runOnIdle {
            assertEquals(draft, current)
            assertTrue(current != ContractInputs())
        }
    }
}
