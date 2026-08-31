package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.ui.screens.ContractDetailScreen
import com.example.myapplication.ui.screens.ContractListScreen
import com.example.myapplication.ui.screens.ContractPaymentCreateScreen
import com.example.myapplication.ui.screens.ContractPaymentsScreen
import com.example.myapplication.ui.screens.CreateContractScreen
import com.example.myapplication.ui.screens.DetallePropiedadScreen
import com.example.myapplication.ui.screens.ListPropertiesScreen
import com.example.myapplication.ui.viewModel.ContractViewModel
import com.example.myapplication.ui.viewModel.CreateContractViewModel

@Composable
fun AppNavigation(
    createContractViewModel: CreateContractViewModel,
    contractViewModel: ContractViewModel,
    modifier: Modifier = Modifier
) {
    val backStack = remember { mutableStateListOf<Route>(ContractList) }
    val properties by createContractViewModel.properties().collectAsStateWithLifecycle()
    val contracts by contractViewModel.contracts.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key: Route ->
            when (key) {
                is ContractList -> NavEntry(key) {
                    ContractListScreen(
                        contracts = contracts,
                        onContractClick = { backStack.add(ContractDetail(it)) },
                        addContractClick = { backStack.add(AddPropiedad) }
                    )
                }
                is ListaPropiedades -> NavEntry(key) {
                    ListPropertiesScreen(
                        properties = properties,
                        onPropertyClick = { backStack.add(DetallePropiedad(it)) },
                        addPropertyClick = { backStack.add(AddPropiedad) }
                    )
                }
                is AddPropiedad -> NavEntry(key) {
                    CreateContractScreen(
                        viewModel = createContractViewModel,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is DetallePropiedad -> NavEntry(key) {
                    DetallePropiedadScreen(
                        property = key.property,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                is ContractDetail -> NavEntry(key) {
                    ContractDetailScreen(
                        contract = key.contract,
                        onCreatePaymentButtonClick = { backStack.add(CreatePaymentScreen(key.contract)) },
                        onViewPaymentsButtonClick = { backStack.add(ContractPayments(key.contract)) },
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