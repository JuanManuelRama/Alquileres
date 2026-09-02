package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.myapplication.ui.cards.ContractDurationCard
import com.example.myapplication.ui.cards.ContractRentCard
import com.example.myapplication.ui.cards.ContractServicesCard
import com.example.myapplication.ui.cards.PropertyCard
import com.example.myapplication.ui.cards.TenantCard
import com.example.myapplication.ui.viewmodel.ContractViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    contract: Contract,
    viewModel: ContractViewModel,
    onCreatePaymentButtonClick: (Contract) -> Unit,
    onViewPaymentsButtonClick: (Contract) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val payments by viewModel.payments.collectAsState(emptyList())
    val cardModifier = Modifier.fillMaxWidth()

    LaunchedEffect(contract.id) {
        viewModel.selectContract(contract)
    }

    Scaffold(
        modifier = modifier,
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
            PropertyCard(
                property = contract.property,
                modifier = cardModifier
            )
            TenantCard(
                tenant = contract.tenant,
                modifier = cardModifier
            )
            ContractRentCard(
                contract = contract,
                modifier = cardModifier
            )
            ContractDurationCard(
                contract = contract,
                modifier = cardModifier
            )

            if (contract.services.isNotEmpty()) {
                ContractServicesCard(
                    services = contract.services,
                    modifier = cardModifier
                )
            }

            if (contract.adjustments.isNotEmpty()) {
                Text(text = "Adjustment Schedule", style = MaterialTheme.typography.titleSmall)
                Column {
                    contract.adjustments.forEach { adjustment ->
                        val hasPaymentForMonth = payments.any {
                            YearMonth.from(it.date) == YearMonth.from(adjustment.time)
                        }
                        AdjustmentCard(
                            adjustment = adjustment,
                            contract = contract,
                            modifier = cardModifier,
                            editable = !hasPaymentForMonth,
                            onAmountChange = { amount ->
                                viewModel.updateAdjustmentAmount(contract, adjustment.time, amount)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = cardModifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onViewPaymentsButtonClick(contract) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "View Payments")
                }
                Button(
                    onClick = { onCreatePaymentButtonClick(contract) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "New Payment")
                }
            }
        }
    }
}
