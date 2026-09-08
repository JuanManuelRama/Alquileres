package com.example.myapplication.ui.contract.list

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.myapplication.model.Address
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class ContractListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingDoesNotShowEmptyFallbackAndLoadedResultsChooseTheCorrectContent() {
        var state by mutableStateOf<ContractListUiState>(ContractListUiState.Loading)
        composeRule.setContent {
            MaterialTheme {
                ContractListScreen(state = state, onContractClick = {}, addContractClick = {})
            }
        }
        composeRule.onNodeWithText("Loading contracts...").assertIsDisplayed()
        composeRule.onNodeWithText("A fresh start, on paper").assertDoesNotExist()

        val contract = Contract(
            property = Property(Address("Main", "1", null), Owner("Owner")),
            tenant = Tenant("Ada", "123"), rent = BigDecimal("1000"),
            startingDate = LocalDate.of(2026, 1, 1), finishingDate = LocalDate.of(2027, 1, 1),
            adjustments = emptyList(), services = emptyList(), id = 1L
        )
        composeRule.runOnIdle {
            state = ContractListUiState.Loaded(listOf(ContractListItem(contract, false, false, false)))
        }
        composeRule.onNodeWithText("Tenant: Ada").assertIsDisplayed()
        composeRule.onNodeWithText("Loading contracts...").assertDoesNotExist()
        composeRule.onNodeWithText("A fresh start, on paper").assertDoesNotExist()

        composeRule.runOnIdle { state = ContractListUiState.Loaded(emptyList()) }
        composeRule.onNodeWithText("A fresh start, on paper").assertIsDisplayed()
        composeRule.onNodeWithText("Loading contracts...").assertDoesNotExist()
    }
}
