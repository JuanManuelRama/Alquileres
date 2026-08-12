package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun FormItem(
    item: FormItemState,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = item.value,
        onValueChange = item.onValueChange,
        label = { Text(item.field + if (item.mandatory) " *" else "") },
        keyboardOptions = KeyboardOptions(keyboardType = item.keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

data class FormItemState(
    val field: String,
    val mandatory: Boolean = false,
    val value: String,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val validate: ((String) -> Boolean)? = null,
    val onValueChange: (String) -> Unit
) {
    fun isValidated(): Boolean = (!mandatory || value.isNotBlank()) && (validate?.invoke(value) ?: true)
}