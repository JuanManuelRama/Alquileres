package com.example.myapplication.ui.contract.detail

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    viewModel: ContractDetailViewModel,
    onPrepareBillClick: (Contract) -> Unit,
    onViewPaymentsButtonClick: (Contract) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cardModifier = Modifier.fillMaxWidth()

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
            val state = uiState as? ContractDetailUiState.Ready
            if (state == null) {
                Text(if (uiState == ContractDetailUiState.Loading) "Loading contract..." else "Contract not found.")
                return@Column
            }
            val contract = state.contract
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
                        val hasPaymentForMonth = YearMonth.from(adjustment.time) in state.paidMonths
                        AdjustmentCard(
                            adjustment = adjustment,
                            contract = contract,
                            modifier = cardModifier,
                            editable = !hasPaymentForMonth,
                            onAmountChange = { amount ->
                                viewModel.updateAdjustmentAmount(adjustment.time, amount)
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
                    onClick = { onPrepareBillClick(contract) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Prepare bill")
                }
            }
        }
    }
}
