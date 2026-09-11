package com.example.myapplication.ui.owner

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.model.Owner
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.doesntExist

@Composable
fun OwnerCreator(
    owners: List<Owner>,
    onCreated: (Owner) -> Unit,
    cancel: Cancel? = null,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    val existingNames = remember(owners) { owners.map { it.name }.toSet() }

    Form(
        submitText = "Create",
        onSubmit = {
            onCreated(
                Owner(
                    name = name
                )
            )
        },
        cancel = cancel,
        modifier = modifier
    ) {
        Item(
            field = "Full Name",
            value = name,
            keyboardOptions = KeyboardOptions(
                keyboardType =  KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.semantics { ContentType.PersonFullName },
            onValueChange = { name = it },
            validate = { Validate(it).doesntExist(existingNames) }
        )
    }
}
