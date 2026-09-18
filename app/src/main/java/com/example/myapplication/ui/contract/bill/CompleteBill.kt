package com.example.myapplication.ui.contract.bill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Bill
import com.example.myapplication.model.BillItem
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.check
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.limitDecimals
import kotlinx.serialization.builtins.ListSerializer
import java.math.BigDecimal
import java.time.LocalDate

@Composable
internal fun BillCompletionForm(
    bill: Bill,
    isSaving: Boolean,
    onSubmit: (Bill) -> Unit,
    onMarkPaid: (Bill, LocalDate) -> Unit,
    isInactive: Boolean = false,
    onChangesChanged: (Boolean) -> Unit = {},
    onSendMessage: ((Bill) -> Unit)? = null
) {
    val isFrozen = isInactive || bill.isPaid
    val initialItems = rememberSerializable(bill, serializer = ListSerializer(BillItemInput.serializer())) {
        bill.items.map { item ->
            BillItemInput(
                description = item.description,
                amount = item.amount?.toPlainString().orEmpty(),
                isExtra = item.isExtra
            )
        }
    }
    var items by rememberSerializable(bill) {
        mutableStateOf(initialItems)
    }
    SideEffect { onChangesChanged(!isFrozen && items != initialItems) }
    var confirmingPayment by remember(bill.id) { mutableStateOf(false) }
    val amountsValid = items.all { it.description.isNotBlank() && Validate(it.amount).isBigDecimal().limitDecimals(2).isValid }
    val total = bill.rent + items.sumOf { it.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO }

    fun completedBill() = bill.copy(
        items = items.map { item ->
            BillItem(item.description.trim(), item.amount.toBigDecimal(), item.isExtra)
        }
    )


    if (confirmingPayment && !isFrozen && amountsValid) {
        PaymentConfirmation(
            bill = completedBill(),
            onConfirm = { date ->
                confirmingPayment = false
                onMarkPaid(completedBill(), date)
            },
            onDismiss = { confirmingPayment = false }
        )
    }

    Form(
        showActions = false,
        enabled = !isSaving,
        submitText = if (isSaving) "Saving..." else "Save bill",
        onSubmit = {
            if (!isSaving && !isFrozen && amountsValid) {
                onSubmit(completedBill())
            }
        }
    ) {
        Content {
            if (isInactive && !bill.isPaid) {
                Text(
                    text = "Unpaid",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (bill.isPaid) {
                val formattedDate = bill.paidAt!!.format(BillDateFormatter)
                Text(
                    text = "Paid on $formattedDate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Item",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Amount",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            BillAmountRow("Rent") {
                Item(
                    field = "Rent",
                    value = bill.rent.toCurrencyString(),
                    prefix = { Text("$") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                    showLabel = false,
                    modifiable = false,
                    onValueChange = {}
                )
            }
        }

        items.forEach { item ->
            key(item.key) {
                Content {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    BillAmountRow(
                        label = item.description,
                        labelContent = if (item.isExtra && !isFrozen) ({
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { if (!isSaving) items = items.filterNot { it.key == item.key } },
                                    enabled = !isSaving,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .semantics { contentDescription = "Remove extra item" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Item(
                                    field = "Item name",
                                    value = item.description,
                                    showLabel = false,
                                    modifiable = !isSaving,
                                    modifier = Modifier.weight(1f).semantics { contentDescription = "Item name" },
                                    onValueChange = { description ->
                                        if (!isSaving) {
                                            items = items.map {
                                                if (it.key == item.key) it.copy(description = description) else it
                                            }
                                        }
                                    }
                                )
                            }
                        }) else null
                    ) {
                        Item(
                            field = if (item.isExtra) "Extra amount" else item.description,
                            modifier = Modifier.semantics {
                                contentDescription = if (item.isExtra) "Extra amount" else item.description
                            },
                            value = item.amount,
                            modifiable = !isFrozen && !isSaving,
                            prefix = { Text("$") },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                            showLabel = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.DecimalSigned),
                            validate = { Validate(it).isBigDecimal().limitDecimals(2) },
                            onValueChange = { amount ->
                                if (!isSaving && !isFrozen) {
                                    items = items.map {
                                        if (it.key == item.key) it.copy(amount = amount) else it
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (!isFrozen) {
            Content {
                TextButton(
                    onClick = { if (!isSaving) items = items + BillItemInput("", "", true) },
                    enabled = !isSaving,
                    modifier = Modifier.semantics { contentDescription = "Add bill item" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add extra item")
                }
            }
        }

        Content {
            HorizontalDivider(thickness = 2.dp)
            BillAmountRow("Total", total = true) {
                Item(
                    field = "Total",
                    value = total.toCurrencyString(),
                    modifiable = false,
                    mandatory = false,
                    showLabel = false,
                    prefix = { Text("$") },
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.End),
                    onValueChange = {}
                )
            }
        }

        if (!isFrozen) {
            Content {
                Button(
                    onClick = { confirmingPayment = true },
                    enabled = amountsValid && !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as paid")
                }
            }
        }
        if(!isFrozen) {
            Content {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onSendMessage != null) {
                        OutlinedButton(
                            onClick = {
                                if (amountsValid && !isSaving) onSendMessage(completedBill())
                            },
                            enabled = amountsValid && !isSaving && bill.contract.tenant.phone.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Send message")
                        }
                    }
                    Button(
                        onClick = {
                            if (amountsValid && !isSaving) onSubmit(completedBill())
                        },
                        enabled = amountsValid && !isSaving
                    ) { Text(if (isSaving) "Saving..." else "Save bill") }
                }
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
                    val monthText = bill.month.format(BillMonthFormatter)
                    Text("$monthText - $${bill.total.toCurrencyString()}")
                    Text("This will save these amounts, mark it as paid, and permanently lock this bill. You won't be able to edit it afterward.")
                }
                DateItem(
                    label = "Payment date",
                    value = paymentDate,
                    validate = { date ->
                        Validate(date).check("Payment date cannot be in the future") {
                            it != null && !it.isAfter(LocalDate.now())
                        }
                    },
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
    labelContent: (@Composable () -> Unit)? = null,
    amount: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (labelContent != null) labelContent() else Text(
                text = label,
                style = if (total) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            amount()
        }
    }
}
