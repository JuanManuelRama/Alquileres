package com.example.myapplication.ui.components.form

import androidx.compose.ui.text.input.KeyboardType

data class TextFieldItemState(
    override val key: FormFieldKey,
    override val field: String,
    override val mandatory: Boolean = false,
    val value: String,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val validate: ((String) -> Boolean)? = null,
    override val onValueChange: (String) -> Unit
) : FormFieldState {
    override fun isValidated(): Boolean =
        (!mandatory || value.isNotBlank()) && (validate?.invoke(value) ?: true)
}