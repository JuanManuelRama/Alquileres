package com.example.myapplication.ui.contract.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    modifier: Modifier = Modifier
) {
    var ownersExpanded by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search property") },
            placeholder = { Text("Street, number or unit") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(paymentDue, { onPaymentDueChange(!paymentDue) }, label = { Text("Payment due") })
            FilterChip(expiresSoon, { onExpiresSoonChange(!expiresSoon) }, label = { Text("Expires soon") })
            FilterChip(hikeThisMonth, { onHikeThisMonthChange(!hikeThisMonth) }, label = { Text("Hike this month") })
            Box {
                FilterChip(
                    selected = ownerId != null,
                    onClick = { ownersExpanded = true },
                    label = { Text(if (ownerId == null) "All owners" else owners.firstOrNull { it.id == ownerId }?.name ?: "Selected owner") }
                )
                DropdownMenu(expanded = ownersExpanded, onDismissRequest = { ownersExpanded = false }) {
                    DropdownMenuItem(text = { Text("All owners") }, onClick = {
                        onOwnerChange(null)
                        ownersExpanded = false
                    })
                    owners.forEach { owner ->
                        DropdownMenuItem(text = { Text(owner.name) }, onClick = {
                            onOwnerChange(owner.id)
                            ownersExpanded = false
                        })
                    }
                }
            }
            if (query.isNotEmpty() || ownerId != null || paymentDue || expiresSoon || hikeThisMonth) {
                TextButton(onClick = onClear) { Text("Clear filters") }
            }
        }
    }
}
