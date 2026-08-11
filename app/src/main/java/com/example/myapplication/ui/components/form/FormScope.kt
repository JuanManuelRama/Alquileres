package com.example.myapplication.ui.components.form

import androidx.compose.ui.text.input.KeyboardType

class FormScope {
    val items = mutableListOf<FormFieldState>()

    fun Item(
        key: FormFieldKey,
        value: String,
        mandatory: Boolean = false,
        keyboardType: KeyboardType = KeyboardType.Text,
        validate: ((String) -> Boolean)? = null,
        onValueChange: (String) -> Unit
    ) {
        items.add(
            TextFieldItemState(
                key = key,
                field = key.label,
                value = value,
                mandatory = mandatory,
                keyboardType = keyboardType,
                validate = validate,
                onValueChange = onValueChange
            )
        )
    }

    fun TypeaheadItem(
        key: FormFieldKey,
        value: String,
        options: List<TypeaheadOption>,
        mandatory: Boolean = false,
        validate: ((String) -> Boolean)? = null,
        onCreateNew: ((String) -> Unit)? = null,
        onValueChange: (String) -> Unit
    ) {
        items.add(
            TypeaheadItemState(
                key = key,
                field = key.label,
                value = value,
                mandatory = mandatory,
                options = options,
                validate = validate,
                onCreateNew = onCreateNew,
                onValueChange = onValueChange,
                onOptionSelected = { option ->
                    onValueChange(option.value)
                    autoFill(option.autoFill)
                }
            )
        )
    }

    private fun autoFill(values: Map<FormFieldKey, String>) {
        values.forEach { (targetKey, targetValue) ->
            items.firstOrNull { it.key == targetKey }?.onValueChange?.invoke(targetValue)
        }
    }
}