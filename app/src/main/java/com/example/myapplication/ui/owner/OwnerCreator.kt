package com.example.myapplication.ui.owner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Owner
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.doesntExist

@Composable
fun OwnerCreator(
    owners: List<Owner>,
    onCreated: (Owner) -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
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
        cancelText = cancelText,
        onCancel = onCancel,
        modifier = modifier
    ) {
        Item(
            field = "Name",
            value = name,
            onValueChange = { name = it },
            validate = { Validate(it).doesntExist(existingNames) }
        )
    }
}
