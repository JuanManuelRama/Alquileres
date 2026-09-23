package com.example.myapplication.ui.contract.detail

import com.example.myapplication.ui.util.toInputBigDecimal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.R
import com.example.myapplication.model.Adjustment
import com.example.myapplication.model.Contract
import com.example.myapplication.ui.SaveStatus
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isPositive
import com.example.myapplication.ui.components.form.limitDecimals
import com.example.myapplication.ui.components.form.maxLen
import com.example.myapplication.ui.util.rememberDateFormatters
import com.example.myapplication.ui.util.toMoneyWithCurrency
import com.example.myapplication.ui.util.toPercentage
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun AdjustmentCard(
    adjustment: Adjustment,
    contract: Contract,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    enabled: Boolean = true,
    saveStatus: SaveStatus = SaveStatus.Idle,
    onSaveHandled: () -> Unit = {},
    onAmountChange: (BigDecimal) -> Unit
) {
    val dates = rememberDateFormatters()
    var showEditor by rememberSaveable(adjustment.time.toString()) { mutableStateOf(false) }
    var percentage by rememberSaveable(adjustment.time.toString()) { mutableStateOf("") }
    val saving = saveStatus == SaveStatus.Saving || saveStatus == SaveStatus.Saved
    LaunchedEffect(saveStatus) {
        if (saveStatus == SaveStatus.Saved) {
            showEditor = false
            onSaveHandled()
        }
    }
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = dates.month(adjustment.time),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            if (adjustment.amount != null || editable) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = adjustment.amount?.let { "${it.multiply(100.toBigDecimal()).toPercentage()}%" }
                            ?: stringResource(R.string.label_not_set),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (editable) {
                        IconButton(
                            enabled = enabled,
                            onClick = {
                                percentage = adjustment.amount?.multiply(BigDecimal(100))
                                    ?.setScale(4, RoundingMode.HALF_UP)?.stripTrailingZeros()?.toPlainString().orEmpty()
                                showEditor = true
                            },
                            modifier = Modifier
                                .offset(y = (-7).dp)
                                .size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.cd_edit_hike),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Text(
                    text = contract.valueAtMonth(adjustment.time).toMoneyWithCurrency(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showEditor && editable) {
        Dialog(onDismissRequest = { if (!saving) { showEditor = false; onSaveHandled() } }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.title_edit_hike), style = MaterialTheme.typography.headlineSmall)
                    if (saveStatus == SaveStatus.Failed) {
                        Text(stringResource(R.string.err_could_not_save_adjustment), color = MaterialTheme.colorScheme.error)
                    }
                    Form(
                        enabled = !saving,
                        submitText = stringResource(if (saving) R.string.action_saving else R.string.action_save_hike),
                        onSubmit = {
                            onAmountChange(percentage.toInputBigDecimal().divide(BigDecimal(100)))
                        },
                        cancel = Cancel(text = stringResource(R.string.action_cancel), onClick = { showEditor = false; onSaveHandled() } )
                    ) {
                        Item(
                            field = stringResource(R.string.label_hike),
                            value = percentage,
                            modifiable = !saving,
                            keyboardOptions = KeyboardOptions(keyboardType =  KeyboardType.Decimal),
                            validate = { Validate(it).maxLen(6).isBigDecimal().isPositive().limitDecimals(4) },
                            onValueChange = { if (!saving) percentage = it }
                        )
                    }
                }
            }
        }
    }
}
