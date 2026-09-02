package com.example.myapplication.ui.components.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate

internal sealed interface FormElement {
    data class Field(val state: FormItemState) : FormElement
    data class Content(val content: @Composable () -> Unit) : FormElement
}

class FormScope {
    internal val elements = mutableListOf<FormElement>()

    fun Content(content: @Composable () -> Unit) {
        elements.add(FormElement.Content(content))
    }

    fun DateItem(
        label: String,
        value: LocalDate?,
        mandatory: Boolean = true,
        validate: ((LocalDate?) -> Validation<*>)? = null,
        onValueChange: (LocalDate?) -> Unit
    ) {
        elements.add(
            FormElement.Field(DateFormItemState(
                label = label,
                value = value,
                mandatory = mandatory,
                validate = validate,
                onValueChange = onValueChange
            ))
        )
    }

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
    ) {
        elements.add(
            FormElement.Field(MultiPickerFormItemState(
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
            ))
        )
    }

    fun Item(
        field: String,
        value: String,
        mandatory: Boolean = true,
        keyboardType: KeyboardType = KeyboardType.Text,
        validate: ((String) -> Validation<*>)? = null,
        onValueChange: (String) -> Unit
    ) {
        elements.add(
            FormElement.Field(TextFormItemState(
                field = field,
                value = value,
                mandatory = mandatory,
                keyboardType = keyboardType,
                validate = validate,
                onValueChange = onValueChange
            ))
        )
    }

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
    ) {
        elements.add(
            FormElement.Field(PickerFormItemState(
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
            ))
        )
    }
}
