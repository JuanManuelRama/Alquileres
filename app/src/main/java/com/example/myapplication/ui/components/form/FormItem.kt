package com.example.myapplication.ui.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.ui.picker.EntityPicker
import com.example.myapplication.ui.picker.MultiEntityPicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun FormItem(item: FormItemState, modifier: Modifier = Modifier) {
    when (item) {
        is TextFormItemState -> TextFormItemContent(item, modifier)
        is PickerFormItemState<*> -> PickerFormItemContent(item, modifier)
        is MultiPickerFormItemState<*> -> MultiPickerFormItemContent(item, modifier)
        is DateFormItemState -> DateFormItemContent(item, modifier)
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

@Composable
private fun <T> MultiPickerFormItemContent(
    item: MultiPickerFormItemState<T>,
    modifier: Modifier = Modifier
) {
    MultiEntityPicker(
        items = item.items,
        selectedItems = item.selectedItems,
        onItemsChanged = item.onItemsChanged,
        onItemCreated = item.onItemCreated,
        itemLabel = item.itemLabel,
        itemSearchText = item.itemSearchText,
        createContent = item.createContent,
        modifier = modifier,
        label = item.label + if (item.mandatory) " *" else "",
        placeholder = item.placeholder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFormItemContent(item: DateFormItemState, modifier: Modifier = Modifier) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = item.value?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        item.onValueChange(date)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = item.value?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
        onValueChange = { },
        label = { Text(item.label + if (item.mandatory) " *" else "") },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        readOnly = true,
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

data class MultiPickerFormItemState<T>(
    val label: String,
    override val mandatory: Boolean = false,
    val items: List<T>,
    val selectedItems: List<T>,
    val onItemsChanged: (List<T>) -> Unit,
    val onItemCreated: (T) -> Unit,
    val itemLabel: (T) -> String,
    val itemSearchText: (T) -> String = itemLabel,
    val createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
    val placeholder: String = "Search $label...",
    val validate: ((List<T>) -> Boolean)? = null,
) : FormItemState {
    override fun isValidated(): Boolean =
        (!mandatory || selectedItems.isNotEmpty()) && (validate?.invoke(selectedItems) ?: true)
}

data class DateFormItemState(
    val label: String,
    override val mandatory: Boolean = false,
    val value: LocalDate?,
    val onValueChange: (LocalDate?) -> Unit,
    val validate: ((LocalDate?) -> Boolean)? = null,
) : FormItemState {
    override fun isValidated(): Boolean =
        (!mandatory || value != null) && (validate?.invoke(value) ?: true)
}
