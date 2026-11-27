package ar.controlalq.ui.components.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

class FormInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nextFromLastTextFieldFocusesAndExpandsPicker() {
        checkNextOpensPicker(multiple = false)
    }

    @Test
    fun nextFocusesAndExpandsMultiPickerBeforeLaterTextFields() {
        checkNextOpensPicker(multiple = true)
    }

    private fun checkNextOpensPicker(multiple: Boolean) {
        var querySelection by mutableStateOf<String?>(null)
        var selections by mutableStateOf(emptyList<String>())
        composeRule.setContent {
            MaterialTheme {
                Form("Save", {}, cancel = Cancel("Cancel", {})) {
                    Content {
                        Row {
                            Item("Number", "12", modifier = Modifier.weight(1f), onValueChange = {})
                            Item("Unit", "A", modifier = Modifier.weight(1f), onValueChange = {})
                        }
                    }
                    if (multiple) {
                        MultiPickerItem(
                            label = "Services", createNewLabel = "Create new service", items = listOf("Water"), selectedItems = selections,
                            itemLabel = { it }, createContent = { _, _ -> },
                            placeholder = "Find service",
                            onItemsChanged = { selections = it }, onItemCreated = {}
                        )
                        Item("Later", "", onValueChange = {})
                    } else {
                        PickerItem(
                            label = "Owner", createNewLabel = "Create new owner", items = listOf("Ada"), selectedItem = querySelection,
                            itemLabel = { it }, createContent = { _, _ -> },
                            placeholder = "Find owner",
                            onItemSelected = { querySelection = it }, onItemCreated = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("Unit *")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Next))
            .performClick().performImeAction()
        composeRule.onNodeWithText(if (multiple) "Water" else "Ada").assertIsDisplayed()
        composeRule.onNodeWithText(if (multiple) "Find service" else "Find owner")
            .assertIsFocused()
            .performTextInput(if (multiple) "Wat" else "Ad")
        composeRule.onNodeWithText(if (multiple) "Water" else "Ada")
            .assertIsDisplayed().performClick()
        composeRule.runOnIdle {
            if (multiple) assertEquals(listOf("Water"), selections)
            else assertEquals("Ada", querySelection)
        }
    }

    @Test
    fun monthPickerSelectsYearAndMonthAndEnablesSubmission() {
        var month by mutableStateOf<YearMonth?>(null)
        composeRule.setContent {
            MaterialTheme {
                Form("Save", {}, cancel = Cancel("Cancel", {})) {
                    MonthItem("Starting month", month, onValueChange = { month = it })
                }
            }
        }
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.onNodeWithText("Starting month *").performClick()
        composeRule.onNodeWithText("Year").performTextReplacement("2028")
        val february = Month.FEBRUARY.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
        composeRule.onNodeWithText(february).performClick()
        composeRule.runOnIdle { assertEquals(YearMonth.of(2028, 2), month) }
        composeRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun nestedContentUsesItsLayoutAndValidatesCurrentValues() {
        var value by mutableStateOf("bad")
        var submissions = 0
        composeRule.setContent {
            MaterialTheme {
                Form("Save", { submissions++ }, cancel = Cancel("Cancel", {})) {
                    Item("Outside", "valid", onValueChange = {})
                    Content {
                        Column(Modifier.padding(start = 24.dp)) {
                            Text("Details")
                            Content {
                                Item(
                                    "Amount", value,
                                    validate = { Validate(it).map("must be a valid decimal number") { v -> v.toBigDecimalOrNull() } },
                                    onValueChange = { value = it }
                                )
                            }
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("must be a valid decimal number").assertIsDisplayed()
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        val outside = composeRule.onNodeWithText("Outside *").fetchSemanticsNode().boundsInRoot
        val nested = composeRule.onNodeWithText("Amount *").fetchSemanticsNode().boundsInRoot
        assertTrue(nested.left > outside.left)
        assertTrue(nested.top >= outside.bottom)
        composeRule.onNodeWithText("Amount *").performTextReplacement("12")
        composeRule.onNodeWithText("Save").assertIsEnabled().performClick()
        composeRule.runOnIdle {
            assertEquals("12", value)
            assertEquals(1, submissions)
        }
    }

    @Test
    fun removingAndRestoringContentUpdatesValidationAndLastTextField() {
        var visible by mutableStateOf(true)
        composeRule.setContent {
            MaterialTheme {
                Form("Save", {}, cancel = Cancel("Cancel", {})) {
                    Item("Outside", "valid", onValueChange = {})
                    Content {
                        if (visible) {
                            Column {
                                Item("Inside", "", onValueChange = {})
                            }
                        }
                    }
                }
            }
        }

        repeat(2) {
            composeRule.onNodeWithText("Save").assertIsNotEnabled()
            composeRule.onNodeWithText("Outside *").assert(
                SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Next)
            )
            composeRule.runOnIdle { visible = false }
            composeRule.onNodeWithText("Inside *").assertDoesNotExist()
            composeRule.onNodeWithText("Save").assertIsEnabled()
            composeRule.onNodeWithText("Outside *").assert(
                SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Done)
            )
            composeRule.runOnIdle { visible = true }
        }
    }

    @Test
    fun nestedRowsSupportModifiersAndKeyboardNavigationAfterReordering() {
        var names by mutableStateOf(listOf("First", "Second"))
        composeRule.setContent {
            MaterialTheme {
                Form("Save", {}, cancel = Cancel("Cancel", {})) {
                    Content {
                        Row {
                            names.forEach { name ->
                                key(name) {
                                    Item(name, "valid", modifier = Modifier.weight(1f), onValueChange = {})
                                }
                            }
                        }
                    }
                }
            }
        }

        fun checkOrder(first: String, last: String) {
            val firstNode = composeRule.onNodeWithText("$first *")
            val lastNode = composeRule.onNodeWithText("$last *")
            val firstBounds = firstNode.fetchSemanticsNode().boundsInRoot
            val lastBounds = lastNode.fetchSemanticsNode().boundsInRoot
            assertEquals(firstBounds.top, lastBounds.top)
            assertTrue(firstBounds.right <= lastBounds.left)
            firstNode.assert(SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Next))
            lastNode.assert(SemanticsMatcher.expectValue(SemanticsProperties.ImeAction, ImeAction.Done))
            firstNode.performClick().performImeAction()
            lastNode.assertIsFocused()
        }

        checkOrder("First", "Second")
        composeRule.runOnIdle { names = names.reversed() }
        checkOrder("Second", "First")
    }

    @Test
    fun nestedDateAndPickersAllParticipateInSubmission() {
        var date by mutableStateOf<LocalDate?>(null)
        var selected by mutableStateOf<String?>(null)
        var selections by mutableStateOf(emptyList<String>())
        composeRule.setContent {
            MaterialTheme {
                Form("Save", {}, cancel = Cancel("Cancel", {})) {
                    Content {
                        Column {
                            DateItem("Date", date, onValueChange = { date = it })
                            PickerItem(
                                label = "Choice", createNewLabel = "Create new choice", items = listOf("A"), selectedItem = selected,
                                itemLabel = { it }, createContent = { _, _ -> },
                                onItemSelected = { selected = it }, onItemCreated = {}
                            )
                            MultiPickerItem(
                                label = "Choices", createNewLabel = "Create new choice", items = listOf("A"), selectedItems = selections,
                                itemLabel = { it }, createContent = { _, _ -> },
                                onItemsChanged = { selections = it }, onItemCreated = {}
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.runOnIdle { date = LocalDate.of(2026, 1, 1) }
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.runOnIdle { selected = "A" }
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.runOnIdle { selections = listOf("A") }
        composeRule.onNodeWithText("Save").assertIsEnabled()
        composeRule.runOnIdle { date = null }
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun mandatoryFieldControlsSubmissionAndForwardsTextChanges() {
        var value by mutableStateOf("")
        var submissions = 0

        composeRule.setContent {
            MaterialTheme {
                Form(
                    submitText = "Save",
                    onSubmit = { submissions++ },
                    cancel = Cancel("Cancel", {}),
                ) {
                    Item(field = "Name", value = value, onValueChange = { value = it })
                }
            }
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.onNodeWithText("Name *").performTextInput("Ada")
        composeRule.onNodeWithText("Save").assertIsEnabled().performClick()

        composeRule.runOnIdle {
            assertEquals("Ada", value)
            assertEquals(1, submissions)
        }
    }

    @Test
    fun invalidFieldDisplaysMessageAndBlocksSubmission() {
        composeRule.setContent {
            MaterialTheme {
                Form(
                    submitText = "Save",
                    onSubmit = {},
                    cancel = Cancel("Cancel", {}),
                ) {
                    Item(
                        field = "Amount",
                        value = "not-a-number",
                        validate = { Validate(it).map("must be a valid decimal number") { v -> v.toBigDecimalOrNull() } },
                        onValueChange = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("must be a valid decimal number").assertIsDisplayed()
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun cancelRemainsAvailableWhenFormIsInvalid() {
        var cancellations = 0
        composeRule.setContent {
            MaterialTheme {
                Form(
                    submitText = "Save",
                    onSubmit = {},
                    cancel = Cancel("Cancel", { cancellations++ }),
                ) {
                    Item(field = "Required", value = "", onValueChange = {})
                }
            }
        }

        composeRule.onNodeWithText("Cancel").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(1, cancellations) }
    }
}
