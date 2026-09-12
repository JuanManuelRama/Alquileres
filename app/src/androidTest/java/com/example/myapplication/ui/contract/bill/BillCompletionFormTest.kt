package com.example.myapplication.ui.contract.bill

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.example.myapplication.model.Bill
import com.example.myapplication.model.BillItem
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Property
import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class BillCompletionFormTest {
    @get:Rule val composeRule = createComposeRule()

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
