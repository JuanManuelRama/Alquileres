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
import androidx.compose.material.icons.filled.HourglassEmpty
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Owner
import com.example.myapplication.ui.util.rememberDateFormatters
import java.time.Month
import java.time.YearMonth

@Composable
internal fun ContractListFilters(
    state: ContractListFilterState,
    owners: List<Owner>,
    showStatusFilters: Boolean = true,
    showDateFilters: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dates = rememberDateFormatters()
    var ownersExpanded by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showFinishPicker by remember { mutableStateOf(false) }

    if (showStartPicker && showDateFilters) {
        MonthPickerDialog(
            title = stringResource(R.string.filter_start_date_title),
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
            title = stringResource(R.string.filter_end_date_title),
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
            placeholder = { Text(stringResource(R.string.action_search_placeholder_contracts)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.cd_search_icon)
                )
            },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { state.query = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_clear_search)
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
                    label = { Text(stringResource(R.string.filter_due)) },
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
                    label = { Text(stringResource(R.string.filter_expiring)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )

                FilterChip(
                    selected = state.hikeThisMonth,
                    onClick = { state.hikeThisMonth = !state.hikeThisMonth },
                    label = { Text(stringResource(R.string.filter_hike)) },
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
                val fromFormat = stringResource(R.string.filter_from)
                FilterChip(
                    selected = state.startingMonth != null,
                    onClick = { showStartPicker = true },
                    label = {
                        Text(
                            state.startingMonth?.let { stringResource(R.string.filter_from_format, dates.shortMonth(it)) } ?: fromFormat
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
                                    contentDescription = stringResource(R.string.cd_clear_start_date),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                )
            }

            if (showDateFilters) {
                val toFormat = stringResource(R.string.filter_to)
                FilterChip(
                    selected = state.finishingMonth != null,
                    onClick = { showFinishPicker = true },
                    label = {
                        Text(
                            state.finishingMonth?.let { stringResource(R.string.filter_to_format, dates.shortMonth(it)) } ?: toFormat
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
                                    contentDescription = stringResource(R.string.cd_clear_end_date),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                )
            }

            Box {
                val ownersLabel = stringResource(R.string.filter_owners)
                val ownerLabel = stringResource(R.string.filter_owner)
                FilterChip(
                    selected = state.ownerId != null,
                    onClick = { ownersExpanded = true },
                    label = {
                        Text(
                            if (state.ownerId == null) ownersLabel
                            else owners.firstOrNull { it.id == state.ownerId }?.name ?: ownerLabel
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
                    Text(stringResource(R.string.action_clear_all), style = MaterialTheme.typography.labelMedium)
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
    val dates = rememberDateFormatters()
    var yearText by remember { mutableStateOf((initialMonth ?: YearMonth.now()).year.toString()) }
    val year = yearText.toIntOrNull()?.takeIf { it in 1..9999 }

    val previousYearCd = stringResource(R.string.cd_previous_year)
    val nextYearCd = stringResource(R.string.cd_next_year)
    val yearLabel = stringResource(R.string.label_year)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        enabled = year != null && year > 1,
                        onClick = { yearText = (year!! - 1).toString() },
                        modifier = Modifier.semantics { contentDescription = previousYearCd }
                    ) { Text("<") }
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) yearText = it },
                        label = { Text(yearLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = year == null,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        enabled = year != null && year < 9999,
                        onClick = { yearText = (year!! + 1).toString() },
                        modifier = Modifier.semantics { contentDescription = nextYearCd }
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
                                Text(dates.monthName(month))
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
                    Text(stringResource(R.string.action_clear_date))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
