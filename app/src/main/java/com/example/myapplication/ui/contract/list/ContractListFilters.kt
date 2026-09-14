package com.example.myapplication.ui.contract.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Owner
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

internal fun List<ContractListItem>.filtered(
    query: String,
    ownerId: Long?,
    paymentDue: Boolean,
    expiresSoon: Boolean,
    hikeThisMonth: Boolean,
    startingMonth: YearMonth? = null,
    finishingMonth: YearMonth? = null,
): List<ContractListItem> = filter { item ->
    item.contract.property.toString().contains(query.trim(), ignoreCase = true) &&
        (ownerId == null || item.contract.property.owner.id == ownerId) &&
        (!paymentDue || item.paymentDue) &&
        (!expiresSoon || item.expiresSoon) &&
        (!hikeThisMonth || item.hikeThisMonth) &&
        (startingMonth == null || item.contract.finishingMonth >= startingMonth) &&
        (finishingMonth == null || item.contract.startingMonth <= finishingMonth)
}

@Composable
internal fun ContractListFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    owners: List<Owner>,
    ownerId: Long?,
    onOwnerChange: (Long?) -> Unit,
    startingMonth: YearMonth? = null,
    onStartingMonthChange: ((YearMonth?) -> Unit)? = null,
    finishingMonth: YearMonth? = null,
    onFinishingMonthChange: ((YearMonth?) -> Unit)? = null,
    paymentDue: Boolean = false,
    onPaymentDueChange: (Boolean) -> Unit = {},
    expiresSoon: Boolean = false,
    onExpiresSoonChange: (Boolean) -> Unit = {},
    hikeThisMonth: Boolean = false,
    onHikeThisMonthChange: (Boolean) -> Unit = {},
    onClear: () -> Unit,
    showStatusFilters: Boolean = true,
    showDateFilters: Boolean = false,
    modifier: Modifier = Modifier
) {
    var ownersExpanded by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showFinishPicker by remember { mutableStateOf(false) }

    val hasActiveFilters = query.isNotBlank() || ownerId != null || paymentDue ||
        expiresSoon || hikeThisMonth || startingMonth != null || finishingMonth != null

    if (showStartPicker && onStartingMonthChange != null) {
        MonthPickerDialog(
            title = "Filter by start date",
            initialMonth = startingMonth,
            onMonthSelected = {
                onStartingMonthChange(it)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showFinishPicker && onFinishingMonthChange != null) {
        MonthPickerDialog(
            title = "Filter by end date",
            initialMonth = finishingMonth,
            onMonthSelected = {
                onFinishingMonthChange(it)
                showFinishPicker = false
            },
            onDismiss = { showFinishPicker = false }
        )
    }

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

            if (showDateFilters && onStartingMonthChange != null) {
                FilterChip(
                    selected = startingMonth != null,
                    onClick = { showStartPicker = true },
                    label = {
                        Text(
                            startingMonth?.let { "From " + it.format(DateTimeFormatter.ofPattern("MMM yyyy")) } ?: "From date"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    trailingIcon = {
                        if (startingMonth != null) {
                            IconButton(
                                onClick = { onStartingMonthChange(null) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear start date",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                )
            }

            if (showDateFilters && onFinishingMonthChange != null) {
                FilterChip(
                    selected = finishingMonth != null,
                    onClick = { showFinishPicker = true },
                    label = {
                        Text(
                            finishingMonth?.let { "To " + it.format(DateTimeFormatter.ofPattern("MMM yyyy")) } ?: "To date"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    trailingIcon = {
                        if (finishingMonth != null) {
                            IconButton(
                                onClick = { onFinishingMonthChange(null) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear end date",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
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

@Composable
private fun MonthPickerDialog(
    title: String,
    initialMonth: YearMonth?,
    onMonthSelected: (YearMonth?) -> Unit,
    onDismiss: () -> Unit
) {
    var yearText by remember { mutableStateOf((initialMonth ?: YearMonth.now()).year.toString()) }
    val year = yearText.toIntOrNull()?.takeIf { it in 1..9999 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        enabled = year != null && year > 1,
                        onClick = { yearText = (year!! - 1).toString() },
                        modifier = Modifier.semantics { contentDescription = "Previous year" }
                    ) { Text("<") }
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) yearText = it },
                        label = { Text("Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = year == null,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        enabled = year != null && year < 9999,
                        onClick = { yearText = (year!! + 1).toString() },
                        modifier = Modifier.semantics { contentDescription = "Next year" }
                    ) { Text(">") }
                }
                Month.entries.chunked(3).forEach { months ->
                    Row(Modifier.fillMaxWidth()) {
                        months.forEach { month ->
                            TextButton(
                                enabled = year != null,
                                onClick = {
                                    onMonthSelected(YearMonth.of(year!!, month))
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { selected = initialMonth?.year == year && initialMonth?.month == month }
                            ) {
                                Text(month.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (initialMonth != null) {
                TextButton(onClick = {
                    onMonthSelected(null)
                    onDismiss()
                }) {
                    Text("Clear date")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
