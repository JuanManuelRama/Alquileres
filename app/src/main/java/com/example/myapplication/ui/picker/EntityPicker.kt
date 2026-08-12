package com.example.myapplication.ui.picker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun <T> EntityPicker(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    onItemCreated: (T) -> Unit,
    itemLabel: (T) -> String,
    itemSearchText: (T) -> String = itemLabel,
    createContent: @Composable (onCreated: (T) -> Unit, onCancel: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String = "Search...",
) {
    var mode by remember { mutableStateOf(EntityPickerMode.SELECT) }

    when (mode) {
        EntityPickerMode.SELECT -> {
            EntitySelection(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                onCreateNew = {
                    mode = EntityPickerMode.CREATE
                },
                itemLabel = itemLabel,
                itemSearchText = itemSearchText,
                modifier = modifier,
                label = label,
                placeholder = placeholder,
            )
        }

        EntityPickerMode.CREATE -> {
            createContent(
                { item ->
                    onItemCreated(item)
                    mode = EntityPickerMode.SELECT
                },
                { mode = EntityPickerMode.SELECT },
            )
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
            style = MaterialTheme.typography.labelLarge
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
                    .menuAnchor(),
                placeholder = {
                    Text(placeholder)
                },
                singleLine = true,
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