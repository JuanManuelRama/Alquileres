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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Contract
import com.example.myapplication.navigation.RootNavigationBar
import com.example.myapplication.navigation.RootTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractListScreen(
    state: ContractListUiState,
    onContractClick: (Contract) -> Unit,
    addContractClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTabSelected: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onPropertiesClick: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isInactive = state.isInactive
    val contracts = when (state) {
        is ContractListUiState.Loading -> null
        is ContractListUiState.Active -> state.contracts.map { it.contract }
        is ContractListUiState.Inactive -> state.contracts
    }
    val statuses = (state as? ContractListUiState.Active)?.contracts.orEmpty().associateBy { it.contract.id }
    val filters = rememberSaveable(isInactive, saver = ContractListFilterState.Saver) {
        ContractListFilterState()
    }
    val owners = remember(contracts) {
        contracts.orEmpty().map { it.property.owner }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }
    val filteredContracts = contracts.orEmpty().filter { filters.matches(it, statuses[it.id]) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isInactive) stringResource(R.string.title_inactive_contracts) else stringResource(R.string.title_active_contracts)) },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_options))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.title_settings)) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSettingsClick()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            RootNavigationBar(
                selected = if (isInactive) RootTab.Inactive else RootTab.Active,
                onSelected = { tab ->
                    when (tab) {
                        RootTab.Active -> onTabSelected(false)
                        RootTab.Inactive -> onTabSelected(true)
                        RootTab.Properties -> onPropertiesClick()
                    }
                },
            )
        },
        floatingActionButton = {
            if (!isInactive)
            FloatingActionButton(onClick = addContractClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_create_new_contract)
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
                    Text(stringResource(R.string.loading_contracts), style = MaterialTheme.typography.bodyMedium)
                }
            } else if (contracts.isEmpty()) {
                if (isInactive) {
                    Text(stringResource(R.string.empty_inactive_contracts), modifier = Modifier.align(Alignment.Center))
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
                            state = filters,
                            owners = owners,
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        )
                    }
                    if (filteredContracts.isEmpty()) {
                        item(key = "no_matches") {
                            Text(stringResource(R.string.no_matching_contracts), modifier = Modifier.padding(16.dp))
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
