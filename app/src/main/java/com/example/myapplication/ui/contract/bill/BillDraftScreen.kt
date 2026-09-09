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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isPositive
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDraftScreen(viewModel: BillDraftViewModel, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onBack = { if (!state.isSaving) onBackClick() }
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Prepare bill") }, navigationIcon = {
                IconButton(onClick = onBack, enabled = !state.isSaving) {
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
                if (state.draft == null) TextButton(onClick = viewModel::retry) { Text("Retry") }
            }
            val draft = state.draft
            if (draft != null) {
                BillCards(state.bills, state.month, !state.isSaving, viewModel::selectMonth) {
                    BillCompletionForm(
                        bill = draft, isSaving = state.isSaving, saved = state.saved,
                        onSubmit = viewModel::save, onCancel = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun BillCompletionForm(
    bill: BillDraft, isSaving: Boolean, saved: Boolean,
    onSubmit: (BillDraft) -> Unit, onCancel: () -> Unit
) {
    var input by remember(bill) { mutableStateOf(bill) }
    Form(
        submitText = if (isSaving) "Saving..." else "Save bill",
        onSubmit = { if (!isSaving) onSubmit(input) },
        cancelText = "Cancel",
        onCancel = { if (!isSaving) onCancel() }
    ) {
        Content {
            Text("Complete the amounts, then save this bill.", style = MaterialTheme.typography.bodySmall)
            if (saved && input == bill) Text("Bill saved.", color = MaterialTheme.colorScheme.primary)
        }
        Item(
            field = "Rent", value = input.rentInput,
            keyboardType = KeyboardType.Decimal,
            validate = { Validate(it).isBigDecimal().isPositive() },
            onValueChange = { if (!isSaving) input = input.copy(rentInput = it) }
        )
        Content {
            Text("Services", style = MaterialTheme.typography.titleMedium)
            if (input.services.isEmpty()) Text("No services on this bill.")
        }
        input.services.forEach { service ->
            key(service.serviceId) {
                Item(
                    field = service.name, value = service.amountInput,
                    keyboardType = KeyboardType.Decimal,
                    validate = { Validate(it).isBigDecimal() },
                    onValueChange = { amount ->
                        if (!isSaving) input = input.copy(services = input.services.map {
                            if (it.serviceId == service.serviceId) it.copy(amountInput = amount) else it
                        })
                    }
                )
            }
        }
        Content {
            input.total?.let { Text("Total: $" + it.toPlainString(), style = MaterialTheme.typography.titleLarge) }
        }
    }
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
