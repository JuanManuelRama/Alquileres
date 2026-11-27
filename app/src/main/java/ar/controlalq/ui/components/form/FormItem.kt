package ar.controlalq.ui.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import ar.controlalq.R
import ar.controlalq.ui.components.picker.EntityPicker
import ar.controlalq.ui.components.picker.MultiEntityPicker
import ar.controlalq.ui.util.rememberDateFormatters
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun FormItem(item: FormItemState, modifier: Modifier = Modifier) {
    FormItem(item, modifier, item.error(), onNext = null)
}

@Composable
internal fun FormItem(
    item: FormItemState,
    modifier: Modifier,
    error: Error,
    onNext: (() -> Unit)?
) {
    when (item) {
        is TextFormItemState -> TextFormItemContent(item, error, onNext, modifier)
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
    onNext: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = item.value,
        onValueChange = item.onValueChange,
        visualTransformation = item.visualTransformation,
        readOnly = !item.modifiable,
        label = if (item.showLabel) ( { Text(item.displayLabel) } ) else null,
        prefix = item.prefix,
        textStyle = item.textStyle ?: LocalTextStyle.current,
        keyboardOptions = item.keyboardOptions.copy(
            imeAction = if (onNext == null) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            // Keep keyboard navigation on this field instead of returning to the screen's first control.
            onDone = { keyboardController?.hide() }
        ),
        singleLine = true,
        isError = error.hasMessage,
        supportingText = { Text(error.renderMessage()) },
        modifier = modifier
    )
}

@Composable
private fun <T> PickerFormItemContent(
    item: PickerFormItemState<T>,
    error: Error,
    modifier: Modifier = Modifier
) {
    val defaultPlaceholder = stringResource(R.string.action_search_placeholder)
    EntityPicker(
        items = item.items,
        selectedItem = item.selectedItem,
        onItemSelected = item.onItemSelected,
        onItemCreated = item.onItemCreated,
        itemLabel = item.itemLabel,
        itemSearchText = item.itemSearchText,
        createNewLabel = item.createNewLabel,
        createContent = item.createContent,
        pickerInputs = item.pickerInputs,
        onPickerInputsChanged = item.onPickerInputsChanged,
        modifier = modifier,
        label = item.displayLabel,
        placeholder = item.placeholder ?: defaultPlaceholder,
        isError = error.hasMessage,
        supportingText = error.renderMessage()
    )
}

@Composable
private fun <T> MultiPickerFormItemContent(
    item: MultiPickerFormItemState<T>,
    error: Error,
    modifier: Modifier = Modifier
) {
    val defaultPlaceholder = stringResource(R.string.action_search_placeholder)
    MultiEntityPicker(
        items = item.items,
        selectedItems = item.selectedItems,
        onItemsChanged = item.onItemsChanged,
        onItemCreated = item.onItemCreated,
        itemLabel = item.itemLabel,
        itemSearchText = item.itemSearchText,
        createNewLabel = item.createNewLabel,
        createContent = item.createContent,
        pickerInputs = item.pickerInputs,
        onPickerInputsChanged = item.onPickerInputsChanged,
        modifier = modifier,
        label = item.displayLabel,
        placeholder = item.placeholder ?: defaultPlaceholder,
        isError = error.hasMessage,
        supportingText = error.renderMessage()
    )
}

@Composable
private fun DateFormItemContent(item: DateFormItemState, error: Error, modifier: Modifier = Modifier) {
    val dates = rememberDateFormatters()
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
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = item.value?.let(dates::shortDate) ?: "",
        onValueChange = { },
        label = { Text(item.displayLabel) },
        isError = error.hasMessage,
        supportingText = { Text(error.renderMessage()) },
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
