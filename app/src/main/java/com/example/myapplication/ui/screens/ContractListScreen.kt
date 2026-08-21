package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract

@Composable
fun ContractListScreen(
    contracts: List<Contract>,
    onContractClick: (Contract) -> Unit,
    addContractClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(contracts) { contract ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    onClick = { onContractClick(contract) }
                ) {
                    ListItem(
                        headlineContent = { Text(contract.property.toString()) },
                        supportingContent = { Text("Tenant: ${contract.tenant.name}") },
                        trailingContent = { Text("$${contract.currentRent}") }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = addContractClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create new contract"
            )
        }
    }
}