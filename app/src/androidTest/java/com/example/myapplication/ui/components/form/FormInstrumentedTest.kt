package com.example.myapplication.ui.components.form

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
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FormInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun nestedContentUsesItsLayoutAndValidatesCurrentValues() {
        var value by mutableStateOf("bad")
        var submissions = 0
        composeRule.setContent {
            MaterialTheme {
                Form("Save", { submissions++ }, "Cancel", {}) {
                    Item("Outside", "valid", onValueChange = {})
                    Content {
                        Column(Modifier.padding(start = 24.dp)) {
                            Text("Details")
                            Content {
                                Item(
                                    "Amount", value,
                                    validate = { Validate(it).isBigDecimal() },
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
                Form("Save", {}, "Cancel", {}) {
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
                Form("Save", {}, "Cancel", {}) {
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
        var date by mutableStateOf<java.time.LocalDate?>(null)
        var selected by mutableStateOf<String?>(null)
        var selections by mutableStateOf(emptyList<String>())
        composeRule.setContent {
            MaterialTheme {
                Form("Save", {}, "Cancel", {}) {
                    Content {
                        Column {
                            DateItem("Date", date, onValueChange = { date = it })
                            PickerItem(
                                label = "Choice", items = listOf("A"), selectedItem = selected,
                                itemLabel = { it }, createContent = { _, _ -> },
                                onItemSelected = { selected = it }, onItemCreated = {}
                            )
                            MultiPickerItem(
                                label = "Choices", items = listOf("A"), selectedItems = selections,
                                itemLabel = { it }, createContent = { _, _ -> },
                                onItemsChanged = { selections = it }, onItemCreated = {}
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.runOnIdle { date = java.time.LocalDate.of(2026, 1, 1) }
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
                    cancelText = "Cancel",
                    onCancel = {},
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
                    cancelText = "Cancel",
                    onCancel = {},
                ) {
                    Item(
                        field = "Amount",
                        value = "not-a-number",
                        validate = { Validate(it).isBigDecimal() },
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
                    cancelText = "Cancel",
                    onCancel = { cancellations++ },
                ) {
                    Item(field = "Required", value = "", onValueChange = {})
                }
            }
        }

        composeRule.onNodeWithText("Cancel").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(1, cancellations) }
    }
}
