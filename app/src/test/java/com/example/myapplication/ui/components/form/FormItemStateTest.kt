package com.example.myapplication.ui.components.form

import androidx.compose.foundation.text.KeyboardOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class FormItemStateTest {
    private fun fields(
        empty: Boolean,
        mandatory: Boolean,
        validate: (Any?) -> Validation<*>
    ): List<FormItemState> = listOf(
        TextFormItemState(
            field = "Field",
            value = if (empty) "  " else "value",
            mandatory = mandatory,
            validate = validate,
            onValueChange = {},
            keyboardOptions = KeyboardOptions.Default
        ),
        PickerFormItemState(
            label = "Field", items = listOf("value"),
            selectedItem = if (empty) null else "value",
            mandatory = mandatory, validate = validate,
            onItemSelected = {}, onItemCreated = {}, itemLabel = { it },
            createContent = { _, _ -> }
        ),
        MultiPickerFormItemState(
            label = "Field", items = listOf("value"),
            selectedItems = if (empty) emptyList() else listOf("value"),
            mandatory = mandatory, validate = validate,
            onItemsChanged = {}, onItemCreated = {}, itemLabel = { it },
            createContent = { _, _ -> }
        ),
        MonthFormItemState(
            label = "Field", value = if (empty) null else YearMonth.of(2026, 1),
            mandatory = mandatory, validate = validate, onValueChange = {}
        ),
        DateFormItemState(
            label = "Field", value = if (empty) null else LocalDate.of(2026, 1, 1),
            mandatory = mandatory, validate = validate, onValueChange = {}
        )
    )

    @Test
    fun emptyFieldsSkipCustomValidationAndRespectRequiredStatus() {
        for (mandatory in listOf(false, true)) {
            fields(empty = true, mandatory = mandatory) {
                error("Empty fields must not run custom validators")
            }.forEach { field ->
                assertEquals(if (mandatory) Error.Simple else Error.None, field.error())
                assertFalse(field.error().hasMessage)
                assertEquals(if (mandatory) "Field *" else "Field", field.displayLabel)
            }
        }
    }

    @Test
    fun populatedFieldsValidateTheirOriginalTypedValues() {
        val received = mutableListOf<Any?>()
        fields(empty = false, mandatory = true) {
            received.add(it)
            Validation.Valid(it)
        }.forEach { assertEquals(Error.None, it.error()) }
        assertEquals(listOf("value", "value", listOf("value"), YearMonth.of(2026, 1), LocalDate.of(2026, 1, 1)), received)
    }

    @Test
    fun invalidPopulatedFieldsBlockSubmissionEvenWhenOptional() {
        for (mandatory in listOf(false, true)) {
            fields(empty = false, mandatory = mandatory) {
                Validation.Invalid("Invalid value")
            }.forEach {
                assertEquals(Error.MessageError("Invalid value"), it.error())
                assertTrue(it.error().isError)
                assertTrue(it.error().hasMessage)
            }
        }
    }

    @Test
    fun copiedFieldsUseTheirCurrentValues() {
        val original = TextFormItemState(
            field = "Name",
            value = "",
            mandatory = true,
            onValueChange = {},
            keyboardOptions = KeyboardOptions.Default,
        )
        assertEquals(Error.Simple, original.error())
        assertEquals(Error.None, original.copy(value = "Ada").error())
    }
}
