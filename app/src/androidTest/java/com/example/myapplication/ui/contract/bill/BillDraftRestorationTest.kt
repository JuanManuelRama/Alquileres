package com.example.myapplication.ui.contract.bill

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.example.myapplication.model.Address
import com.example.myapplication.model.Bill
import com.example.myapplication.model.BillItem
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class BillDraftRestorationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun switchingAndRestoringRetainsBothDraftsWithoutSavingToStorage() {
        val month = YearMonth.of(2026, 9)
        val contract = Contract(
            property = Property(Address("Street", "12", null), Owner("Owner", 1), 1),
            tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
            startingMonth = month, finishingMonth = month.plusMonths(3),
            adjustments = emptyList(), services = emptyList(), id = 1
        )
        val bills = listOf(
            Bill(1, contract, month, BigDecimal("1000"), listOf(BillItem("Water", BigDecimal("12"))), 0),
            Bill(2, contract, month.plusMonths(1), BigDecimal("1000"), listOf(BillItem("Water", BigDecimal("24"))), 0)
        )
        var saves = 0
        val changed = mutableMapOf<Long, Boolean>()
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            var selected by rememberSaveable { mutableStateOf(0) }
            MaterialTheme {
                BillCards(bills, bills[selected].month, true, { selectedMonth ->
                    selected = bills.indexOfFirst { it.month == selectedMonth }
                }) {
                    val bill = bills[selected]
                    BillCompletionForm(
                        bill, false, { saves++ }, { _, _ -> saves++ },
                        onChangesChanged = { changed[bill.id] = it }
                    )
                }
            }
        }

        composeRule.runOnIdle { assertFalse(changed.getValue(1)) }
        restoration.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { assertFalse(changed.getValue(1)) }
        composeRule.onNodeWithText("12").performTextReplacement("55.")
        composeRule.runOnIdle { assertTrue(changed.getValue(1)) }
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("24").performTextReplacement("-")
        composeRule.onNodeWithText("Previous").performClick()
        composeRule.onNodeWithText("55.").assertExists()

        restoration.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText("55.").assertExists()
        composeRule.runOnIdle { assertTrue(changed.getValue(1)) }
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("-").assertExists()
        composeRule.runOnIdle { assertTrue(changed.getValue(2)) }
        composeRule.onNodeWithText("Previous").performClick()
        composeRule.onNodeWithText("55.").assertExists()
        composeRule.onNodeWithText("55.").performTextReplacement("12")
        composeRule.runOnIdle { assertFalse(changed.getValue(1)) }
        composeRule.runOnIdle { assertEquals(0, saves) }
    }
}
