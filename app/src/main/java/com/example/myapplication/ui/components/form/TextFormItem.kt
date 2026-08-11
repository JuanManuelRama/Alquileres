package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TextFormItem(
    item: TextFieldItemState,
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