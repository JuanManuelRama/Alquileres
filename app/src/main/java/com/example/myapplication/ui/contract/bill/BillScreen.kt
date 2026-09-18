package com.example.myapplication.ui.contract.bill

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.message.BillMessageFormatter
import com.example.myapplication.message.MessageSender
import com.example.myapplication.ui.components.form.DiscardChangesDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    viewModel: BillViewModel,
    messageSender: MessageSender,
    onBackClick: () -> Unit,
    isInactive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var changedBillIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
    var confirmingDiscard by rememberSaveable { mutableStateOf(false) }
    val onBack = {
        if (!state.isSaving) {
            if (changedBillIds.isNotEmpty()) {
                confirmingDiscard = true
            } else onBackClick()
        }
    }

    BackHandler(onBack = onBack)

    if (confirmingDiscard) {
        DiscardChangesDialog(
            onDiscard = {
                if (!state.isSaving) {
                    confirmingDiscard = false
                    onBackClick()
                }
            },
            onKeepEditing = {
                confirmingDiscard = false
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bill_screen_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
                if (state.bill == null) {
                    TextButton(onClick = viewModel::retry) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }

            val bill = state.bill
            if (bill != null) {
                BillCards(
                    bills = state.bills,
                    selectedMonth = state.month,
                    enabled = !state.isSaving,
                    onSelect = viewModel::selectMonth
                ) {
                    BillCompletionForm(
                        bill = bill,
                        isInactive = isInactive,
                        isSaving = state.isSaving,
                        onSubmit = viewModel::save,
                        onMarkPaid = viewModel::markPaid,
                        onSendMessage = { draft ->
                            messageSender.sendMessage(
                                context,
                                draft.contract.tenant.phone,
                                BillMessageFormatter.format(draft)
                            )
                        },
                        onChangesChanged = { hasChanges ->
                            changedBillIds = if (hasChanges) {
                                (changedBillIds + bill.id).distinct()
                            } else changedBillIds - bill.id
                        }
                    )
                }
            }
        }
    }
}
