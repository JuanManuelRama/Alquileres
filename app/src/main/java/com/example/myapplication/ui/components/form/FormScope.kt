package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate

/**
 * Items compose at their call site and participate in the enclosing form, even inside
 * Content, Column, Row, or another layout. Helpers that declare items must be @Composable.
 */
class FormScope {
    private class FieldRegistration(val isText: Boolean) {
        var error by mutableStateOf(Error.None)
        var position by mutableStateOf(Offset.Zero)
    }

    private val fields = mutableStateListOf<FieldRegistration>()
    internal val isValid: Boolean get() = fields.none { it.error.isError }

    /**
     * Groups arbitrary UI and form items without introducing a layout of its own.
     * For example: Content { Column { Item("Name", name) { name = it } } }.
     * Only items currently in composition participate in validation.
     */
    @Composable
    fun Content(content: @Composable FormScope.() -> Unit) {
        content()
    }

    @Composable
    private fun Field(state: FormItemState, modifier: Modifier) {
        val registration = remember(this) { FieldRegistration(state is TextFormItemState) }
        val error = state.error()
        DisposableEffect(this, registration) {
            fields.add(registration)
            onDispose { fields.remove(registration) }
        }
        SideEffect { registration.error = error }

        // Use rendered order so keyed children can be moved or inserted inside layouts.
        val direction = LocalLayoutDirection.current
        val lastTextField = fields.filter { it.isText }.maxWithOrNull(
            compareBy<FieldRegistration> { it.position.y }
                .thenBy { if (direction == LayoutDirection.Ltr) it.position.x else -it.position.x }
        )
        FormItem(
            item = state,
            modifier = modifier.onGloballyPositioned { registration.position = it.positionInRoot() },
            error = error,
            isLastTextField = registration === lastTextField
        )
    }

    @Composable
    fun DateItem(
        label: String,
        value: LocalDate?,
        mandatory: Boolean = true,
        validate: ((LocalDate?) -> Validation<*>)? = null,
        modifier: Modifier = Modifier,
        onValueChange: (LocalDate?) -> Unit
    ) {
        Field(
            state = DateFormItemState(
                label = label,
                value = value,
                mandatory = mandatory,
                validate = validate,
                onValueChange = onValueChange
            ),
            modifier = modifier
        )
    }

    @Composable
    fun <T> MultiPickerItem(
        label: String,
        items: List<T>,
        selectedItems: List<T>,
        mandatory: Boolean = true,
        itemLabel: (T) -> String,
        itemSearchText: (T) -> String = itemLabel,
        createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
        placeholder: String = "Search...",
        validate: ((List<T>) -> Validation<*>)? = null,
        onItemsChanged: (List<T>) -> Unit,
        onItemCreated: (T) -> Unit,
        modifier: Modifier = Modifier.fillMaxWidth(),
    ) {
        Field(
            state = MultiPickerFormItemState(
                label = label,
                mandatory = mandatory,
                items = items,
                selectedItems = selectedItems,
                onItemsChanged = onItemsChanged,
                onItemCreated = onItemCreated,
                itemLabel = itemLabel,
                itemSearchText = itemSearchText,
                createContent = createContent,
                placeholder = placeholder,
                validate = validate,
            ),
            modifier = modifier
        )
    }

    @Composable
    fun Item(
        field: String,
        value: String,
        mandatory: Boolean = true,
        keyboardType: KeyboardType = KeyboardType.Text,
        validate: ((String) -> Validation<*>)? = null,
        modifier: Modifier = Modifier.fillMaxWidth(),
        onValueChange: (String) -> Unit
    ) {
        Field(
            state = TextFormItemState(
                field = field,
                value = value,
                mandatory = mandatory,
                keyboardType = keyboardType,
                validate = validate,
                onValueChange = onValueChange
            ),
            modifier = modifier
        )
    }

    @Composable
    fun <T> PickerItem(
        label: String,
        items: List<T>,
        selectedItem: T?,
        mandatory: Boolean = true,
        itemLabel: (T) -> String,
        itemSearchText: (T) -> String = itemLabel,
        createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
        placeholder: String = "Search...",
        validate: ((T?) -> Validation<*>)? = null,
        onItemSelected: (T?) -> Unit,
        onItemCreated: (T) -> Unit,
        modifier: Modifier = Modifier.fillMaxWidth(),
    ) {
        Field(
            state = PickerFormItemState(
                label = label,
                mandatory = mandatory,
                items = items,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                onItemCreated = onItemCreated,
                itemLabel = itemLabel,
                itemSearchText = itemSearchText,
                createContent = createContent,
                placeholder = placeholder,
                validate = validate,
            ),
            modifier = modifier
        )
    }
}
