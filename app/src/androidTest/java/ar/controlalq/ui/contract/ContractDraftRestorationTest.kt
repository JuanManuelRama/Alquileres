package ar.controlalq.ui.contract

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import ar.controlalq.R
import ar.controlalq.model.Address
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Service
import ar.controlalq.model.Tenant
import ar.controlalq.ui.components.picker.PickerInputs
import ar.controlalq.ui.contract.create.ContractCreator
import ar.controlalq.ui.contract.create.ContractInputs
import ar.controlalq.ui.contract.create.renewalInputs
import ar.controlalq.ui.owner.OwnerInputs
import ar.controlalq.ui.property.PropertyInputs
import ar.controlalq.ui.service.ServiceInputs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractDraftRestorationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun renewalDraftRestoresEditsAndSubmitsANewContract() {
        val original = Contract(
            property = Property(Address("Main", "12", null), Owner("Owner", 41), 42),
            tenant = Tenant("Tenant", "16502530000"), rent = BigDecimal("1000"),
            startingMonth = YearMonth.of(2026, 1), finishingMonth = YearMonth.of(2026, 12),
            services = listOf(Service("Water", 7)), adjustments = emptyList(), id = 99,
        )
        var created: Contract? = null
        lateinit var current: ContractInputs
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            var inputs by rememberSerializable { mutableStateOf(original.renewalInputs()) }
            current = inputs
            MaterialTheme {
                ContractCreator(listOf(original.property), listOf(original.property.owner), original.services,
                    { created = it }, inputs, { inputs = it },
                    modifier = Modifier.verticalScroll(rememberScrollState()))
            }
        }
        composeRule.onNodeWithText("1000.00").performScrollTo().performTextReplacement("1500")
        restoration.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle {
            assertEquals("1500", current.rent)
            assertEquals(original.property, current.selectedProperty)
            assertEquals(original.services, current.selectedServices)
            assertEquals(null, created)
        }
        val createText = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.action_create_contract)
        composeRule.onNodeWithText(createText).performScrollTo().assertIsEnabled().performClick()
        composeRule.runOnIdle {
            val renewed = checkNotNull(created)
            assertEquals(0L, renewed.id)
            assertEquals(original.property, renewed.property)
            assertEquals(original.tenant, renewed.tenant)
            assertEquals(original.services, renewed.services)
            assertEquals(YearMonth.of(2027, 1), renewed.startingMonth)
            assertEquals(YearMonth.of(2027, 12), renewed.finishingMonth)
            assertEquals(BigDecimal("1500"), renewed.rent)
            assertEquals(BigDecimal("1000"), original.rent)
        }
    }

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
