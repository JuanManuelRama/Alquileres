package com.example.myapplication.ui.components.form

import androidx.compose.ui.text.input.KeyboardType

class FormScope {
    val items = mutableListOf<FormItemState>()

    fun Item(
        field: String,
        value: String,
        mandatory: Boolean = false,
        keyboardType: KeyboardType = KeyboardType.Text,
        validate: ((String) -> Boolean)? = null,
        onValueChange: (String) -> Unit
    ) {
        items.add(
            FormItemState(
                field = field,
                value = value,
                mandatory = mandatory,
                keyboardType = keyboardType,
                validate = validate,
                onValueChange = onValueChange
            )
        )
    }
}