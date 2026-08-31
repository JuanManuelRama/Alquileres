package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract
import com.example.myapplication.ui.cards.AdjustmentCard
import com.example.myapplication.ui.cards.PropertyCard
import com.example.myapplication.ui.cards.TenantCard
import com.example.myapplication.ui.viewModel.ContractViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContractDetailScreen(
    contract: Contract,
    onCreatePaymentButtonClick: (Contract) -> Unit,
    viewModel: ContractViewModel,
    onBackClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val payments by viewModel.payments.collectAsState(emptyList())

    LaunchedEffect(contract) {
        viewModel.selectContract(contract)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contract Details") },
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
            PropertyCard(property = contract.property)
            TenantCard(tenant = contract.tenant)

            // Rent Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Rent", style = MaterialTheme.typography.labelLarge)
                    Text(text = "Current: $${contract.currentValue()}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Initial: $${contract.rent}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Dates
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Duration", style = MaterialTheme.typography.labelLarge)
                    Text(text = "From: ${contract.startingDate.format(dateFormatter)}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "To: ${contract.finishingDate.format(dateFormatter)}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Services
            if (contract.services.isNotEmpty()) {
                Text(text = "Included Services", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contract.services.forEach { service ->
                        SuggestionChip(onClick = { }, label = { Text(service.name) })
                    }
                }
            }

            if (contract.adjustments.isNotEmpty()) {
                Text(text = "Adjustment Schedule", style = MaterialTheme.typography.titleSmall)
                Column {
                    contract.adjustments.forEach { adjustment ->
                        AdjustmentCard(
                            adjustment = adjustment,
                            contract = contract
                        )
                    }
                }
            }

            if (payments.isNotEmpty()) {
                Text(text = "Payments History", style = MaterialTheme.typography.titleSmall)
                payments.forEach { payment ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total: $${payment.total}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Rent: $${payment.rent}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (payment.services.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                payment.services.forEach { servicePayment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = servicePayment.service.name,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "$${servicePayment.amount}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { onCreatePaymentButtonClick(contract) }
            ) {
                Text(text = "Create Payment")
            }
        }
    }
}
