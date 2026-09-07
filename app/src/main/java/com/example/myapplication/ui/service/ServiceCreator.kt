package com.example.myapplication.ui.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Service
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.doesntExist

@Composable
fun ServiceCreator(
    services: List<Service>,
    onCreated: (Service) -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var serviceNames by remember(services) { mutableStateOf(services.map { it.name }.toSet()) }
    var name by remember { mutableStateOf("") }

    Form(
        submitText = "Create",
        onSubmit = {
            onCreated(
                Service(
                    name = name
                )
            )
        },
        cancelText = cancelText,
        onCancel = onCancel,
        modifier = modifier,
    ) {
        Item(
            field = "Service Name",
            value = name,
            validate = { Validate(it).doesntExist(serviceNames) },
            onValueChange = { name = it }
        )
    }
}
