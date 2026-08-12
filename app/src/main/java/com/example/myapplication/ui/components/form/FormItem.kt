package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.ui.picker.EntityPicker

@Composable
fun FormItem(item: FormItemState, modifier: Modifier = Modifier) {
    when (item) {
        is TextFormItemState -> TextFormItemContent(item, modifier)
        is PickerFormItemState<*> -> PickerFormItemContent(item, modifier)
    }
}

@Composable
private fun TextFormItemContent(item: TextFormItemState, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = item.value,
        onValueChange = item.onValueChange,
        label = { Text(item.field + if (item.mandatory) " *" else "") },
        keyboardOptions = KeyboardOptions(keyboardType = item.keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun <T> PickerFormItemContent(
    item: PickerFormItemState<T>,
    modifier: Modifier = Modifier) {
    EntityPicker(
        items = item.items,
        selectedItem = item.selectedItem,
        onItemSelected = item.onItemSelected,
        onItemCreated = item.onItemCreated,
        itemLabel = item.itemLabel,
        itemSearchText = item.itemSearchText,
        createContent = item.createContent,
        modifier = modifier,
        label = item.label + if (item.mandatory) " *" else "",
        placeholder = item.placeholder,
    )
}

sealed interface FormItemState {
    val mandatory: Boolean
    fun isValidated(): Boolean
}

data class TextFormItemState(
    val field: String,
    override val mandatory: Boolean = false,
    val value: String,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val validate: ((String) -> Boolean)? = null,
    val onValueChange: (String) -> Unit
) : FormItemState {
    override fun isValidated(): Boolean =
        (!mandatory || value.isNotBlank()) && (validate?.invoke(value) ?: true)
}

data class PickerFormItemState<T>(
    val label: String,
    override val mandatory: Boolean = false,
    val items: List<T>,
    val selectedItem: T?,
    val onItemSelected: (T?) -> Unit,
    val onItemCreated: (T) -> Unit,
    val itemLabel: (T) -> String,
    val itemSearchText: (T) -> String = itemLabel,
    val createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
    val placeholder: String = "Search $label...",
    val validate: ((T?) -> Boolean)? = null,
) : FormItemState {
    override fun isValidated(): Boolean =
        (!mandatory || selectedItem != null) && (validate?.invoke(selectedItem) ?: true)
}