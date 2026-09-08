package com.example.myapplication.ui.contract.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Contract
import java.time.YearMonth

data class ContractListItem(
    val contract: Contract,
    val paymentDue: Boolean,
    val expiresSoon: Boolean,
    val hikeThisMonth: Boolean
)

internal fun Contract.listItem(month: YearMonth, paymentDue: Boolean, hikeThisMonth: Boolean): ContractListItem {
    val expiryMonth = YearMonth.from(finishingDate)
    return ContractListItem(
        contract = this,
        paymentDue = paymentDue,
        expiresSoon = expiryMonth == month || expiryMonth == month.plusMonths(1),
        hikeThisMonth = hikeThisMonth
    )
}

@Composable
fun ContractListCard(
    item: ContractListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contract = item.contract
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        ListItem(
            headlineContent = { Text(contract.property.toString()) },
            supportingContent = { Text("Tenant: ${contract.tenant.name}") },
            trailingContent = { Text("Base: $${contract.rent.stripTrailingZeros().toPlainString()}") }
        )
        if (item.paymentDue || item.expiresSoon || item.hikeThisMonth) {
            FlowRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.paymentDue) ContractStatus(Icons.Default.Warning, "Payment due", MaterialTheme.colorScheme.error)
                if (item.expiresSoon) ContractStatus(Icons.Default.DateRange, "Expires soon", MaterialTheme.colorScheme.tertiary)
                if (item.hikeThisMonth) ContractStatus(Icons.Default.KeyboardArrowUp, "Hike this month", MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ContractStatus(icon: ImageVector, label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

