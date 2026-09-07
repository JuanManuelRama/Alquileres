package com.example.myapplication.ui.contract.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.check
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isLong
import com.example.myapplication.ui.components.form.isPositive
import com.example.myapplication.ui.property.PropertyCreator
import com.example.myapplication.ui.service.ServiceCreator
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Composable
fun ContractCreator(
    properties: List<Property>,
    owners: List<Owner>,
    services: List<Service>,
    onCreated: (Contract) -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tenantName by remember { mutableStateOf("") }
    var tenantPhone by remember { mutableStateOf("") }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var rent by remember { mutableStateOf("") }
    var fixedHike by remember { mutableStateOf("") }
    var selectedServices by remember { mutableStateOf<List<Service>>(emptyList()) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var adjustmentTime by remember { mutableStateOf("") }

    Form(
        submitText = "Create Contract",
        onSubmit = {
            val property = checkNotNull(selectedProperty)
            val startingDate = checkNotNull(startDate)
            val finishingDate = checkNotNull(endDate)
            val contract = Contract(
                property = property,
                tenant = Tenant(name = tenantName, phone = tenantPhone),
                rent = rent.toBigDecimal(),
                startingDate = startingDate,
                finishingDate = finishingDate,
                adjustments = Contract.generateAdjustments(
                    startingDate,
                    finishingDate,
                    adjustmentTime.toLongOrNull(),
                    fixedHike.toBigDecimalOrNull()?.divide(BigDecimal("100"), 4, RoundingMode.HALF_UP)
                ),
                services = selectedServices
            )
            onCreated(contract)
        },
        cancelText = cancelText,
        onCancel = onCancel,
        modifier = modifier
    ) {
        PickerItem(
            label = "Property",
            items = properties,
            selectedItem = selectedProperty,
            itemLabel = { it.toString() },
            createContent = { onCreated, onCanceled ->
                PropertyCreator(
                    owners = owners,
                    onCreated = onCreated,
                    canceledText = "Cancel",
                    onCanceled = onCanceled,
                )
            },
            onItemSelected = { selectedProperty = it },
            onItemCreated = {
                selectedProperty = it
            }
        )
        Content {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tenant", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Item(
                        field = "Name",
                        value = tenantName,
                        modifier = Modifier.weight(1f),
                        onValueChange = { tenantName = it }
                    )
                    Item(
                        field = "Phone",
                        value = tenantPhone,
                        keyboardType = KeyboardType.Phone,
                        modifier = Modifier.weight(1f),
                        onValueChange = { tenantPhone = it }
                    )
                }
            }
        }
        Item(
            field = "Rent",
            value = rent,
            keyboardType = KeyboardType.Decimal,
            validate = { Validate(it).isBigDecimal().isPositive() },
            onValueChange = { rent = it }
        )
        MultiPickerItem(
            label = "Services",
            items = services,
            selectedItems = selectedServices,
            itemLabel = { it.name },
            createContent = { onCreated, onCanceled ->
                ServiceCreator(
                    services = services + selectedServices,
                    onCreated = onCreated,
                    cancelText = "Cancel",
                    onCancel = onCanceled,
                )
            },
            onItemsChanged = { selectedServices = it },
            onItemCreated = { }
        )

        Content {
            Text("Dates", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateItem(
                    label = "Starting Date",
                    value = startDate,
                    onValueChange = { startDate = it },
                    modifier = Modifier.weight(1f)
                )
                DateItem(
                    label = "Finishing Date",
                    value = endDate,
                    validate = {
                        Validate(it).check("must be after starting date") { date ->
                            startDate == null || date?.isAfter(startDate) == true
                        }
                    },
                    onValueChange = { endDate = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Content {
            Text("Adjustments", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Item(
                    field = "Adjustment Time",
                    value = adjustmentTime,
                    mandatory = false,
                    keyboardType = KeyboardType.Number,
                    validate = { Validate(it).isLong().isPositive() },
                    onValueChange = { adjustmentTime = it },
                    modifier = Modifier.weight(1f)
                )
                Item(
                    field = "Fixed Hike (%)",
                    value = fixedHike,
                    mandatory = false,
                    keyboardType = KeyboardType.Decimal,
                    validate = { Validate(it).isBigDecimal().check("can't have hikes without adjustment time") { adjustmentTime.isNotBlank() } },
                    onValueChange = { fixedHike = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
