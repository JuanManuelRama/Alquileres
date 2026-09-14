package com.example.myapplication.ui.contract.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Contract
import java.time.YearMonth

@Composable
fun ContractListScreen(
    state: ContractListUiState,
    onContractClick: (Contract) -> Unit,
    addContractClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTabSelected: (Boolean) -> Unit = {},
) {
    val items = (state as? ContractListUiState.Loaded)?.contracts
    ContractListContent(
        contracts = items?.map { it.contract },
        statuses = items.orEmpty().associateBy { it.contract.id },
        isInactive = false,
        onContractClick = onContractClick,
        addContractClick = addContractClick,
        onTabSelected = onTabSelected,
        modifier = modifier
    )
}

@Composable
fun InactiveContractListScreen(
    contracts: List<Contract>?,
    onContractClick: (Contract) -> Unit,
    onTabSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ContractListContent(
        contracts = contracts,
        statuses = emptyMap(),
        isInactive = true,
        onContractClick = onContractClick,
        addContractClick = {},
        onTabSelected = onTabSelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContractListContent(
    contracts: List<Contract>?,
    statuses: Map<Long, ContractListItem>,
    isInactive: Boolean,
    onContractClick: (Contract) -> Unit,
    addContractClick: () -> Unit,
    onTabSelected: (Boolean) -> Unit,
    modifier: Modifier,
) {
    var query by rememberSaveable(isInactive) { mutableStateOf("") }
    var ownerId by rememberSaveable(isInactive) { mutableStateOf<Long?>(null) }
    var startingMonth by rememberSaveable(isInactive) { mutableStateOf<YearMonth?>(null) }
    var finishingMonth by rememberSaveable(isInactive) { mutableStateOf<YearMonth?>(null) }
    var paymentDue by rememberSaveable(isInactive) { mutableStateOf(false) }
    var expiresSoon by rememberSaveable(isInactive) { mutableStateOf(false) }
    var hikeThisMonth by rememberSaveable(isInactive) { mutableStateOf(false) }
    val owners = remember(contracts) {
        contracts.orEmpty().map { it.property.owner }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }
    val filteredContracts = remember(contracts, statuses, query, ownerId, startingMonth, finishingMonth, paymentDue, expiresSoon, hikeThisMonth) {
        contracts.orEmpty().filter { contract ->
            val status = statuses[contract.id]
            contract.property.toString().contains(query.trim(), ignoreCase = true) &&
                (ownerId == null || contract.property.owner.id == ownerId) &&
                (startingMonth == null || contract.finishingMonth >= startingMonth) &&
                (finishingMonth == null || contract.startingMonth <= finishingMonth) &&
                (!paymentDue || status?.paymentDue == true) &&
                (!expiresSoon || status?.expiresSoon == true) &&
                (!hikeThisMonth || status?.hikeThisMonth == true)
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(if (isInactive) "Inactive contracts" else "Active contracts") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = !isInactive,
                    onClick = { onTabSelected(false) },
                    icon = { Icon(painterResource(R.drawable.ic_contract), contentDescription = null, modifier = Modifier.size(24.dp)) },
                    label = { Text("Active") }
                )
                NavigationBarItem(
                    selected = isInactive,
                    onClick = { onTabSelected(true) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Inactive") }
                )
            }
        },
        floatingActionButton = {
            if (!isInactive)
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
                if (isInactive) {
                    Text("No inactive contracts yet.", modifier = Modifier.align(Alignment.Center))
                } else EmptyContracts(
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
                            showStatusFilters = !isInactive,
                            showDateFilters = isInactive,
                            query = query, onQueryChange = { query = it },
                            owners = owners, ownerId = ownerId, onOwnerChange = { ownerId = it },
                            startingMonth = startingMonth, onStartingMonthChange = { startingMonth = it },
                            finishingMonth = finishingMonth, onFinishingMonthChange = { finishingMonth = it },
                            paymentDue = paymentDue, onPaymentDueChange = { paymentDue = it },
                            expiresSoon = expiresSoon, onExpiresSoonChange = { expiresSoon = it },
                            hikeThisMonth = hikeThisMonth, onHikeThisMonthChange = { hikeThisMonth = it },
                            onClear = {
                                query = ""
                                ownerId = null
                                startingMonth = null
                                finishingMonth = null
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
                    items(filteredContracts, key = { it.id }) { contract ->
                        ContractListCard(
                            contract = contract,
                            item = statuses[contract.id],
                            onClick = { onContractClick(contract) },
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
