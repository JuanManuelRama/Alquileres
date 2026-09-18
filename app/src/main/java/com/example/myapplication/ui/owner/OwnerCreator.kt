package com.example.myapplication.ui.owner

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.R
import com.example.myapplication.model.Owner
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.doesntExist

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
    val uniqueError = stringResource(R.string.validation_must_be_unique)

    Form(
        submitText = stringResource(R.string.action_create),
        onSubmit = {
            onCreated(
                Owner(
                    name = inputs.name
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
            validate = { Validate(it).doesntExist(existingNames, uniqueError) }
        )
    }
}
