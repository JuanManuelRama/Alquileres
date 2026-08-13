package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.ui.screens.CreateContractScreen
import com.example.myapplication.ui.screens.DetallePropiedadScreen
import com.example.myapplication.ui.screens.ListPropertiesScreen
import com.example.myapplication.ui.viewModel.CreateContractViewModel

@Composable
fun AppNavigation(
    createContractViewModel: CreateContractViewModel,
    modifier: Modifier = Modifier
) {
    val backStack = remember { mutableStateListOf<Route>(ListaPropiedades) }
    val properties by createContractViewModel.properties().collectAsStateWithLifecycle()

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key: Route ->
            when (key) {
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
            }
        }
    )
}