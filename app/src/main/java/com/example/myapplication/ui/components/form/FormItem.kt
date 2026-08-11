// FormItem.kt
package com.example.myapplication.ui.components.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FormItem(item: FormFieldState, modifier: Modifier = Modifier) {
    when (item) {
        is TextFieldItemState -> TextFormItem(item = item, modifier = modifier)
        is TypeaheadItemState -> TypeaheadFormItem(item = item, modifier = modifier)
    }
}