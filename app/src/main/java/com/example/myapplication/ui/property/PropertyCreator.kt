package com.example.myapplication.ui.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.owner.OwnerInputs
import com.example.myapplication.ui.components.picker.PickerInputs
import com.example.myapplication.ui.owner.OwnerCreator

@Composable
fun PropertyCreator(
    inputs: PropertyInputs,
    onInputsChanged: (PropertyInputs) -> Unit,
    owners: List<Owner>,
    onCreated: (Property) -> Unit,
    cancel: Cancel? = null,
    modifier: Modifier = Modifier,
) {

    Form(
        submitText = "Create",
        onSubmit = {
            val owner = checkNotNull(inputs.selectedOwner)
            onCreated(
                Property(
                    address = Address(
                        street = inputs.street,
                        number = inputs.number,
                        unit = inputs.unit
                    ),
                    owner = owner
                )) },
        cancel = cancel,
        modifier = modifier
    ) {
        Item(
            field = "Street",
            value = inputs.street,
            onValueChange = { onInputsChanged(inputs.copy(street = it)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            ),
            modifier = Modifier.semantics { ContentType.AddressStreet }
        )
        Content {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Item(
                    field = "Number",
                    value = inputs.number,
                    onValueChange = { onInputsChanged(inputs.copy(number = it)) },
                    modifier = Modifier.weight(1f).semantics { ContentType.AddressAuxiliaryDetails }
                )
                Item(
                    field = "Unit",
                    value = inputs.unit,
                    mandatory = false,
                    onValueChange = { onInputsChanged(inputs.copy(unit = it)) },
                    modifier = Modifier.weight(1f).semantics { ContentType.AddressAuxiliaryDetails }
                )
            }
        }
        PickerItem(
            label = "Owner",
            items = owners,
            selectedItem = inputs.selectedOwner,
            pickerInputs = inputs.ownerPicker,
            onPickerInputsChanged = {
                onInputsChanged(inputs.copy(ownerPicker = it,
                    ownerDraft = if (it.isCreating) inputs.ownerDraft else OwnerInputs()))
            },
            itemLabel = { it.name },
            createContent = { onCreated, onCanceled ->
                OwnerCreator(
                    inputs = inputs.ownerDraft,
                    onInputsChanged = { onInputsChanged(inputs.copy(ownerDraft = it)) },
                    owners = owners,
                    onCreated = onCreated,
                    cancel = Cancel(text = "Cancel", onClick = onCanceled),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            onItemSelected = { onInputsChanged(inputs.copy(selectedOwner = it, ownerPicker = PickerInputs(), ownerDraft = OwnerInputs())) } ,
            onItemCreated = { onInputsChanged(inputs.copy(selectedOwner = it, ownerPicker = PickerInputs(), ownerDraft = OwnerInputs())) }
        )

    }
}
