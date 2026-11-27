package ar.controlalq.ui.owner

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import ar.controlalq.R
import ar.controlalq.model.Owner
import ar.controlalq.ui.components.form.Cancel
import ar.controlalq.ui.components.form.Form
import ar.controlalq.ui.components.form.Validate
import ar.controlalq.ui.components.form.doesntExist
import ar.controlalq.ui.components.form.maxLen
import ar.controlalq.ui.components.form.noInvalidChars

@Composable
fun OwnerCreator(
    inputs: OwnerInputs,
    onInputsChanged: (OwnerInputs) -> Unit,
    owners: List<Owner>,
    onCreated: (Owner) -> Unit,
    cancel: Cancel? = null,
    modifier: Modifier = Modifier,
) {
    val existingNames = remember(owners) { owners.map { it.name }.toSet() }

    val fullNameLabel = stringResource(R.string.label_full_name)

    Form(
        submitText = stringResource(R.string.action_create),
        onSubmit = {
            onCreated(
                Owner(
                    name = inputs.name.trim()
                )
            )
        },
        cancel = cancel,
        modifier = modifier
    ) {
        Item(
            field = fullNameLabel,
            value = inputs.name,
            keyboardOptions = KeyboardOptions(
                keyboardType =  KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.semantics { ContentType.PersonFullName },
            onValueChange = { onInputsChanged(inputs.copy(name = it)) },
            validate = { Validate(it).noInvalidChars().maxLen(100).doesntExist(existingNames) }
        )
    }
}
