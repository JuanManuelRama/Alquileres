package com.example.myapplication.ui.components.form

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FormInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

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
