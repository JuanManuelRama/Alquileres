package com.example.myapplication.ui.contract.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.PhoneItem
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.check
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isLong
import com.example.myapplication.ui.components.form.isPositive
import com.example.myapplication.ui.components.form.limitDecimals
import com.example.myapplication.ui.components.form.phoneNumberDigits
import com.example.myapplication.ui.components.picker.PhoneContactPicker
import com.example.myapplication.ui.components.picker.PickerInputs
import com.example.myapplication.ui.components.picker.queryPhoneDetails
import com.example.myapplication.ui.property.PropertyCreator
import com.example.myapplication.ui.property.PropertyInputs
import com.example.myapplication.ui.service.ServiceCreator
import com.example.myapplication.ui.service.ServiceInputs
import com.example.myapplication.ui.util.getCurrencySymbol
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
    enabled: Boolean = true,
    isSaving: Boolean = false,
) {
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = PhoneContactPicker()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val (name, phone) = queryPhoneDetails(context, uri)
        onInputsChanged(
            inputs.copy(
                tenantName = name.ifBlank { inputs.tenantName },
                tenantPhone = phone.ifBlank { inputs.tenantPhone }
            ))
    }

    val cancelText = stringResource(R.string.action_cancel)
    val pickContactText = stringResource(R.string.action_pick_contact)
    val monthValidationError = stringResource(R.string.validation_end_month_after_start)
    val hikeValidationError = stringResource(R.string.validation_hike_requires_adjustment_time)

    Form(
        enabled = enabled,
        submitText = stringResource(if (isSaving) R.string.action_saving else R.string.action_create_contract),
        onSubmit = {
            val property = checkNotNull(inputs.selectedProperty)
            val startingMonth = checkNotNull(inputs.startMonth)
            val finishingMonth = checkNotNull(inputs.endMonth)
            val contract = Contract(
                property = property,
                tenant = Tenant(name = inputs.tenantName, phone = phoneNumberDigits(inputs.tenantPhone)),
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
            label = stringResource(R.string.label_property),
            items = properties,
            selectedItem = inputs.selectedProperty,
            pickerInputs = inputs.propertyPicker,
            onPickerInputsChanged = {
                onInputsChanged (inputs.copy(
                    propertyPicker = it,
                    propertyDraft = if (it.isCreating) inputs.propertyDraft else PropertyInputs()
                ))
            },
            itemLabel = { it.toString() },
            createContent = { onCreated, onCanceled ->
                PropertyCreator(
                    inputs = inputs.propertyDraft,
                    onInputsChanged = { onInputsChanged(inputs.copy(propertyDraft = it)) },
                    owners = owners,
                    onCreated = onCreated,
                    cancel = Cancel(text = cancelText, onClick = onCanceled)
                )
            },
            onItemSelected = { onInputsChanged(inputs.copy(selectedProperty = it, propertyPicker = PickerInputs(), propertyDraft = PropertyInputs())) },
            onItemCreated = {
                onInputsChanged(inputs.copy(selectedProperty = it, propertyPicker = PickerInputs(), propertyDraft = PropertyInputs()))
            }
        )
        Content {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.label_tenant), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { contactPickerLauncher.launch(Unit) }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = pickContactText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(pickContactText)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Item(
                        field = stringResource(R.string.label_full_name),
                        value = inputs.tenantName,
                        modifier = Modifier.weight(1f).semantics { ContentType.PersonFullName },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words
                        ),
                        onValueChange = { onInputsChanged(inputs.copy(tenantName = it)) }
                    )
                    PhoneItem(
                        field = stringResource(R.string.label_phone),
                        value = inputs.tenantPhone,
                        modifier = Modifier.weight(1f).semantics { ContentType.PhoneNumber },
                        onValueChange = { onInputsChanged(inputs.copy(tenantPhone = it)) }
                    )
                }
            }
        }
        Item(
            field = stringResource(R.string.label_rent),
            value = inputs.rent,
            keyboardOptions = KeyboardOptions(keyboardType =  KeyboardType.Decimal),
            validate = { Validate(it).isBigDecimal().isPositive().limitDecimals(2) },
            prefix = { Text(getCurrencySymbol()) },
            onValueChange = { onInputsChanged(inputs.copy(rent = it)) }
        )
        MultiPickerItem(
            label = stringResource(R.string.label_services),
            items = services,
            selectedItems = inputs.selectedServices,
            pickerInputs = inputs.servicePicker,
            onPickerInputsChanged = {
                onInputsChanged(inputs.copy(servicePicker = it,
                    serviceDraft = if (it.isCreating) inputs.serviceDraft else ServiceInputs()))
            },
            itemLabel = { it.name },
            createContent = { onCreated, onCanceled ->
                ServiceCreator(
                    inputs = inputs.serviceDraft,
                    onInputsChanged = { onInputsChanged(inputs.copy(serviceDraft = it)) },
                    services = services + inputs.selectedServices,
                    onCreated = onCreated,
                    cancel = Cancel(text = cancelText, onClick = onCanceled)
                )
            },
            onItemsChanged = {
                onInputsChanged(if (it.size > inputs.selectedServices.size) {
                    inputs.copy(selectedServices = it, servicePicker = PickerInputs(), serviceDraft = ServiceInputs())
                } else inputs.copy(selectedServices = it))
            },
            onItemCreated = { }
        )

        Content {
            Text(stringResource(R.string.header_months), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonthItem(
                    label = stringResource(R.string.label_starting_month),
                    value = inputs.startMonth,
                    onValueChange = { onInputsChanged(inputs.copy(startMonth = it)) },
                    modifier = Modifier.weight(1f)
                )
                MonthItem(
                    label = stringResource(R.string.label_finishing_month),
                    value = inputs.endMonth,
                    validate = {
                        Validate(it).check(monthValidationError) { date ->
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
            Text(stringResource(R.string.header_adjustments), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Item(
                    field = stringResource(R.string.label_adjustment_time),
                    value = inputs.adjustmentTime,
                    mandatory = false,
                    keyboardOptions = KeyboardOptions(keyboardType =  KeyboardType.Number),
                    validate = { Validate(it).isLong().isPositive() },
                    onValueChange = { onInputsChanged(inputs.copy(adjustmentTime = it)) },
                    modifier = Modifier.weight(1f)
                )
                Item(
                    field = stringResource(R.string.label_fixed_hike),
                    value = inputs.fixedHike,
                    mandatory = false,
                    keyboardOptions = KeyboardOptions(keyboardType =  KeyboardType.Decimal),
                    validate = { Validate(it).isBigDecimal().limitDecimals(4)
                        .check(hikeValidationError) { inputs.adjustmentTime.isNotBlank() } },
                    onValueChange = { onInputsChanged(inputs.copy(fixedHike = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
