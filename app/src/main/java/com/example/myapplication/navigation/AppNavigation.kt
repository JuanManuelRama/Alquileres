package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.model.Contract
import com.example.myapplication.ui.AppViewModelFactory
import com.example.myapplication.ui.contract.ContractViewModel
import com.example.myapplication.ui.contract.bill.BillScreen
import com.example.myapplication.ui.contract.create.CreateContractScreen
import com.example.myapplication.ui.contract.create.CreateContractViewModel
import com.example.myapplication.ui.contract.detail.ContractDetailScreen
import com.example.myapplication.ui.contract.list.ContractListScreen
import com.example.myapplication.ui.contract.list.InactiveContractListScreen

@Composable
fun AppNavigation(
    createContractViewModel: CreateContractViewModel,
    contractViewModel: ContractViewModel,
    viewModelFactory: AppViewModelFactory,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(ContractList(isInactive = false))

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (val route = key as Route) {
                is ContractList -> NavEntry(route) {
                    val onContractClick: (Contract) -> Unit = {
                        backStack.add(ContractDetail(it.id, route.isInactive))
                    }
                    val onTabSelected: (Boolean) -> Unit = { isInactive ->
                        if (isInactive != route.isInactive) {
                            val index = backStack.indexOf(route)
                            if (index >= 0) backStack[index] = ContractList(isInactive)
                        }
                    }
                    if (route.isInactive) {
                        val contracts by contractViewModel.inactiveContracts.collectAsStateWithLifecycle()
                        InactiveContractListScreen(
                            contracts = contracts,
                            onContractClick = onContractClick,
                            onTabSelected = onTabSelected
                        )
                    } else {
                        val listState by contractViewModel.listState.collectAsStateWithLifecycle()
                        ContractListScreen(
                            state = listState,
                            onContractClick = onContractClick,
                            addContractClick = { backStack.add(AddPropiedad) },
                            onTabSelected = onTabSelected
                        )
                    }
                }
                is AddPropiedad -> NavEntry(route) {
                    CreateContractScreen(
                        viewModel = createContractViewModel,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is ContractDetail -> NavEntry(route) {
                    val detailFactory = remember(route.contractId, viewModelFactory) {
                        viewModelFactory.contractDetailFactory(route.contractId)
                    }
                    ContractDetailScreen(
                        viewModel = viewModel(factory = detailFactory),
                        onPrepareBillClick = { backStack.add(PrepareBill(it.id, route.isInactive)) },
                        isInactive = route.isInactive,
                        onFinished = {
                            val index = backStack.indexOf(route)
                            if (index >= 0) backStack[index] = ContractDetail(route.contractId, isInactive = true)
                        },
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is PrepareBill -> NavEntry(route) {
                    val factory = remember(route.contractId, viewModelFactory) {
                        viewModelFactory.billFactory(route.contractId)
                    }
                    BillScreen(
                        isInactive = route.isInactive,
                        viewModel = viewModel(factory = factory),
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    )
}
