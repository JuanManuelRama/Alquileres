package com.example.myapplication.ui.property

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.model.Address
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.SaveStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class PropertyListScreenTest {
    @get:Rule val rule = createComposeRule()
    private val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
    private val properties = PropertyRepository(db)
    private val contracts = ContractRepository(db)
    private lateinit var model: PropertyListViewModel
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
    private fun seed(): Contract = runBlocking {
        contracts.insert(Contract(Property(Address("Main", "1", null), Owner("Owner")), Tenant("Tenant", "123"),
            BigDecimal("1000"), YearMonth.of(2020, 1), YearMonth.of(2020, 2), emptyList(), emptyList()))
    }
    private fun show(): StateRestorationTester {
        model = PropertyListViewModel(properties, OwnerRepository(db.ownerDao()))
        return StateRestorationTester(rule).also { restoration ->
            restoration.setContent { MaterialTheme { PropertyListScreen(model, {}, {}) } }
            rule.waitUntil(5_000) { model.state.value is PropertyListState.Ready }
            rule.waitForIdle()
        }
    }
    @Test fun activeContractsBlockDeactivationAndDeleteRequiresConfirmation() {
        val contract = seed()
        show()
        rule.onNodeWithText(text(R.string.property_active)).assertIsNotEnabled()
        rule.onNodeWithContentDescription(text(R.string.action_delete)).performClick()
        rule.onNodeWithText(text(R.string.delete_property_title)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.action_cancel)).performClick()
        assertNotNull(runBlocking { contracts.observeById(contract.id).first() })
        rule.onNodeWithContentDescription(text(R.string.action_delete)).performClick()
        rule.onNodeWithText(text(R.string.action_delete)).performClick()
        rule.waitUntil(5_000) { (model.state.value as? PropertyListState.Ready)?.rows?.isEmpty() == true }
        rule.onNodeWithText(text(R.string.empty_properties)).assertIsDisplayed()
        assertNull(runBlocking { contracts.observeById(contract.id).first() })
    }

    @Test fun finishedPropertyCanBeDeactivatedAndReactivatedWithCheckbox() {
        val contract = seed()
        runBlocking { contracts.finish(contract.id) }
        show()
        rule.onNodeWithText(text(R.string.property_active)).assertIsEnabled().performClick()
        rule.waitUntil(5_000) { (model.state.value as? PropertyListState.Ready)?.rows?.single()?.isActive == false && model.saveStatus.value == SaveStatus.Idle }
        assertTrue(runBlocking { properties.getActive().first().isEmpty() })
        rule.onNodeWithText(text(R.string.property_inactive)).assertIsEnabled().performClick()
        rule.waitUntil(5_000) { (model.state.value as? PropertyListState.Ready)?.rows?.single()?.isActive == true }
        assertEquals(contract.property.id, runBlocking { properties.getActive().first().single().id })
    }

    @Test fun editDraftRestoresAndDiscardKeepsPersistedPropertyThenSaveUpdatesIt() {
        val contract = seed()
        val restoration = show()
        rule.onNodeWithContentDescription(text(R.string.action_edit)).performClick()
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("Corrected")
        restoration.emulateSavedInstanceStateRestore()
        rule.onAllNodes(hasSetTextAction())[0].assertTextContains("Corrected")
        rule.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        rule.onNodeWithText(text(R.string.discard_changes_title)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.action_discard)).performClick()
        assertEquals("Main", runBlocking { properties.getAll().first().single().address.street })
        rule.onNodeWithContentDescription(text(R.string.action_edit)).performClick()
        rule.onAllNodes(hasSetTextAction())[0].performTextReplacement("")
        rule.onNodeWithText(text(R.string.action_save)).performScrollTo().assertIsNotEnabled()
        rule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement("Corrected")
        rule.onNodeWithText(text(R.string.action_save)).performScrollTo().performClick()
        rule.waitUntil(5_000) { (model.state.value as? PropertyListState.Ready)?.rows?.single()?.property?.address?.street == "Corrected" }
        assertEquals("Corrected", runBlocking { contracts.observeById(contract.id).first()!!.property.address.street })
    }
}
