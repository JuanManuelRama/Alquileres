package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContractDetailScreen(
    contract: Contract,
    onBackClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

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
            // Property & Tenant
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Property", style = MaterialTheme.typography.labelLarge)
                    Text(text = contract.property.toString(), style = MaterialTheme.typography.bodyLarge)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(text = "Tenant", style = MaterialTheme.typography.labelLarge)
                    Text(text = contract.tenant.name, style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Phone: ${contract.tenant.phone}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Rent Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Rent", style = MaterialTheme.typography.labelLarge)
                    Text(text = "Current: $${contract.currentRent}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Initial: $${contract.initialRent}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Adjustments every ${contract.adjustmentTime} months", style = MaterialTheme.typography.bodySmall)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contract.services.forEach { service ->
                        SuggestionChip(onClick = { }, label = { Text(service.name) })
                    }
                }
            }

            // Adjustment Schedule
            val adjustments = contract.adjustmentMonths()
            if (adjustments.isNotEmpty()) {
                Text(text = "Adjustment Schedule", style = MaterialTheme.typography.titleSmall)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        adjustments.forEach { date ->
                            Text(
                                text = "• ${date.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
