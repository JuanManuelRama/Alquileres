package com.example.myapplication.ui.creator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.components.form.Form


@Composable
fun TenantCreator(
    onCreated: (Tenant) -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Form(
        submitText = "Create",
        onSubmit = {
            onCreated(
                Tenant(
                    name = name,
                    phone = phone
                )
            )
        },
        cancelText = cancelText,
        onCancel = onCancel,
        modifier = modifier,
    ) {
        Item(
            field = "Name",
            value = name,
            onValueChange = { name = it }
        )

        Item(
            field = "Phone",
            value = phone,
            keyboardType = KeyboardType.Phone,
            onValueChange = { phone = it }
        )
    }
}