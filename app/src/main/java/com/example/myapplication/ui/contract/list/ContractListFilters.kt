package com.example.myapplication.ui.contract.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Owner

internal fun List<ContractListItem>.filtered(
    query: String, ownerId: Long?, paymentDue: Boolean, expiresSoon: Boolean, hikeThisMonth: Boolean
): List<ContractListItem> = filter { item ->
    item.contract.property.toString().contains(query.trim(), ignoreCase = true) &&
        (ownerId == null || item.contract.property.owner.id == ownerId) &&
        (!paymentDue || item.paymentDue) &&
        (!expiresSoon || item.expiresSoon) &&
        (!hikeThisMonth || item.hikeThisMonth)
}

@Composable
internal fun ContractListFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    owners: List<Owner>,
    ownerId: Long?,
    onOwnerChange: (Long?) -> Unit,
    paymentDue: Boolean,
    onPaymentDueChange: (Boolean) -> Unit,
    expiresSoon: Boolean,
    onExpiresSoonChange: (Boolean) -> Unit,
    hikeThisMonth: Boolean,
    onHikeThisMonthChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    showStatusFilters: Boolean = true,
    modifier: Modifier = Modifier
) {
    var ownersExpanded by remember { mutableStateOf(false) }
    val hasActiveFilters = query.isNotBlank() || ownerId != null || paymentDue || expiresSoon || hikeThisMonth

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search by address, unit...") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showStatusFilters) {
                FilterChip(
                    selected = paymentDue,
                    onClick = { onPaymentDueChange(!paymentDue) },
                    label = { Text("Payment due") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )

                FilterChip(
                    selected = expiresSoon,
                    onClick = { onExpiresSoonChange(!expiresSoon) },
                    label = { Text("Expires soon") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )

                FilterChip(
                    selected = hikeThisMonth,
                    onClick = { onHikeThisMonthChange(!hikeThisMonth) },
                    label = { Text("Hike this month") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
            Box {
                FilterChip(
                    selected = ownerId != null,
                    onClick = { ownersExpanded = true },
                    label = {
                        Text(
                            if (ownerId == null) "All owners"
                            else owners.firstOrNull { it.id == ownerId }?.name ?: "Owner"
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
                        text = { Text("All owners") },
                        onClick = {
                            onOwnerChange(null)
                            ownersExpanded = false
                        }
                    )
                    owners.forEach { owner ->
                        DropdownMenuItem(
                            text = { Text(owner.name) },
                            onClick = {
                                onOwnerChange(owner.id)
                                ownersExpanded = false
                            }
                        )
                    }
                }
            }

            if (hasActiveFilters) {
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Clear all", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
