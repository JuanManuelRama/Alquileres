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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Bill
import com.example.myapplication.model.BillItem
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.check
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.limitDecimals
import com.example.myapplication.ui.components.form.maxLen
import com.example.myapplication.ui.components.form.noInvalidChars
import com.example.myapplication.ui.util.LocalNumberLocale
import com.example.myapplication.ui.util.getCurrencySymbol
import com.example.myapplication.ui.util.rememberDateFormatters
import com.example.myapplication.ui.util.rememberNumberVisualTransformation
import com.example.myapplication.ui.util.toInputBigDecimal
import com.example.myapplication.ui.util.toInputBigDecimalOrNull
import com.example.myapplication.ui.util.toMoney
import kotlinx.serialization.builtins.ListSerializer
import java.math.BigDecimal
import java.time.LocalDate

@Composable
internal fun BillCompletionForm(
    bill: Bill,
    isSaving: Boolean,
    onSubmit: (Bill) -> Unit,
    onMarkPaid: (Bill, LocalDate) -> Unit,
    isInactive: Boolean,
    onChangesChanged: (Boolean) -> Unit,
    paymentFailed: Boolean,
    onSendMessage: ((Bill) -> Unit)? = null,
    isPaying: Boolean,
) {
    val dates = rememberDateFormatters()
    val isFrozen = isInactive || bill.isPaid
    val initialItems = rememberSerializable(bill.id, bill.isPaid, serializer = ListSerializer(BillItemInput.serializer())) {
        bill.items.map { item ->
            BillItemInput(
                description = item.description,
                amount = item.amount?.toPlainString().orEmpty(),
                isExtra = item.isExtra
            )
        }
    }
    var items by rememberSerializable(bill.id, bill.isPaid) {
        mutableStateOf(initialItems)
    }
    val draftItems = items.map { it.toBillItem() }
    val draftValid = items.all { it.isValidForSaving }
    val hasChanges = !isFrozen && (!draftValid || draftItems != bill.items)
    var observedItems by remember { mutableStateOf<List<BillItemInput>?>(null) }
    SideEffect { onChangesChanged(hasChanges) }
    LaunchedEffect(items, isFrozen) {
        val edited = observedItems != null && observedItems != items
        observedItems = items
        if (!isFrozen && (hasChanges || edited) && draftValid) onSubmit(bill.copy(items = draftItems))
    }
    var confirmingPayment by rememberSaveable(bill.id, bill.isPaid) { mutableStateOf(false) }
    val total = bill.rent + items.sumOf { it.amount.toInputBigDecimalOrNull() ?: BigDecimal.ZERO }

    fun completedBill() = bill.copy(
        items = items.map { item ->
            BillItem(item.description.trim(), item.amount.toInputBigDecimal(), item.isExtra)
        }
    )


    val rentLabel = stringResource(R.string.label_rent)
    val itemNameLabel = stringResource(R.string.label_item_name)
    val extraAmountLabel = stringResource(R.string.label_extra_amount)
    val removeExtraCd = stringResource(R.string.cd_remove_extra_item)
    val addBillItemCd = stringResource(R.string.cd_add_bill_item)
    val totalLabel = stringResource(R.string.label_total)

    Form(
        showActions = !isFrozen,
        submitText = stringResource(R.string.action_mark_as_paid),
        onSubmit = { confirmingPayment = true },
        cancel = onSendMessage?.let { sendMessage ->
            Cancel(
                text = stringResource(R.string.action_send_message),
                onClick = { sendMessage(completedBill()) },
                requiresValidForm = true,
                icon = Icons.AutoMirrored.Filled.Send
            )
        }
    ) {
        Content {
            if (isInactive && !bill.isPaid) {
                Text(
                    text = stringResource(R.string.bill_status_unpaid),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (bill.isPaid) {
                val formated = dates.date(bill.paidAt!!)
                Text(
                    text = stringResource(R.string.bill_status_paid_on, formated),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_item),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.label_amount),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            BillAmountRow(rentLabel) {
                Item(
                    field = rentLabel,
                    value = bill.rent.toMoney(LocalNumberLocale.current),
                    prefix = { Text(getCurrencySymbol()) },
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
                                    onClick = { items = items.filterNot { it.key == item.key } },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .semantics { contentDescription = removeExtraCd }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Item(
                                    field = itemNameLabel,
                                    value = item.description,
                                    showLabel = false,
                                    mandatory = true,
                                    modifier = Modifier.weight(1f).semantics { contentDescription = itemNameLabel },
                                    validate = { Validate(it).noInvalidChars().maxLen(50) },
                                    onValueChange = { description ->
                                        items = items.map {
                                            if (it.key == item.key) it.copy(description = description) else it
                                        }
                                    }
                                )
                            }
                        }) else null
                    ) {
                        Item(
                            field = if (item.isExtra) extraAmountLabel else item.description,
                            modifier = Modifier.semantics {
                                contentDescription = if (item.isExtra) extraAmountLabel else item.description
                            },
                            value = item.amount,
                            modifiable = !isFrozen,
                            prefix = { Text(getCurrencySymbol()) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                            showLabel = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.DecimalSigned),
                            validate = { Validate(it).maxLen(12).isBigDecimal().limitDecimals(2) },
                            visualTransformation = rememberNumberVisualTransformation(),
                            onValueChange = { amount ->
                                items = items.map {
                                        if (it.key == item.key) it.copy(amount = amount) else it

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
                    onClick = { items = items + BillItemInput("", "", true) },
                    modifier = Modifier.semantics { contentDescription = addBillItemCd }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_add_extra_item))
                }
            }
        }

        Content {
            HorizontalDivider(thickness = 2.dp)
            BillAmountRow(totalLabel, total = true) {
                Item(
                    field = totalLabel,
                    value = total.toMoney(LocalNumberLocale.current),
                    modifiable = false,
                    mandatory = false,
                    showLabel = false,
                    prefix = { Text(getCurrencySymbol()) },
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.End),
                    onValueChange = {}
                )
            }
        }

        if (!isFrozen) {
            Content {
                if (confirmingPayment && isValid) {
                    PaymentConfirmation(
                        bill = completedBill(),
                        isSaving = isPaying,
                        paymentFailed = paymentFailed,
                        onConfirm = { date ->
                            if (!isSaving) onMarkPaid(completedBill(), date)
                        },
                        onDismiss = { if (!isSaving) confirmingPayment = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentConfirmation(
    bill: Bill,
    isSaving: Boolean,
    paymentFailed: Boolean,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val dates = rememberDateFormatters()
    var paymentDay by rememberSaveable(bill.id) { mutableStateOf<Long?>(LocalDate.now().toEpochDay()) }
    val paymentDate = paymentDay?.let(LocalDate::ofEpochDay)
    val futureDateError = stringResource(R.string.validation_payment_date_future)
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.bill_confirm_payment_title)) },
        text = {
            Form(
                enabled = !isSaving,
                submitText = stringResource(if (isSaving) R.string.action_saving else R.string.action_confirm_payment),
                onSubmit = { if (!isSaving) paymentDate?.let(onConfirm) },
                cancel = Cancel(text = stringResource(R.string.action_cancel), onClick = onDismiss)
            ) {
                Content {
                    val monthText = dates.month(bill.month)
                    Text("$monthText - ${bill.total.toMoney(LocalNumberLocale.current)}")
                    Text(stringResource(R.string.bill_confirm_payment_message))
                    if (paymentFailed) {
                        Text(stringResource(R.string.err_payment_save_failed), color = MaterialTheme.colorScheme.error)
                    }
                }
                if (isSaving) Content {
                    Text("${stringResource(R.string.label_payment_date)}: ${paymentDate?.let(dates::date).orEmpty()}")
                } else DateItem(
                    label = stringResource(R.string.label_payment_date),
                    value = paymentDate,
                    validate = { date ->
                        Validate(date).check(futureDateError) {
                            it != null && !it.isAfter(LocalDate.now())
                        }
                    },
                    onValueChange = { if (!isSaving) paymentDay = it?.toEpochDay() }
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
