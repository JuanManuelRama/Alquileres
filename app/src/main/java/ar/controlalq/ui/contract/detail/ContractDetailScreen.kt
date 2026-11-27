package ar.controlalq.ui.contract.detail

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.controlalq.R
import ar.controlalq.model.Contract
import ar.controlalq.ui.SaveStatus

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
    val write by viewModel.writeState.collectAsStateWithLifecycle()
    val busy = write.isBusy
    val onBack = { if (!busy) onBackClick() }
    BackHandler(onBack = onBack)
    LaunchedEffect(write) {
        if (write.status == SaveStatus.Saved && write.action != ContractWriteAction.Adjustment) {
            if (write.action != ContractWriteAction.Delete) onFinished()
            onBackClick()
        }
    }
    val writeError = if (write.status == SaveStatus.Failed) stringResource(when (write.action) {
        ContractWriteAction.Adjustment -> R.string.err_could_not_save_adjustment
        ContractWriteAction.Cancel -> R.string.err_could_not_cancel_contract
        ContractWriteAction.Delete -> R.string.err_could_not_delete_contract
        else -> R.string.err_could_not_finish_contract
    }) else null
    val cardModifier = Modifier.fillMaxWidth()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showFinishDialog by rememberSaveable { mutableStateOf(false) }
    var isFinishAction by rememberSaveable { mutableStateOf(false) }

    val state = uiState as? ContractDetailUiState.Ready

    if (showFinishDialog && state != null && !isInactive) {
        AlertDialog(
            onDismissRequest = { if (!busy) { showFinishDialog = false; viewModel.clearWriteResult() } },
            title = { Text(stringResource(if (isFinishAction) R.string.contract_finish_title else R.string.contract_cancel_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.contract_finish_message))
                    writeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        if (isFinishAction) viewModel.finishContract()
                        else viewModel.cancelContract()
                    }
                ) {
                    Text(
                        text = stringResource(if (busy) R.string.action_saving else if (isFinishAction) R.string.action_finish_contract else R.string.action_cancel_contract),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { showFinishDialog = false; viewModel.clearWriteResult() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
    if (showDeleteDialog && state != null) {
        AlertDialog(
            onDismissRequest = { if (!busy) { showDeleteDialog = false; viewModel.clearWriteResult() } },
            title = { Text(stringResource(R.string.contract_delete_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.contract_delete_message))
                    writeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = viewModel::deleteContract
                ) {
                    Text(
                        text = stringResource(if (busy) R.string.action_saving else R.string.action_delete_contract),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { showDeleteDialog = false; viewModel.clearWriteResult() }) {
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
                    IconButton(onClick = onBack, enabled = !busy) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }, enabled = !busy) {
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
                                enabled = state != null && !isInactive && !busy,
                                onClick = {
                                    showMenu = false
                                    isFinishAction = false
                                    showFinishDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.action_delete_contract), color = MaterialTheme.colorScheme.error) },
                                enabled = state != null && !busy,
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
            if (!showFinishDialog && !showDeleteDialog && write.action != ContractWriteAction.Adjustment) {
                writeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            if (!isInactive && state.allBillsPaid) {
                Text(stringResource(R.string.contract_all_bills_paid_message))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !busy,
                        onClick = {
                            isFinishAction = true
                            showFinishDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (busy) stringResource(R.string.action_finishing) else stringResource(R.string.action_finish_contract)) }
                    Button(
                        onClick = onRenewContractClick,
                        enabled = !busy,
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
                        AdjustmentCard(
                            adjustment = adjustment,
                            contract = contract,
                            modifier = cardModifier,
                            editable = !isInactive,
                            enabled = !busy,
                            saveStatus = if (write.action == ContractWriteAction.Adjustment && write.month == adjustment.time) write.status else SaveStatus.Idle,
                            onSaveHandled = viewModel::clearWriteResult,
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
                    enabled = !busy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.action_view_bills))
                }
            }
        }
    }
}
