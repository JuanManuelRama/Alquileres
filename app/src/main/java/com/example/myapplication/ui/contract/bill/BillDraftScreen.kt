package com.example.myapplication.ui.contract.bill

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import com.example.myapplication.model.BillDraft
import java.time.YearMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDraftScreen(viewModel: BillDraftViewModel, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.savePending()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    BackHandler { viewModel.leave(onBackClick) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Prepare bill") }, navigationIcon = {
                IconButton(onClick = { viewModel.leave(onBackClick) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator()
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::retry, enabled = !state.isNavigating) { Text("Retry") }
            }
            val draft = state.draft
            if (draft != null) {
                BillCards(state.bills, state.month, !state.isNavigating, viewModel::selectMonth) {
                    Text("Draft bill - does not record a payment.", style = MaterialTheme.typography.bodySmall)
                    Text(when (state.saveStatus) {
                        DraftSaveStatus.Saved -> "Saved"
                        DraftSaveStatus.Unsaved -> "Unsaved changes"
                        DraftSaveStatus.Saving -> "Saving..."
                        DraftSaveStatus.Error -> "Not saved"
                    })
                    BillAmountField(
                        "Rent", draft.rentInput, viewModel::changeRent,
                        enabled = !state.isNavigating,
                        invalid = draft.rentInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } != true,
                        error = "Enter a positive rent amount"
                    )
                    Text("Services", style = MaterialTheme.typography.titleMedium)
                    if (draft.services.isEmpty()) Text("No services on this bill.")
                    draft.services.forEach { service ->
                        key(draft.id, service.serviceId) {
                            BillAmountField(
                                service.name, service.amountInput,
                                { viewModel.changeService(service.serviceId, it) },
                                enabled = !state.isNavigating,
                                invalid = service.amountInput.toBigDecimalOrNull() == null,
                                error = "Enter a service amount (zero is allowed)"
                            )
                        }
                    }
                    draft.total?.let { Text("Total: $${it.toPlainString()}", style = MaterialTheme.typography.titleLarge) }
                    Text(
                        if (state.readyToSend) "Ready to send"
                        else if (draft.isValid) "Waiting for changes to be saved"
                        else "Complete all amounts to make this bill ready to send.",
                        color = if (state.readyToSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BillAmountField(
    label: String, value: String, onValueChange: (String) -> Unit,
    enabled: Boolean, invalid: Boolean, error: String
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = invalid && value.isNotEmpty(),
        supportingText = { if (invalid) Text(error) }
    )
}

/** Displays a finite collection of bill cards supplied by the caller. */
@Composable
fun BillCards(
    bills: List<BillDraft>, selectedMonth: YearMonth, enabled: Boolean,
    onSelect: (YearMonth) -> Unit, content: @Composable () -> Unit
) {
    val index = bills.indexOfFirst { it.month == selectedMonth }
    if (index < 0) return
    var showBillPicker by remember { mutableStateOf(false) }
    val monthFormat = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    if (showBillPicker && enabled) {
        AlertDialog(
            onDismissRequest = { showBillPicker = false },
            title = { Text("Select bill") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    state = rememberLazyListState(initialFirstVisibleItemIndex = index)
                ) {
                    items(bills, key = { it.id }) { bill ->
                        TextButton(
                            onClick = {
                                showBillPicker = false
                                onSelect(bill.month)
                            },
                            modifier = Modifier.fillMaxWidth().semantics {
                                selected = bill.month == selectedMonth
                            }
                        ) {
                            Text(
                                bill.month.format(monthFormat) +
                                    if (bill.month == selectedMonth) " (current)" else ""
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBillPicker = false }) { Text("Cancel") }
            }
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onSelect(bills[index - 1].month) }, enabled = enabled && index > 0) { Text("Previous") }
            TextButton(
                onClick = { showBillPicker = true },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) { Text(selectedMonth.format(monthFormat)) }
            TextButton(onClick = { onSelect(bills[index + 1].month) }, enabled = enabled && index < bills.lastIndex) { Text("Next") }
        }
        key(bills[index].id) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    content()
                }
            }
        }
    }
}
