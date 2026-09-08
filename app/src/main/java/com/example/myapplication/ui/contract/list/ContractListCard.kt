package com.example.myapplication.ui.contract.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
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
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
                headlineContent = {
                    Text(
                        text = contract.property.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                supportingContent = {
                    Text(
                        text = "Tenant: ${contract.tenant.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                trailingContent = {
                    Text(
                        text = "Base: $${contract.rent.stripTrailingZeros().toPlainString()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
            if (item.paymentDue || item.expiresSoon || item.hikeThisMonth) {
                FlowRow(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.paymentDue) {
                        ContractStatusBadge(
                            icon = Icons.Default.Warning,
                            label = "Payment due",
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    if (item.expiresSoon) {
                        ContractStatusBadge(
                            icon = Icons.Default.DateRange,
                            label = "Expires soon",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    if (item.hikeThisMonth) {
                        ContractStatusBadge(
                            icon = Icons.Default.KeyboardArrowUp,
                            label = "Hike this month",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractStatusBadge(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
