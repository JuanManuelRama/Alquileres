package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.ui.contract.ContractViewModel
import com.example.myapplication.ui.AppViewModelFactory
import com.example.myapplication.ui.contract.create.CreateContractScreen
import com.example.myapplication.ui.contract.create.CreateContractViewModel
import com.example.myapplication.ui.contract.detail.ContractDetailScreen
import com.example.myapplication.ui.contract.list.ContractListScreen
import com.example.myapplication.ui.contract.payment.ContractPaymentCreateScreen
import com.example.myapplication.ui.contract.payment.ContractPaymentsScreen

@Composable
fun AppNavigation(
    createContractViewModel: CreateContractViewModel,
    contractViewModel: ContractViewModel,
    viewModelFactory: AppViewModelFactory,
    modifier: Modifier = Modifier
) {
    val backStack = remember { mutableStateListOf<Route>(ContractList) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key: Route ->
            when (key) {
                is ContractList -> NavEntry(key) {
                    val contracts by contractViewModel.contracts.collectAsStateWithLifecycle()
                    ContractListScreen(
                        contracts = contracts,
                        onContractClick = { backStack.add(ContractDetail(it.id)) },
                        addContractClick = { backStack.add(AddPropiedad) }
                    )
                }
                is AddPropiedad -> NavEntry(key) {
                    CreateContractScreen(
                        viewModel = createContractViewModel,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is ContractDetail -> NavEntry(key) {
                    val detailFactory = remember(key.contractId, viewModelFactory) {
                        viewModelFactory.contractDetailFactory(key.contractId)
                    }
                    ContractDetailScreen(
                        viewModel = viewModel(factory = detailFactory),
                        onCreatePaymentButtonClick = { backStack.add(CreatePaymentScreen(it)) },
                        onViewPaymentsButtonClick = { backStack.add(ContractPayments(it)) },
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is ContractPayments -> NavEntry(key) {
                    ContractPaymentsScreen(
                        contract = key.contract,
                        viewModel = contractViewModel,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is CreatePaymentScreen -> NavEntry(key) {
                    ContractPaymentCreateScreen(
                        contract = key.contract,
                        viewModel = contractViewModel,
                        onBackClick = { backStack.removeLastOrNull() },
                        modifier = modifier
                    )
                }
            }
        }
    )
}
