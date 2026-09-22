package com.example.myapplication.ui.components.filter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Owner

@Composable
internal fun ListSearchField(query: String, onQueryChanged: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.cd_search_icon)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_clear_search)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun OwnerFilterChip(ownerId: Long?, owners: List<Owner>, onOwnerSelected: (Long?) -> Unit) {
    var ownersExpanded by remember { mutableStateOf(false) }
    Box {
        val ownersLabel = stringResource(R.string.filter_owners)
        val ownerLabel = stringResource(R.string.filter_owner)
        FilterChip(
            selected = ownerId != null,
            onClick = { ownersExpanded = true },
            label = {
                Text(
                    if (ownerId == null) ownersLabel
                    else owners.firstOrNull { it.id == ownerId }?.name ?: ownerLabel
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )
        DropdownMenu(
            expanded = ownersExpanded,
            onDismissRequest = { ownersExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all_owners)) },
                onClick = {
                    onOwnerSelected(null)
                    ownersExpanded = false
                }
            )
            owners.forEach { owner ->
                DropdownMenuItem(
                    text = { Text(owner.name) },
                    onClick = {
                        onOwnerSelected(owner.id)
                        ownersExpanded = false
                    }
                )
            }
        }
    }
}
