package com.example.myapplication.ui.creator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Payment
import com.example.myapplication.model.ServicePayment
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isPositive
import java.time.LocalDate

@Composable
fun ContractPaymentCreator(
    contract: Contract,
    onCreated: (Payment) -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    var price by remember { mutableStateOf(contract.currentValue().toString()) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    val serviceFees = remember(contract.services) {
        contract.services.map { "" }.toMutableStateList()
    }

    Form(
        submitText = "Create",
        onSubmit = { 
            onCreated(
                Payment(
                    contract = contract,
                    services = serviceFees.zip(contract.services) { fee, service ->
                        ServicePayment(service, fee.toBigDecimal())
                    },
                    rent = price.toBigDecimal(),
            ))
        },
        cancelText = cancelText,
        onCancel = onCancel,
        modifier = modifier
    ) {
        Item(
            field = "Ammount",
            value = price,
            keyboardType = KeyboardType.Decimal,
            validate = { (Validate(it).isBigDecimal().isPositive()) },
            onValueChange = { price = it }
        )
        DateItem(
            label = "Payment date",
            value = date,
            onValueChange = { date = it }
        )
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