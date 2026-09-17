package com.example.myapplication.ui.contract.bill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.myapplication.model.Address
import com.example.myapplication.model.Bill
import com.example.myapplication.model.BillItem
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class BillCompletionFormTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun extraRowsAreDraftsUntilSaveOrPaymentAndCanBeRemoved() {
        val month = YearMonth.of(2026, 9)
        val contract = Contract(
            property = Property(Address("Street", "12", null), Owner("Owner", 1), 1),
            tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
            startingMonth = month, finishingMonth = month.plusMonths(3),
            adjustments = emptyList(), services = emptyList(), id = 1
        )
        val bill = Bill(1, contract, month, BigDecimal("1000"), emptyList(), 0)
        var saved: Bill? = null
        var paid: Bill? = null
        composeRule.setContent {
            MaterialTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    BillCompletionForm(bill, false, { saved = it }, { value, _ -> paid = value })
                }
            }
        }
        composeRule.onNodeWithContentDescription("Add bill item").performScrollTo().performClick()
        composeRule.onNodeWithText("Save bill").assertIsNotEnabled()
        composeRule.onNodeWithText("Mark as paid").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Item name").performTextReplacement("Repairs")
        composeRule.onNodeWithContentDescription("Extra amount").performTextReplacement("25.50")
        composeRule.onNodeWithContentDescription("Add bill item").performScrollTo().performClick()
        composeRule.onAllNodesWithContentDescription("Item name")[1].performTextReplacement("Refund")
        composeRule.onAllNodesWithContentDescription("Extra amount")[1].performTextReplacement("-10.25")
        composeRule.onAllNodesWithContentDescription("Remove extra item")[0].performScrollTo().performClick()
        composeRule.onNodeWithText("Refund").assertExists()
        composeRule.onNodeWithText("989.75").assertExists()
        composeRule.runOnIdle {
            assertEquals(null, saved)
            assertEquals(null, paid)
            assertEquals(emptyList<BillItem>(), bill.items)
        }
        composeRule.onNodeWithText("Save bill").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(listOf(BillItem("Refund", BigDecimal("-10.25"), true)), saved!!.items)
        }
        composeRule.onNodeWithText("Mark as paid").performScrollTo().performClick()
        composeRule.onNodeWithText("Confirm payment").performClick()
        composeRule.runOnIdle { assertEquals(saved!!.items, paid!!.items) }
    }

    @Test fun amountKeepsIntermediateTextAndConvertsOnlyOnSubmission() {
        val contract = Contract(
            property = Property(Address("Street", "12", null), Owner("Owner", 1), 1),
            tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
            startingMonth = YearMonth.of(2026, 9), finishingMonth = YearMonth.of(2026, 12),
            adjustments = emptyList(), services = emptyList(), id = 1
        )
        val bill = Bill(1, contract, YearMonth.of(2026, 9), BigDecimal("1000"),
            listOf(BillItem("Water", BigDecimal("12"))), 0)
        var saved: Bill? = null
        composeRule.setContent {
            MaterialTheme {
                BillCompletionForm(bill, false, { saved = it }, { _, _ -> })
            }
        }
        composeRule.onNodeWithText("12").performTextReplacement("55.")
        composeRule.onNodeWithText("55.").assertExists().performTextInput("5")
        composeRule.onNodeWithText("55.5").assertExists().performTextReplacement("-")
        composeRule.onNodeWithText("-").assertExists()
        composeRule.onNodeWithText("Save bill").assertIsNotEnabled()
        composeRule.onNodeWithText("Mark as paid").assertIsNotEnabled()
        composeRule.onNodeWithText("-").performTextReplacement("55.555")
        composeRule.onNodeWithText("Mark as paid").assertIsNotEnabled()
        composeRule.onNodeWithText("55.555").performTextReplacement("55.50")
        composeRule.onNodeWithText("1055.50").assertExists()
        composeRule.onNodeWithText("Save bill").performClick()
        composeRule.runOnIdle {
            assertEquals(BigDecimal("55.50"), saved!!.items.single().amount)
            assertEquals(BigDecimal("12"), bill.items.single().amount)
        }
    }
}
