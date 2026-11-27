package ar.controlalq.ui.contract.list

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import ar.controlalq.model.Address
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Tenant
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun inactiveLoadingAndEmptyStatesKeepTheInactiveTabAndHideCreation() {
        var state by mutableStateOf<ContractListUiState>(ContractListUiState.Loading(isInactive = true))
        composeRule.setContent {
            MaterialTheme { ContractListScreen(
                state = state,
                onContractClick = {},
                addContractClick = {},
                onTabSelected = {},
                onSettingsClick = {}
            ) }
        }
        composeRule.onNodeWithText("Inactive contracts").assertIsDisplayed()
        composeRule.onNodeWithText("Inactive").assertIsSelected()
        composeRule.onNodeWithText("Loading contracts…").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Create new contract").assertDoesNotExist()
        composeRule.runOnIdle { state = ContractListUiState.Inactive(emptyList()) }
        composeRule.onNodeWithText("No inactive contracts yet.").assertIsDisplayed()
        composeRule.onNodeWithText("Inactive").assertIsSelected()
        composeRule.onNodeWithText("A fresh start, on paper").assertDoesNotExist()
    }

    @Test
    fun loadingDoesNotShowEmptyFallbackAndLoadedResultsChooseTheCorrectContent() {
        var state by mutableStateOf<ContractListUiState>(ContractListUiState.Loading())
        composeRule.setContent {
            MaterialTheme {
                ContractListScreen(
                    state = state,
                    onContractClick = {},
                    addContractClick = {},
                    onTabSelected = {},
                    onSettingsClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Loading contracts…").assertIsDisplayed()
        composeRule.onNodeWithText("A fresh start, on paper").assertDoesNotExist()

        val contract = Contract(
            property = Property(Address("Main", "1", null), Owner("Owner")),
            tenant = Tenant("Ada", "123"), rent = BigDecimal("1000"),
            startingMonth = YearMonth.of(2026, 1), finishingMonth = YearMonth.of(2027, 1),
            adjustments = emptyList(), services = emptyList(), id = 1L
        )
        composeRule.runOnIdle {
            state = ContractListUiState.Active(listOf(ContractListItem(contract, false, false, false)))
        }
        composeRule.onNodeWithText("Tenant: Ada").assertIsDisplayed()
        composeRule.onNodeWithText("Loading contracts…").assertDoesNotExist()
        composeRule.onNodeWithText("A fresh start, on paper").assertDoesNotExist()

        composeRule.runOnIdle { state = ContractListUiState.Active(emptyList()) }
        composeRule.onNodeWithText("A fresh start, on paper").assertIsDisplayed()
        composeRule.onNodeWithText("Loading contracts…").assertDoesNotExist()
    }
}
