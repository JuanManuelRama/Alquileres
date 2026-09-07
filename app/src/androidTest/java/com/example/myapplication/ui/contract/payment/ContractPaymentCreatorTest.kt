package com.example.myapplication.ui.contract.payment

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.myapplication.model.Address
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class ContractPaymentCreatorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun paidMonthUpdatesBlockSubmissionAndSameMonthInAnotherYearIsAllowed() {
        val today = LocalDate.now()
        val month = YearMonth.from(today)
        var paidMonths by mutableStateOf(setOf(month.minusYears(1)))
        var submissions = 0
        val contract = Contract(
            property = Property(Address("Main", "1", null), Owner("Owner")),
            tenant = Tenant("Tenant", "123"),
            rent = BigDecimal("1000"),
            startingDate = today.minusMonths(6),
            finishingDate = today.plusMonths(6),
            adjustments = emptyList(),
            services = emptyList(),
            id = 1L
        )
        composeRule.setContent {
            MaterialTheme {
                ContractPaymentCreator(
                    contract = contract,
                    paidMonths = paidMonths,
                    onCreated = { submissions++ },
                    cancelText = "Return",
                    onCancel = {}
                )
            }
        }
        composeRule.onNodeWithText("Create").assertIsEnabled()
        composeRule.runOnIdle { paidMonths = setOf(month) }
        composeRule.onNodeWithText("A payment already exists for this month.").assertExists()
        composeRule.onNodeWithText("Create").assertIsNotEnabled()
        composeRule.runOnIdle { assertEquals(0, submissions) }
        composeRule.runOnIdle { paidMonths = emptySet() }
        composeRule.onNodeWithText("Create").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(1, submissions) }
    }
}
