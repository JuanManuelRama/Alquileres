package com.example.myapplication.ui.components.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate

class FormScope {
    val items = mutableListOf<FormItemState>()

    fun DateItem(
        label: String,
        value: LocalDate?,
        mandatory: Boolean = true,
        validate: ((LocalDate?) -> Boolean)? = null,
        onValueChange: (LocalDate?) -> Unit
    ) {
        items.add(
            DateFormItemState(
                label = label,
                value = value,
                mandatory = mandatory,
                validate = validate,
                onValueChange = onValueChange
            )
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
        validate: ((List<T>) -> Boolean)? = null,
        onItemsChanged: (List<T>) -> Unit,
        onItemCreated: (T) -> Unit,
    ) {
        this.items.add(
            MultiPickerFormItemState(
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
            )
        )
    }

    fun Item(
        field: String,
        value: String,
        mandatory: Boolean = true,
        keyboardType: KeyboardType = KeyboardType.Text,
        validate: ((String) -> Boolean)? = null,
        onValueChange: (String) -> Unit
    ) {
        items.add(
            TextFormItemState(
                field = field,
                value = value,
                mandatory = mandatory,
                keyboardType = keyboardType,
                validate = validate,
                onValueChange = onValueChange
            )
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
        validate: ((T?) -> Boolean)? = null,
        onItemSelected: (T?) -> Unit,
        onItemCreated: (T) -> Unit,
    ) {
        this.items.add(
            PickerFormItemState(
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
            )
        )
    }
}