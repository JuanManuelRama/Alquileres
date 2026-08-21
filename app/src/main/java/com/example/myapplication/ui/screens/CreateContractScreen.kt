package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.creator.PropertyCreator
import com.example.myapplication.ui.viewModel.CreateContractViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.creator.ServiceCreator
import com.example.myapplication.ui.creator.TenantCreator
import java.time.LocalDate
import kotlin.let

@Composable
fun CreateContractScreen(
    viewModel: CreateContractViewModel,
    onBackClick: () -> Unit
    //onCreateContract: (ContractDraft) -> Unit,
) {
    val properties by viewModel.properties().collectAsState()
    val owners by viewModel.owners().collectAsState()
    val tenants by viewModel.tenants().collectAsState()
    val services by viewModel.services().collectAsState()

    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var initialRent by remember { mutableStateOf("") }
    var currentRent by remember { mutableStateOf("") }
    var selectedServices by remember { mutableStateOf<List<Service>>(emptyList()) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var adjustmentTime by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "New rental contract",
            style = MaterialTheme.typography.headlineSmall
        )

        Form(
            submitText = "Create Contract",
            onSubmit = {
                val contract = Contract(
                    property = selectedProperty!!, // cannot be null thanks to validation
                    tenant = selectedTenant!!, // cannot be null thanks to validation
                    initialRent = initialRent.toBigDecimal(), // will not fail due to validation
                    currentRent = currentRent.toBigDecimal(), // will not fail due to validation
                    startingDate = startDate!!, // cannot be null thanks to validation
                    finishingDate = endDate!!, // cannot be null thanks to validation
                    adjustmentTime = adjustmentTime.toLong(), // will not fail due to validation
                    services = selectedServices
                )
                viewModel.saveContract(contract)
                onBackClick()
            },
            cancelText = "Back",
            onCancel = onBackClick,
            modifier = Modifier
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
                    viewModel.addProperty(it)
                    selectedProperty = it
                }
            )
            PickerItem(
                label = "Tenant",
                items = tenants,
                selectedItem = selectedTenant,
                itemLabel = { it.name },
                createContent = { onCreated, onCanceled ->
                    TenantCreator(
                        onCreated = onCreated,
                        cancelText = "Cancel",
                        onCancel = onCanceled,
                    )
                },
                onItemSelected = { selectedTenant = it },
                onItemCreated = { 
                    viewModel.addTenant(it)
                    selectedTenant = it 
                }
            )
            Item(
                field = "Initial Rent",
                value = initialRent,
                keyboardType = KeyboardType.Decimal,
                validate = { it.toBigDecimalOrNull()?.let { it > 0.toBigDecimal() } == true },
                onValueChange = { rent ->
                    if (currentRent == "" || currentRent == initialRent) {
                        currentRent = rent
                    }
                    initialRent = rent
                }
            )
            Item(
                field = "Current Rent",
                value = currentRent,
                keyboardType = KeyboardType.Decimal,
                validate = { it.toBigDecimalOrNull()?.let { it > 0.toBigDecimal() } == true },
                onValueChange = { currentRent = it }
            )
            MultiPickerItem(
                label = "Services",
                items = services,
                selectedItems = selectedServices,
                itemLabel = { it.name },
                createContent = { onCreated, onCanceled ->
                    ServiceCreator(
                        onCreated = onCreated,
                        cancelText = "Cancel",
                        onCancel = onCanceled,
                    )
                },
                onItemsChanged = { selectedServices = it },
                onItemCreated = { viewModel.addService(it) }
            )

            DateItem(
                label = "Starting Date",
                value = startDate,
                onValueChange = { startDate = it }
            )
            DateItem(
                label = "Finishing Date",
                value = endDate,
                validate = { it?.isAfter(startDate) ?: false },
                onValueChange = { endDate = it }
            )
            Item(
                field = "Adjustment Time",
                value = adjustmentTime,
                keyboardType = KeyboardType.Number,
                validate = { it.toLongOrNull() != null },
                onValueChange = { adjustmentTime = it }
            )
        }
    }
}