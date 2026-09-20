package com.example.myapplication.ui.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R
import com.example.myapplication.model.Service
import com.example.myapplication.ui.components.form.Cancel
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.doesntExist
import com.example.myapplication.ui.components.form.maxLen
import com.example.myapplication.ui.components.form.noInvalidChars

@Composable
fun ServiceCreator(
    inputs: ServiceInputs,
    onInputsChanged: (ServiceInputs) -> Unit,
    services: List<Service>,
    onCreated: (Service) -> Unit,
    cancel: Cancel? = null,
    modifier: Modifier = Modifier,
) {
    val serviceNames = remember(services) { services.map { it.name }.toSet() }

    Form(
        submitText = stringResource(R.string.action_create),
        onSubmit = {
            onCreated(
                Service(
                    name = inputs.name
                )
            )
        },
        cancel = cancel,
        modifier = modifier,
    ) {
        Item(
            field = stringResource(R.string.label_service_name),
            value = inputs.name,
            validate = { Validate(it).noInvalidChars().maxLen(50).doesntExist(serviceNames) },
            onValueChange = { onInputsChanged(inputs.copy(name = it)) }
        )
    }
}
