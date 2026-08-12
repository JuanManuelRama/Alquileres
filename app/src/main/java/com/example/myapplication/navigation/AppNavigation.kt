package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.model.Propiedad
import com.example.myapplication.ui.screens.CreateContractScreen
//import com.example.myapplication.ui.screens.AddPropiedadScreen
import com.example.myapplication.ui.screens.DetallePropiedadScreen
import com.example.myapplication.ui.screens.ListaPropiedadesScreen

@Composable
fun AppNavigation(
    dptos: List<Propiedad>,
    modifier: Modifier = Modifier
) {
    val backStack = remember { mutableStateListOf<Route>(ListaPropiedades) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key: Route ->
            when (key) {
                is ListaPropiedades -> NavEntry(key) {
                    ListaPropiedadesScreen(
                        dptos = dptos,
                        onPropiedadClick = { id ->
                            backStack.add(DetallePropiedad(id))
                        },
                        addPropiedadClick = { backStack.add(AddPropiedad) }
                    )
                }
                is AddPropiedad -> NavEntry(key) {
                    CreateContractScreen(
                        tenants = emptyList(),
                        properties = emptyList()
                    )
                }
                is DetallePropiedad -> NavEntry(key) {
                    val propiedad = dptos.first { it.id == key.id }
                    DetallePropiedadScreen(
                        propiedad = propiedad,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    )
}