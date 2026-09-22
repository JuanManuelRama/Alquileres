package com.example.myapplication.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
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
import com.example.myapplication.ui.contract.create.RenewContractScreen
import com.example.myapplication.ui.contract.detail.ContractDetailScreen
import com.example.myapplication.ui.contract.list.ContractListScreen
import com.example.myapplication.ui.settings.SettingsScreen
import com.example.myapplication.ui.settings.SettingsViewModel
import com.example.myapplication.ui.property.PropertyListScreen

@Composable
fun AppNavigation(
    contractViewModel: ContractViewModel,
    viewModelFactory: AppViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(ContractList(isInactive = false))
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { backStack.lastOrNull()?.let { backStack.popFrom(it) } },
        entryProvider = { key ->
            val back: () -> Unit = { backStack.popFrom(key) }
            when (val route = key as Route) {
                is ContractList -> NavEntry(route) {
                    val flow = if (route.isInactive) contractViewModel.inactiveListState
                              else contractViewModel.activeListState
                    val listState by flow.collectAsStateWithLifecycle()

                    ContractListScreen(
                        state = listState,
                        onContractClick = { backStack.pushFrom(key, ContractDetail(it.id, route.isInactive)) },
                        addContractClick = { backStack.pushFrom(key, AddPropiedad) },
                        onSettingsClick = { backStack.pushFrom(key, Settings) },
                        onPropertiesClick = {
                            val index = backStack.indexOf(route)
                            if (index >= 0) backStack[index] = PropertyList
                        },
                        onTabSelected = { inactiveState ->
                            if (inactiveState != route.isInactive) {
                                val index = backStack.indexOf(route)
                                if (index >= 0) backStack[index] = ContractList(inactiveState)
                            }
                        },
                    )
                }
                is PropertyList -> NavEntry(route) {
                    PropertyListScreen(
                        viewModel = viewModel(factory = viewModelFactory),
                        onSettingsClick = { backStack.pushFrom(key, Settings) },
                        onTabSelected = { tab ->
                            val index = backStack.indexOf(route)
                            if (index >= 0 && tab != RootTab.Properties) {
                                backStack[index] = ContractList(isInactive = tab == RootTab.Inactive)
                            }
                        },
                    )
                }
                is Settings -> NavEntry(route) {
                    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
                    SettingsScreen(
                        onBackClick = back,
                        settings = settings,
                        onThemeModeSelected = settingsViewModel::setThemeMode,
                        onLanguageSelected = settingsViewModel::setLanguage,
                        onMessageSenderSelected = settingsViewModel::setMessageSender,
                    )
                }
                is AddPropiedad -> NavEntry(route) {
                    CreateContractScreen(
                        viewModel = viewModel(factory = viewModelFactory),
                        onBackClick = back,
                        afterCreate = back,
                    )
                }
                is ContractDetail -> NavEntry(route) {
                    val detailFactory = remember(route.contractId, viewModelFactory) {
                        viewModelFactory.contractDetailFactory(route.contractId)
                    }
                    ContractDetailScreen(
                        viewModel = viewModel(factory = detailFactory),
                        onPrepareBillClick = { backStack.pushFrom(key, PrepareBill(it.id, route.isInactive)) },
                        isInactive = route.isInactive,
                        onFinished = {
                            val index = backStack.indexOf(route)
                            if (index >= 0) backStack[index] =
                                ContractDetail(route.contractId, isInactive = true)
                        },
                        onBackClick = back,
                        onRenewContractClick = {
                            backStack.pushFrom(key, RenewContract(route.contractId))
                        },
                    )
                }

                is RenewContract -> NavEntry(route) {
                    val detailFactory = remember(route.contractId, viewModelFactory) {
                        viewModelFactory.contractDetailFactory(route.contractId)
                    }
                    RenewContractScreen(
                        detailViewModel = viewModel(factory = detailFactory),
                        createViewModel = viewModel(factory = viewModelFactory),
                        onBackClick = back,
                        afterCreate = {
                            backStack.popFrom(key, count = 2)
                        },
                    )
                }

                is PrepareBill -> NavEntry(route) {
                    val factory = remember(route.contractId, viewModelFactory) {
                        viewModelFactory.billFactory(route.contractId)
                    }
                    BillScreen(
                        isInactive = route.isInactive,
                        viewModel = viewModel(factory = factory),
                        onBackClick = back,
                    )
                }
            }
        },
    )
}


internal fun <T> MutableList<T>.popFrom(expectedTop: T, count: Int = 1) {
    if (lastOrNull() != expectedTop) return
    repeat(count.coerceIn(0, (size - 1).coerceAtLeast(0))) {
        removeAt(lastIndex)
    }
}

internal fun <T> MutableList<T>.pushFrom(expectedTop: T, destination: T) {
    if (lastOrNull() != expectedTop) return
    add(destination)
}
