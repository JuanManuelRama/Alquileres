package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.check
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.isLong
import com.example.myapplication.ui.components.form.isPositive
import com.example.myapplication.ui.creator.ServiceCreator
import com.example.myapplication.ui.creator.TenantCreator
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContractScreen(
    viewModel: CreateContractViewModel,
    onBackClick: () -> Unit
) {
    val properties by viewModel.properties().collectAsState()
    val owners by viewModel.owners().collectAsState()
    val tenants by viewModel.tenants().collectAsState()
    val services by viewModel.services().collectAsState()

    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var rent by remember { mutableStateOf("") }
    var fixedHike by remember { mutableStateOf("") }
    var selectedServices by remember { mutableStateOf<List<Service>>(emptyList()) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var adjustmentTime by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Contract") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Form(
                submitText = "Create Contract",
                onSubmit = {
                    val contract = Contract(
                        property = selectedProperty!!, // cannot be null thanks to validation
                        tenant = selectedTenant!!, // cannot be null thanks to validation
                        rent = rent.toBigDecimal(), // will not fail due to validation
                        startingDate = startDate!!, // cannot be null thanks to validation
                        finishingDate = endDate!!, // cannot be null thanks to validation
                        adjustments = Contract.generateAdjustments(
                            startDate!!,
                            endDate!!,
                            adjustmentTime.toLong(),
                            fixedHike.toBigDecimalOrNull()?.divide(java.math.BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP)
                        ),
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
                        selectedTenant = it
                    }
                )
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
                            onCreated = onCreated,
                            cancelText = "Cancel",
                            onCancel = onCanceled,
                        )
                    },
                    onItemsChanged = { selectedServices = it },
                    onItemCreated = { }
                )

                DateItem(
                    label = "Starting Date",
                    value = startDate,
                    onValueChange = { startDate = it }
                )
                DateItem(
                    label = "Finishing Date",
                    value = endDate,
                    validate = {
                        Validate(it).check("must be after starting date") { date ->
                            date?.isAfter(
                                startDate
                            ) == true
                        }
                    },
                    onValueChange = { endDate = it }
                )
                Item(
                    field = "Adjustment Time",
                    value = adjustmentTime,
                    keyboardType = KeyboardType.Number,
                    validate = { Validate(it).isLong() },
                    onValueChange = { adjustmentTime = it }
                )
                Item(
                    field = "Fixed Hike (%)",
                    value = fixedHike,
                    mandatory = false,
                    keyboardType = KeyboardType.Decimal,
                    validate = { Validate(it).isBigDecimal() },
                    onValueChange = { fixedHike = it }
                )
            }
        }
    }
}
