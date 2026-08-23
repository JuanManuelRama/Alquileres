package com.example.myapplication.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Adjustment
import com.example.myapplication.model.Contract
import java.time.format.DateTimeFormatter

@Composable
fun AdjustmentCard(
    adjustment: Adjustment,
    contract: Contract
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    Card {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = adjustment.time.format(dateFormatter),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            if (adjustment.amount != null) {
                Text(
                    text = "${adjustment.amount * 100.toBigDecimal()}%",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "$${contract.valueAfterDate(adjustment.time)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}