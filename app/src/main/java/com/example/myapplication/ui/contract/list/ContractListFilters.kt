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

@Composable
internal fun ContractListFilters(
    state: ContractListFilterState,
    owners: List<Owner>,
    showStatusFilters: Boolean = true,
    showDateFilters: Boolean = false,
    modifier: Modifier = Modifier
) {
    var ownersExpanded by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showFinishPicker by remember { mutableStateOf(false) }

    if (showStartPicker && showDateFilters) {
        MonthPickerDialog(
            title = "Filter by start date",
            initialMonth = state.startingMonth,
            onMonthSelected = {
                state.startingMonth = it
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showFinishPicker && showDateFilters) {
        MonthPickerDialog(
            title = "Filter by end date",
            initialMonth = state.finishingMonth,
            onMonthSelected = {
                state.finishingMonth = it
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
            value = state.query,
            onValueChange = { state.query = it },
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
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { state.query = "" }) {
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
                    selected = state.paymentDue,
                    onClick = { state.paymentDue = !state.paymentDue },
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
                    selected = state.expiresSoon,
                    onClick = { state.expiresSoon = !state.expiresSoon },
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
                    selected = state.hikeThisMonth,
                    onClick = { state.hikeThisMonth = !state.hikeThisMonth },
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

            if (showDateFilters) {
                FilterChip(
                    selected = state.startingMonth != null,
                    onClick = { showStartPicker = true },
                    label = {
                        Text(
                            state.startingMonth?.let { "From " + it.format(DateTimeFormatter.ofPattern("MMM yyyy")) } ?: "From date"
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
                        if (state.startingMonth != null) {
                            IconButton(
                                onClick = { state.startingMonth = null },
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

            if (showDateFilters) {
                FilterChip(
                    selected = state.finishingMonth != null,
                    onClick = { showFinishPicker = true },
                    label = {
                        Text(
                            state.finishingMonth?.let { "To " + it.format(DateTimeFormatter.ofPattern("MMM yyyy")) } ?: "To date"
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
                        if (state.finishingMonth != null) {
                            IconButton(
                                onClick = { state.finishingMonth = null },
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
                    selected = state.ownerId != null,
                    onClick = { ownersExpanded = true },
                    label = {
                        Text(
                            if (state.ownerId == null) "All owners"
                            else owners.firstOrNull { it.id == state.ownerId }?.name ?: "Owner"
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
                            state.ownerId = null
                            ownersExpanded = false
                        }
                    )
                    owners.forEach { owner ->
                        DropdownMenuItem(
                            text = { Text(owner.name) },
                            onClick = {
                                state.ownerId = owner.id
                                ownersExpanded = false
                            }
                        )
                    }
                }
            }

            if (state.hasActiveFilters) {
                TextButton(
                    onClick = state::clear,
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
