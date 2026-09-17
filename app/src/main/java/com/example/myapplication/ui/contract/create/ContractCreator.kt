package com.example.myapplication.ui.contract.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.check
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isLong
import com.example.myapplication.ui.components.form.isPositive
import com.example.myapplication.ui.components.form.limitDecimals
import com.example.myapplication.ui.property.PropertyCreator
import com.example.myapplication.ui.service.ServiceCreator
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ContractCreator(
    properties: List<Property>,
    owners: List<Owner>,
    services: List<Service>,
    onCreated: (Contract) -> Unit,
    inputs: ContractInputs,
    onInputsChanged: (ContractInputs) -> Unit,
    cancel: Cancel? = null,
    modifier: Modifier = Modifier,
) {
    Form(
        submitText = "Create Contract",
        onSubmit = {
            val property = checkNotNull(inputs.selectedProperty)
            val startingMonth = checkNotNull(inputs.startMonth)
            val finishingMonth = checkNotNull(inputs.endMonth)
            val contract = Contract(
                property = property,
                tenant = Tenant(name = inputs.tenantName, phone = inputs.tenantPhone),
                rent = inputs.rent.toBigDecimal(),
                startingMonth = startingMonth,
                finishingMonth = finishingMonth,
                adjustments = Contract.generateAdjustments(
                    startingMonth,
                    finishingMonth,
                    inputs.adjustmentTime.toLongOrNull(),
                    inputs.fixedHike.toBigDecimalOrNull()?.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP)
                ),
                services = inputs.selectedServices
            )
            onCreated(contract)
        },
        cancel = cancel,
        modifier = modifier
    ) {
        PickerItem(
            label = "Property",
            items = properties,
            selectedItem = inputs.selectedProperty,
            itemLabel = { it.toString() },
            createContent = { onCreated, onCanceled ->
                PropertyCreator(
                    owners = owners,
                    onCreated = onCreated,
                    cancel = Cancel(text = "Cancel", onClick = onCanceled)
                )
            },
            onItemSelected = { onInputsChanged(inputs.copy(selectedProperty = it)) },
            onItemCreated = {
                onInputsChanged(inputs.copy(selectedProperty = it))
            }
        )
        Content {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tenant", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Item(
                        field = "Full Name",
                        value = inputs.tenantName,
                        modifier = Modifier.weight(1f).semantics { ContentType.PersonFullName },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        onValueChange = { onInputsChanged(inputs.copy(tenantName = it)) }
                    )
                    Item(
                        field = "Phone",
                        value = inputs.tenantPhone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f).semantics { ContentType.PhoneNumber },
                        onValueChange = { onInputsChanged(inputs.copy(tenantPhone = it)) }
                    )
                }
            }
        }
        Item(
            field = "Rent",
            value = inputs.rent,
            keyboardOptions = KeyboardOptions(keyboardType =  KeyboardType.Decimal),
            validate = { Validate(it).isBigDecimal().isPositive().limitDecimals(2) },
            onValueChange = { onInputsChanged(inputs.copy(rent = it)) }
        )
        MultiPickerItem(
            label = "Services",
            items = services,
            selectedItems = inputs.selectedServices,
            itemLabel = { it.name },
            createContent = { onCreated, onCanceled ->
                ServiceCreator(
                    services = services + inputs.selectedServices,
                    onCreated = onCreated,
                    cancel = Cancel(text = "Cancel", onClick = onCanceled)
                )
            },
            onItemsChanged = { onInputsChanged(inputs.copy(selectedServices = it)) },
            onItemCreated = { }
        )

        Content {
            Text("Months", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonthItem(
                    label = "Starting month",
                    value = inputs.startMonth,
                    onValueChange = { onInputsChanged(inputs.copy(startMonth = it)) },
                    modifier = Modifier.weight(1f)
                )
                MonthItem(
                    label = "Finishing month",
                    value = inputs.endMonth,
                    validate = {
                        Validate(it).check("must be on or after starting month") { date ->
                            val start = inputs.startMonth
                            start == null || date != null && date >= start
                        }
                    },
                    onValueChange = { onInputsChanged(inputs.copy(endMonth = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Content {
            Text("Adjustments", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Item(
                    field = "Adjustment Time",
                    value = inputs.adjustmentTime,
                    mandatory = false,
                    keyboardOptions = KeyboardOptions(keyboardType =  KeyboardType.Number),
                    validate = { Validate(it).isLong().isPositive() },
                    onValueChange = { onInputsChanged(inputs.copy(adjustmentTime = it)) },
                    modifier = Modifier.weight(1f)
                )
                Item(
                    field = "Fixed Hike (%)",
                    value = inputs.fixedHike,
                    mandatory = false,
                    keyboardOptions = KeyboardOptions(keyboardType =  KeyboardType.Decimal),
                    validate = { Validate(it).isBigDecimal().limitDecimals(4)
                        .check("can't have hikes without adjustment time") { inputs.adjustmentTime.isNotBlank() } },
                    onValueChange = { onInputsChanged(inputs.copy(fixedHike = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
