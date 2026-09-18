package com.example.myapplication.ui.contract.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Contract
import com.example.myapplication.ui.util.toMoney

@Composable
fun ContractRentCard(
    contract: Contract,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.label_rent), style = MaterialTheme.typography.labelLarge)
            Text(
                text = stringResource(R.string.rent_current_format, contract.currentValue().toMoney()),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.rent_initial_format, contract.rent.toMoney()),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
