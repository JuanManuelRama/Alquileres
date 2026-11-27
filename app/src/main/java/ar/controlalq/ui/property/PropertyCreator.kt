package ar.controlalq.ui.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.controlalq.R
import ar.controlalq.model.Address
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.ui.components.form.Cancel
import ar.controlalq.ui.components.form.Form
import ar.controlalq.ui.components.form.Validate
import ar.controlalq.ui.components.form.maxLen
import ar.controlalq.ui.components.form.noInvalidChars
import ar.controlalq.ui.components.picker.PickerInputs
import ar.controlalq.ui.owner.OwnerCreator
import ar.controlalq.ui.owner.OwnerInputs

@Composable
fun PropertyCreator(
    inputs: PropertyInputs,
    onInputsChanged: (PropertyInputs) -> Unit,
    owners: List<Owner>,
    onCreated: (Property) -> Unit,
    cancel: Cancel? = null,
    modifier: Modifier = Modifier,
    submitText: String = stringResource(R.string.action_create),
    enabled: Boolean = true,
) {

    val cancelText = stringResource(R.string.action_cancel)

    Form(
        submitText = submitText,
        enabled = enabled,
        onSubmit = {
            val owner = checkNotNull(inputs.selectedOwner)
            onCreated(
                Property(
                    address = Address(
                        street = inputs.street.trim(),
                        number = inputs.number.trim(),
                        unit = inputs.unit.trim().ifBlank { null }
                    ),
                    owner = owner
                )) },
        cancel = cancel,
        modifier = modifier
    ) {
        Item(
            field = stringResource(R.string.label_street),
            value = inputs.street,
            onValueChange = { onInputsChanged(inputs.copy(street = it)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            ),
            modifier = Modifier.semantics { ContentType.AddressStreet },
            validate = { Validate(it).noInvalidChars().maxLen(100) }
        )
        Content {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Item(
                    field = stringResource(R.string.label_number),
                    value = inputs.number,
                    onValueChange = { onInputsChanged(inputs.copy(number = it)) },
                    modifier = Modifier.weight(1f).semantics { ContentType.AddressAuxiliaryDetails },
                    validate = { Validate(it).noInvalidChars().maxLen(20) }
                )
                Item(
                    field = stringResource(R.string.label_unit),
                    value = inputs.unit,
                    mandatory = false,
                    onValueChange = { onInputsChanged(inputs.copy(unit = it)) },
                    modifier = Modifier.weight(1f).semantics { ContentType.AddressAuxiliaryDetails },
                    validate = { Validate(it).noInvalidChars().maxLen(20) }
                )
            }
        }
        PickerItem(
            label = stringResource(R.string.label_owner),
            createNewLabel = stringResource(R.string.picker_create_owner),
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
                    cancel = Cancel(text = cancelText, onClick = onCanceled),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            onItemSelected = { onInputsChanged(inputs.copy(selectedOwner = it, ownerPicker = PickerInputs(), ownerDraft = OwnerInputs())) } ,
            onItemCreated = { onInputsChanged(inputs.copy(selectedOwner = it, ownerPicker = PickerInputs(), ownerDraft = OwnerInputs())) }
        )

    }
}
