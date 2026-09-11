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
import com.example.myapplication.ui.AppViewModelFactory
import com.example.myapplication.ui.contract.ContractViewModel
import com.example.myapplication.ui.contract.bill.BillScreen
import com.example.myapplication.ui.contract.create.CreateContractScreen
import com.example.myapplication.ui.contract.create.CreateContractViewModel
import com.example.myapplication.ui.contract.detail.ContractDetailScreen
import com.example.myapplication.ui.contract.list.ContractListScreen

@Composable
fun AppNavigation(
    createContractViewModel: CreateContractViewModel,
    contractViewModel: ContractViewModel,
    viewModelFactory: AppViewModelFactory,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(ContractList)

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
                    val listState by contractViewModel.listState.collectAsStateWithLifecycle()
                    ContractListScreen(
                        state = listState,
                        onContractClick = { backStack.add(ContractDetail(it.id)) },
                        addContractClick = { backStack.add(AddPropiedad) }
                    )
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
                        onPrepareBillClick = { backStack.add(PrepareBill(it.id)) },
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is PrepareBill -> NavEntry(route) {
                    val factory = remember(route.contractId, viewModelFactory) {
                        viewModelFactory.billFactory(route.contractId)
                    }
                    BillScreen(
                        viewModel = viewModel(factory = factory),
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    )
}
