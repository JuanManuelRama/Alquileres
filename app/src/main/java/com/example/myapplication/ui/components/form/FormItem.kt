package com.example.myapplication.ui.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.example.myapplication.ui.components.picker.EntityPicker
import com.example.myapplication.ui.components.picker.MultiEntityPicker
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun FormItem(item: FormItemState, modifier: Modifier = Modifier) {
    FormItem(item, modifier, item.error(), isLastTextField = true)
}

@Composable
internal fun FormItem(
    item: FormItemState,
    modifier: Modifier,
    error: Error,
    isLastTextField: Boolean
) {
    when (item) {
        is TextFormItemState -> TextFormItemContent(item, error, isLastTextField, modifier)
        is PickerFormItemState<*> -> PickerFormItemContent(item, error, modifier)
        is MultiPickerFormItemState<*> -> MultiPickerFormItemContent(item, error, modifier)
        is MonthFormItemState -> MonthFormItemContent(item, error, modifier)
        is DateFormItemState -> DateFormItemContent(item, error, modifier)
    }
}

@Composable
private fun TextFormItemContent(
    item: TextFormItemState,
    error: Error,
    isLastTextField: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = item.value,
        onValueChange = item.onValueChange,
        readOnly = !item.modifiable,
        label = if (item.showLabel) ( { Text(item.displayLabel) } ) else null,
        prefix = item.prefix,
        textStyle = item.textStyle ?: LocalTextStyle.current,
        keyboardOptions = KeyboardOptions(
            keyboardType = item.keyboardType,
            imeAction = if (isLastTextField) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = { focusManager.clearFocus() }
        ),
        singleLine = true,
        isError = error.hasMessage,
        supportingText = { Text(error.message) },
        modifier = modifier
    )
}

@Composable
private fun <T> PickerFormItemContent(
    item: PickerFormItemState<T>,
    error: Error,
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
        label = item.displayLabel,
        placeholder = item.placeholder,
        isError = error.hasMessage,
        supportingText = error.message
    )
}

@Composable
private fun <T> MultiPickerFormItemContent(
    item: MultiPickerFormItemState<T>,
    error: Error,
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
        label = item.displayLabel,
        placeholder = item.placeholder,
        isError = error.hasMessage,
        supportingText = error.message
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFormItemContent(item: DateFormItemState, error: Error, modifier: Modifier = Modifier) {
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
        label = { Text(item.displayLabel) },
        isError = error.hasMessage,
        supportingText = { Text(error.message) },
        modifier = modifier.clickable { showDatePicker = true },
        readOnly = true,
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = if (error.hasMessage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}
