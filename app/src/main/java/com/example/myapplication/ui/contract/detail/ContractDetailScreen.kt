package com.example.myapplication.ui.contract.detail

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.model.Contract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    viewModel: ContractDetailViewModel,
    onPrepareBillClick: (Contract) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cardModifier = Modifier.fillMaxWidth()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val state = uiState as? ContractDetailUiState.Ready

    if (showFinishDialog && state != null) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finish Contract?") },
            text = {
                Text("Are you sure you want to finish this contract early? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishDialog = false
                    }
                ) {
                    Text(
                        text = "Finish Contract",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false } ) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showDeleteDialog && state != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contract?") },
            text = {
                Text("Are you sure you want to delete this contract? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "Delete Contract",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false } ) {
                    Text("Cancel")
                }
            }
        )
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
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cancel Contract") },
                                onClick = {
                                    showMenu = false
                                    showFinishDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "Delete Contract", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
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
                        val hasAffectedPaidBill = state.paidMonths.any { it >= adjustment.time }
                        AdjustmentCard(
                            adjustment = adjustment,
                            contract = contract,
                            modifier = cardModifier,
                            editable = !hasAffectedPaidBill,
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
                    onClick = { onPrepareBillClick(contract) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "View bills")
                }
            }
        }
    }
}
