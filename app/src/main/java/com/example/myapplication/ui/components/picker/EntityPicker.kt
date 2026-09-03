package com.example.myapplication.ui.components.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
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
    supportingText: String? = null
) {
    var mode by remember { mutableStateOf(EntityPickerMode.SELECT) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    fun collapseCreator() {
        mode = EntityPickerMode.SELECT
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
            onItemSelected = onItemSelected,
            onCreateNew = {
                onItemSelected(null)
                mode = EntityPickerMode.CREATE
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

        if (mode == EntityPickerMode.CREATE) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    createContent(
                        {
                            onItemCreated(it)
                            collapseCreator()
                        },
                        { collapseCreator() }
                    )
                }
            }
        }
    }
}

private enum class EntityPickerMode {
    SELECT,
    CREATE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EntitySelection(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    onCreateNew: () -> Unit,
    itemLabel: (T) -> String,
    itemSearchText: (T) -> String,
    modifier: Modifier,
    label: String,
    placeholder: String,
    isError: Boolean,
    supportingText: String?
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

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
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            OutlinedTextField(
                value = selectedItem?.let(itemLabel) ?: query,
                onValueChange = {
                    query = it
                    expanded = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                        enabled = true
                    ),
                placeholder = {
                    Text(placeholder)
                },
                singleLine = true,
                isError = isError,
                supportingText = supportingText?.let { { Text(it) } },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
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
                            query = ""
                            expanded = false
                        }
                    )
                }

                HorizontalDivider()

                DropdownMenuItem(
                    text = {
                        Text("Create new $label")
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expanded = false
                        query = ""
                        onCreateNew()
                    }
                )
            }
        }
    }
}
