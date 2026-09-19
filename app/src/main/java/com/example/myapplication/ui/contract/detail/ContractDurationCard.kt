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
import com.example.myapplication.ui.util.rememberDateFormatters


@Composable
fun ContractDurationCard(
    contract: Contract,
    modifier: Modifier = Modifier
) {
    val dates = rememberDateFormatters()
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.label_duration), style = MaterialTheme.typography.labelLarge)
            Text(
                text = stringResource(R.string.duration_from_format, dates.month(contract.startingMonth)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.duration_to_format, dates.month(contract.finishingMonth)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
