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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    viewModel: BillViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onBack = { if (!state.isSaving) onBackClick() }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Bills") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                        Text("Retry")
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
                        isSaving = state.isSaving,
                        onSubmit = viewModel::save,
                        onMarkPaid = viewModel::markPaid
                    )
                }
            }
        }
    }
}

