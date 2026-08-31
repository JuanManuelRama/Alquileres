package com.example.myapplication.ui.creator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Payment
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.isBigDecimal

@Composable
fun ContractPaymentCreator(
    contract: Contract,
    onCreated: (Payment) -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    val serviceFees = remember(contract.services) {
        contract.services.map { "" }.toMutableStateList()
    }

    Form(
        submitText = "Create",
        onSubmit = { onCreated },
        cancelText = cancelText,
        onCancel = onCancel,
        modifier = modifier
    ) {
        contract.services.forEachIndexed { index, service ->
            Item(
                field = service.name,
                value = serviceFees[index],
                keyboardType = KeyboardType.Decimal,
                validate = { Validate(it).isBigDecimal() },
                onValueChange = { serviceFees[index] = it }
            )
        }
    }
}