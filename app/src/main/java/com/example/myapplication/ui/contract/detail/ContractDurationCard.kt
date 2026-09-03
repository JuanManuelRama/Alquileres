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
import java.time.format.DateTimeFormatter

private val contractDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun ContractDurationCard(
    contract: Contract,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Duration", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "From: ${contract.startingDate.format(contractDateFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "To: ${contract.finishingDate.format(contractDateFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
