package com.example.myapplication.ui.contract.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractListScreen(
    state: ContractListUiState,
    onContractClick: (Contract) -> Unit,
    addContractClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contracts = (state as? ContractListUiState.Loaded)?.contracts
    var query by rememberSaveable { mutableStateOf("") }
    var ownerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var paymentDue by rememberSaveable { mutableStateOf(false) }
    var expiresSoon by rememberSaveable { mutableStateOf(false) }
    var hikeThisMonth by rememberSaveable { mutableStateOf(false) }
    val owners = remember(contracts) {
        contracts.orEmpty().map { it.contract.property.owner }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }
    val filteredContracts = remember(contracts, query, ownerId, paymentDue, expiresSoon, hikeThisMonth) {
        contracts.orEmpty().filtered(query, ownerId, paymentDue, expiresSoon, hikeThisMonth)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Contracts") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = addContractClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new contract"
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (contracts == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading contracts...", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (contracts.isEmpty()) {
                EmptyContracts(
                    onCreateContract = addContractClick,
                    modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item(key = "filters") {
                        ContractListFilters(
                            query = query, onQueryChange = { query = it },
                            owners = owners, ownerId = ownerId, onOwnerChange = { ownerId = it },
                            paymentDue = paymentDue, onPaymentDueChange = { paymentDue = it },
                            expiresSoon = expiresSoon, onExpiresSoonChange = { expiresSoon = it },
                            hikeThisMonth = hikeThisMonth, onHikeThisMonthChange = { hikeThisMonth = it },
                            onClear = {
                                query = ""
                                ownerId = null
                                paymentDue = false
                                expiresSoon = false
                                hikeThisMonth = false
                            },
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        )
                    }
                    if (filteredContracts.isEmpty()) {
                        item(key = "no_matches") {
                            Text("No contracts match these filters.", modifier = Modifier.padding(16.dp))
                        }
                    }
                    items(filteredContracts, key = { it.contract.id }) { item ->
                        ContractListCard(
                            item = item,
                            onClick = { onContractClick(item.contract) },
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
