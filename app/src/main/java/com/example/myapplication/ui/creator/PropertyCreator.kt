package com.example.myapplication.ui.creator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.ui.components.form.Form

@Composable
fun PropertyCreator(
    owners: List<Owner>,
    onCreated: (Property) -> Unit,
    canceledText: String,
    onCanceled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var street by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var selectedOwner by remember { mutableStateOf<Owner?>(null) }

    Form(
        submitText = "Create",
        onSubmit = {
            onCreated(
                Property(
                    address = Address(
                        street = street,
                        number = number,
                        unit = unit
                    ),
                    owner = selectedOwner!! // Cannot be null because the button won't work
                )) },
        cancelText = canceledText,
        onCancel = onCanceled,
        modifier = modifier
    ) {
        Item(
            field = "Street",
            value = street,
            onValueChange = { street = it }
        )
        Item(
            field = "Number",
            value = number,
            onValueChange = { number = it }
        )
        Item(
            field = "Unit",
            value = unit,
            mandatory = false,
            onValueChange = { unit = it }
        )
        PickerItem(
            label = "Owner",
            items = owners,
            selectedItem = selectedOwner,
            itemLabel = { it.name },
            createContent = { onCreated, onCanceled ->
                OwnerCreator(
                    onCreated = onCreated,
                    cancelText = "Cancel",
                    onCancel = onCanceled,
                    modifier = modifier
                )
            },
            onItemSelected = { selectedOwner = it } ,
            onItemCreated = { selectedOwner = it }
        )

    }
}