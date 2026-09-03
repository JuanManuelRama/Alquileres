package com.example.myapplication.ui.components.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> MultiEntityPicker(
    items: List<T>,
    selectedItems: List<T>,
    onItemsChanged: (List<T>) -> Unit,
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EntityPicker(
            items = items.filter { it !in selectedItems },
            selectedItem = null,
            onItemSelected = { item ->
                if (item != null) {
                    onItemsChanged(selectedItems + item)
                }
            },
            onItemCreated = { item ->
                onItemCreated(item)
                onItemsChanged(selectedItems + item)
            },
            itemLabel = itemLabel,
            itemSearchText = itemSearchText,
            createContent = createContent,
            modifier = Modifier.fillMaxWidth(),
            label = label,
            placeholder = placeholder,
            isError = isError,
            supportingText = supportingText
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            selectedItems.forEach { item ->
                InputChip(
                    selected = true,
                    onClick = { },
                    label = { Text(itemLabel(item)) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.clickable {
                                onItemsChanged(selectedItems - item)
                            }
                        )
                    }
                )
            }
        }
    }
}
