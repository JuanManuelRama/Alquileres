package com.example.myapplication.ui.contract.bill

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.model.Bill
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.check
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isPositive
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    viewModel: BillViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onBack = { if (!state.isSaving) onBackClick() }
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Bills") }, navigationIcon = {
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
                if (state.bill == null) TextButton(onClick = viewModel::retry) { Text("Retry") }
            }
            val bill = state.bill
            if (bill != null) {
                BillCards(state.bills, state.month, !state.isSaving, viewModel::selectMonth) {
                    BillCompletionForm(
                        bill = bill,
                        isSaving = state.isSaving,
                        onSubmit = viewModel::save,
                        onMarkPaid = viewModel::markPaid
                    )
                }
            }
        }
    }
}

@Composable
private fun BillCompletionForm(
    bill: Bill, isSaving: Boolean,
    onSubmit: (Bill) -> Unit,
    onMarkPaid: (Bill, LocalDate) -> Unit
) {
    var input by remember(bill) { mutableStateOf(bill) }
    var confirmingPayment by remember(bill.id) { mutableStateOf(false) }
    if (confirmingPayment && !bill.isPaid) {
        PaymentConfirmation(
            bill = input,
            onConfirm = { date ->
                confirmingPayment = false
                onMarkPaid(input, date)
            },
            onDismiss = { confirmingPayment = false }
        )
    }
    Form(
        showActions = !bill.isPaid,
        enabled = !isSaving,
        submitText = if (isSaving) "Saving..." else "Save bill",
        onSubmit = { if (!isSaving && !bill.isPaid) onSubmit(input) },
    ) {
        Content {
            if (bill.isPaid) {
                Text("Paid on " + bill.paidAt!!.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("This bill is locked.", style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Item", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Amount", Modifier.weight(1f), textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider()
            BillAmountRow("Rent") {
                Item(
                    field = "Rent", value = input.rentInput,
                    prefix = { Text("$") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                    showLabel = false,
                    keyboardType = KeyboardType.Decimal,
                    validate = { Validate(it).isBigDecimal().isPositive() },
                    onValueChange = { if (!isSaving && !bill.isPaid) input = input.copy(rentInput = it) },
                    modifiable = false,
                )
            }
        }
        input.items.forEachIndexed { position, item ->
            key(position) {
                Content {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    BillAmountRow(item.description) {
                        Item(
                            field = item.description,
                            value = item.amount,
                            modifiable = !bill.isPaid && !isSaving,
                            prefix = { Text("$") },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                            showLabel = false,
                            keyboardType = KeyboardType.Decimal,
                            validate = { Validate(it).isBigDecimal() },
                            onValueChange = { amount ->
                                if (!isSaving && !bill.isPaid) input = input.copy(items = input.items.mapIndexed { index, current ->
                                    if (index == position) current.copy(amount = amount) else current
                                })
                            }
                        )
                    }
                }
            }
        }
        Content {
            HorizontalDivider(thickness = 2.dp)
            BillAmountRow("Total", total = true) {
                Item(
                    field = "Total",
                    value = input.rentInput.toBigDecimalOrNull()?.let { input.total.toPlainString() } ?: "",
                    modifiable = false,
                    mandatory = false,
                    showLabel = false,
                    prefix = { Text("$") },
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.End)
                )
            }
        }
        if (!bill.isPaid) {
            Content {
                Button(
                    onClick = { confirmingPayment = true },
                    enabled = input.isValid && !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Mark as paid") }
            }
        }
    }
}

@Composable
private fun PaymentConfirmation(
    bill: Bill,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var paymentDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm payment?") },
        text = {
            Form(
                submitText = "Confirm payment",
                onSubmit = { paymentDate?.let(onConfirm) },
                cancel = Cancel(text = "Cancel", onClick = onDismiss)
            ) {
                Content {
                    Text(bill.month.format(DateTimeFormatter.ofPattern("MMMM yyyy")) + " - $" + bill.total.toPlainString())
                    Text("This will save these amounts, mark it as paid, and permanently lock this bill. You won't be able to edit it afterward.")
                }
                DateItem(
                    label = "Payment date", value = paymentDate,
                    validate = { date -> Validate(date).check("Payment date cannot be in the future") {
                        it != null && !it.isAfter(LocalDate.now())
                    } },
                    onValueChange = { paymentDate = it }
                )
            }
        },
        confirmButton = {}
    )
}

/** Keep every amount box on the same column, including the calculated total. */
@Composable
private fun BillAmountRow(
    label: String,
    total: Boolean = false,
    amount: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.weight(1f).heightIn(min = 56.dp), contentAlignment = Alignment.CenterStart) {
            Text(label, style = if (total) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
        }
        Box(modifier = Modifier.weight(1f)) { amount() }
    }
}

/** Displays a finite collection of bill cards supplied by the caller. */
@Composable
fun BillCards(
    bills: List<Bill>,
    selectedMonth: YearMonth,
    enabled: Boolean,
    onSelect: (YearMonth) -> Unit,
    content: @Composable () -> Unit
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
