package com.example.myapplication.ui.contract.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract

@Composable
fun ContractRentCard(
    contract: Contract,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Rent", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "Current: $${contract.currentValue().stripTrailingZeros().toPlainString()}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Initial: $${contract.rent.stripTrailingZeros().toPlainString()}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
