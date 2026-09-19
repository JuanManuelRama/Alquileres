package com.example.myapplication.ui.contract.detail

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.model.Contract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    viewModel: ContractDetailViewModel,
    onPrepareBillClick: (Contract) -> Unit,
    onBackClick: () -> Unit,
    isInactive: Boolean,
    onFinished: () -> Unit,
    onRenewContractClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cardModifier = Modifier.fillMaxWidth()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var requireAllPaid by remember { mutableStateOf(false) }

    val state = uiState as? ContractDetailUiState.Ready

    if (showFinishDialog && state != null && !isInactive) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text(stringResource(if (requireAllPaid) R.string.contract_finish_title else R.string.contract_cancel_title)) },
            text = {
                Text(stringResource(R.string.contract_finish_message))
            },
            confirmButton = {
                TextButton(
                    enabled = !state.isFinishing,
                    onClick = {
                        viewModel.finishContract(requireAllPaid, onFinished)
                        showFinishDialog = false
                        onBackClick()
                    }
                ) {
                    Text(
                        text = stringResource(if (requireAllPaid) R.string.action_finish_contract else R.string.action_cancel_contract),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false } ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
    if (showDeleteDialog && state != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.contract_delete_title)) },
            text = {
                Text(stringResource(R.string.contract_delete_message))
            },
            confirmButton = {
                TextButton(
                    enabled = !state.isFinishing,
                    onClick = {
                        viewModel.deleteContract {
                            showDeleteDialog = false
                            onBackClick()
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete_contract),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }


    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_contract_details)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_cancel_contract)) },
                                enabled = state != null && !isInactive && !state.isFinishing,
                                onClick = {
                                    showMenu = false
                                    requireAllPaid = false
                                    showFinishDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.action_delete_contract), color = MaterialTheme.colorScheme.error) },
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
                Text(if (uiState == ContractDetailUiState.Loading) stringResource(R.string.loading_contract) else stringResource(R.string.err_contract_not_found))
                return@Column
            }
            val contract = state.contract
            if (isInactive) {
                Text(stringResource(R.string.contract_status_inactive), style = MaterialTheme.typography.titleMedium)
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (!isInactive && state.allBillsPaid) {
                Text(stringResource(R.string.contract_all_bills_paid_message))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !state.isFinishing,
                        onClick = {
                            requireAllPaid = true
                            showFinishDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (state.isFinishing) stringResource(R.string.action_finishing) else stringResource(R.string.action_finish_contract)) }
                    Button(
                        onClick = onRenewContractClick,
                        enabled = !state.isFinishing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.action_renew_contract))
                    }
                }
            }
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
                Text(text = stringResource(R.string.header_adjustment_schedule), style = MaterialTheme.typography.titleSmall)
                Column {
                    contract.adjustments.forEach { adjustment ->
                        val hasAffectedPaidBill = state.paidMonths.any { it >= adjustment.time }
                        AdjustmentCard(
                            adjustment = adjustment,
                            contract = contract,
                            modifier = cardModifier,
                            editable = !isInactive && !state.isFinishing && !hasAffectedPaidBill,
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
                    enabled = !state.isFinishing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.action_view_bills))
                }
            }
        }
    }
}
