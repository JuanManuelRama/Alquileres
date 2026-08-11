package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeaheadFormItem(
    item: TypeaheadItemState,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = remember(item.value, item.options) {
        if (item.value.isBlank()) {
            item.options
        } else {
            item.options.filter { it.value.contains(item.value, ignoreCase = true) }
        }
    }

    val exactMatch = item.options.any { it.value.equals(item.value, ignoreCase = true) }
    val canCreate = item.onCreateNew != null && item.value.isNotBlank() && !exactMatch
    val hasMenuContent = filteredOptions.isNotEmpty() || canCreate

    ExposedDropdownMenuBox(
        expanded = expanded && hasMenuContent,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = item.value,
            onValueChange = {
                item.onValueChange(it)
                expanded = true
            },
            label = { Text(item.field + if (item.mandatory) " *" else "") },
            singleLine = true,
            trailingIcon = { TrailingIcon(expanded = expanded && hasMenuContent) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded && hasMenuContent,
            onDismissRequest = { expanded = false }
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.value) },
                    onClick = {
                        item.onOptionSelected(option)
                        expanded = false
                    }
                )
            }

            if (canCreate) {
                if (filteredOptions.isNotEmpty()) HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Create \"${item.value}\"") },
                    leadingIcon = { Icon(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = "hola") },
                    onClick = {
                        item.onCreateNew.invoke(item.value)
                        expanded = false
                    }
                )
            }
        }
    }
}