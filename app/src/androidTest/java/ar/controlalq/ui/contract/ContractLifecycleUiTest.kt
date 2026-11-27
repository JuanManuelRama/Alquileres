package ar.controlalq.ui.contract

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ar.controlalq.model.Address
import ar.controlalq.model.Bill
import ar.controlalq.model.BillItem
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Tenant
import ar.controlalq.ui.contract.bill.BillCompletionForm
import ar.controlalq.ui.contract.list.ContractListItem
import ar.controlalq.ui.contract.list.ContractListScreen
import ar.controlalq.ui.contract.list.ContractListUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractLifecycleUiTest {
    @get:Rule val composeRule = createComposeRule()
    private fun contract(id: Long) = Contract(
        property = Property(Address("Street $id", "1", null), Owner("Owner", 1), id),
        tenant = Tenant("Tenant $id", "123"), rent = BigDecimal("1000"),
        startingMonth = YearMonth.of(2026, 1), finishingMonth = YearMonth.of(2026, 2),
        adjustments = emptyList(), services = emptyList(), id = id
    )

    @Test fun bottomTabsSeparateActiveAndInactiveContracts() {
        val active = contract(1)
        val inactive = contract(2)
        composeRule.setContent {
            MaterialTheme {
                var isInactive by remember { mutableStateOf(false) }
                val state = if (isInactive) ContractListUiState.Inactive(listOf(inactive))
                    else ContractListUiState.Active(listOf(ContractListItem(
                    contract = active,
                    paymentDue = false,
                    expiresSoon = false,
                    hikeThisMonth = false
                )))
                ContractListScreen(
                    state = state,
                    onContractClick = {},
                    addContractClick =  {},
                    onTabSelected = { isInactive = it },
                    onSettingsClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Tenant: Tenant 1").assertExists()
        composeRule.onNodeWithText("Tenant: Tenant 2").assertDoesNotExist()
        composeRule.onNodeWithText("Inactive", useUnmergedTree = false).performClick()
        composeRule.onNodeWithText("Tenant: Tenant 2").assertExists()
        composeRule.onNodeWithText("Tenant: Tenant 1").assertDoesNotExist()
        composeRule.onNodeWithText("Active", useUnmergedTree = false).performClick()
        composeRule.onNodeWithText("Tenant: Tenant 1").assertExists()
    }

    @Test fun inactiveUnpaidBillsHaveNoEditingOrPaymentActions() {
        val bill = Bill(1, contract(1), YearMonth.of(2026, 1), BigDecimal("1000"),
            listOf(BillItem("Water", null), BillItem("Refund", BigDecimal("-10"), true)), 0)
        var writes = 0
        composeRule.setContent {
            MaterialTheme {
                BillCompletionForm(
                    bill = bill,
                    isSaving = false,
                    onSubmit = { writes++ },
                    onMarkPaid = { _, _ -> writes++ },
                    isInactive = true,
                    onChangesChanged = {},
                    paymentFailed = false,
                    isPaying = false
                )
            }
        }
        composeRule.onNodeWithText("Unpaid").assertExists()
        composeRule.onNodeWithText("Save bill").assertDoesNotExist()
        composeRule.onNodeWithText("Mark as paid").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Add bill item").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Remove extra item").assertDoesNotExist()
        composeRule.onNodeWithText("Refund").assertExists()
        composeRule.runOnIdle { assertEquals(0, writes) }
    }
}
