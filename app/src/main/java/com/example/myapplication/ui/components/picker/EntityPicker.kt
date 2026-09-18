package com.example.myapplication.ui.components.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import kotlinx.coroutines.launch

@Composable
fun <T> EntityPicker(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    onItemCreated: (T) -> Unit,
    itemLabel: (T) -> String,
    itemSearchText: (T) -> String = itemLabel,
    createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String = "Search...",
    isError: Boolean = false,
    supportingText: String? = null,
    pickerInputs: PickerInputs? = null,
    onPickerInputsChanged: (PickerInputs) -> Unit = {}
) {
    var localInputs by rememberSerializable { mutableStateOf(PickerInputs()) }
    val inputs = pickerInputs ?: localInputs
    fun updateInputs(value: PickerInputs) {
        if (pickerInputs == null) localInputs = value else onPickerInputsChanged(value)
    }
    var creatorFocusRequest by remember { mutableStateOf(0) }
    val creatorFocusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    fun collapseCreator() {
        if (pickerInputs == null) localInputs = PickerInputs()
        focusManager.clearFocus()
        coroutineScope.launch {
            withFrameNanos { }
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EntitySelection(
            items = items,
            selectedItem = selectedItem,
            query = inputs.query,
            onQueryChanged = { updateInputs(inputs.copy(query = it)) },
            onItemSelected = {
                if (pickerInputs == null) localInputs = PickerInputs()
                onItemSelected(it)
            },
            onCreateNew = {
                updateInputs(inputs.copy(isCreating = true))
                creatorFocusRequest++
            },
            itemLabel = itemLabel,
            itemSearchText = itemSearchText,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester),
            label = label,
            placeholder = placeholder,
            isError = isError,
            supportingText = supportingText
        )

        if (inputs.isCreating) {
            LaunchedEffect(creatorFocusRequest) {
                // Wait until the creator's input is attached before moving focus.
                withFrameNanos { }
                creatorFocusRequester.requestFocus()
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .focusRequester(creatorFocusRequester)
                ) {
                    createContent(
                        {
                            onItemCreated(it)
                            collapseCreator()
                        },
                        { updateInputs(PickerInputs()); collapseCreator() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EntitySelection(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    onCreateNew: () -> Unit,
    itemLabel: (T) -> String,
    itemSearchText: (T) -> String,
    modifier: Modifier,
    label: String,
    placeholder: String,
    isError: Boolean,
    supportingText: String?,
    query: String,
    onQueryChanged: (String) -> Unit
) {
    var expanded by remember(selectedItem) { mutableStateOf(false) }
    val hasSelection = selectedItem != null
    var inputFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(inputFocused) {
        if (inputFocused && !hasSelection) {
            expanded = true
        }
    }

    val filteredItems = remember(query, items) {
        items.filter {
            itemSearchText(it).contains(
                query,
                ignoreCase = true
            )
        }
    }

    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )

        ExposedDropdownMenuBox(
            expanded = expanded && !hasSelection,
            onExpandedChange = {
                expanded = it && !hasSelection
            }
        ) {
            OutlinedTextField(
                value = selectedItem?.let(itemLabel) ?: query,
                onValueChange = {
                    if (!hasSelection) {
                        onQueryChanged(it)
                        expanded = true
                    }
                },
                readOnly = hasSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { inputFocused = it.isFocused }
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                        enabled = !hasSelection
                    ),
                placeholder = {
                    Text(placeholder)
                },
                singleLine = true,
                isError = isError,
                supportingText = supportingText?.let { { Text(it) } },
                trailingIcon = {
                    if (hasSelection) {
                        IconButton(onClick = {
                            expanded = false
                            focusManager.clearFocus()
                            onItemSelected(null)
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_clear_selection, label)
                            )
                        }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = expanded && !hasSelection,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                filteredItems.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(itemLabel(item))
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                            focusManager.clearFocus()
                        }
                    )
                }

                HorizontalDivider()

                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.picker_create_new, label))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expanded = false
                        onCreateNew()
                    }
                )
            }
        }
    }
}
