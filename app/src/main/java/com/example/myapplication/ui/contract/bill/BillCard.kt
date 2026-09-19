package com.example.myapplication.ui.contract.bill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Bill
import com.example.myapplication.ui.util.rememberDateFormatters
import java.time.YearMonth

/** Displays a finite collection of bill cards supplied by the caller. */
@Composable
fun BillCards(
    bills: List<Bill>,
    selectedMonth: YearMonth,
    enabled: Boolean,
    onSelect: (YearMonth) -> Unit,
    content: @Composable () -> Unit
) {
    val dates = rememberDateFormatters()
    val billStates = rememberSaveableStateHolder()
    val index = bills.indexOfFirst { it.month == selectedMonth }
    if (index < 0) return

    var showBillPicker by remember { mutableStateOf(false) }

    if (showBillPicker && enabled) {
        val currentSuffix = stringResource(R.string.bill_current_suffix)
        AlertDialog(
            onDismissRequest = { showBillPicker = false },
            title = { Text(stringResource(R.string.bill_select_title)) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    state = rememberLazyListState(initialFirstVisibleItemIndex = index)
                ) {
                    items(bills, key = { it.id }) { bill ->
                        val isCurrent = bill.month == selectedMonth
                        TextButton(
                            onClick = {
                                showBillPicker = false
                                onSelect(bill.month)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { selected = isCurrent }
                        ) {
                            Text(
                                text = dates.month(bill.month) + if (isCurrent) currentSuffix else ""
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBillPicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onSelect(bills[index - 1].month) },
                enabled = enabled && index > 0
            ) {
                Text(stringResource(R.string.action_previous))
            }
            TextButton(
                onClick = { showBillPicker = true },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(dates.month(selectedMonth))
            }
            TextButton(
                onClick = { onSelect(bills[index + 1].month) },
                enabled = enabled && index < bills.lastIndex
            ) {
                Text(stringResource(R.string.action_next))
            }
        }
        billStates.SaveableStateProvider(bills[index].id) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}
