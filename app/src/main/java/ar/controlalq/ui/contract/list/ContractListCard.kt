package ar.controlalq.ui.contract.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.controlalq.R
import ar.controlalq.model.Contract
import ar.controlalq.ui.util.rememberDateFormatters
import java.time.YearMonth

data class ContractListItem(
    val contract: Contract,
    val paymentDue: Boolean,
    val expiresSoon: Boolean,
    val hikeThisMonth: Boolean,
    val billIncomplete: Boolean = false
)

internal fun Contract.listItem(month: YearMonth, paymentDue: Boolean, hikeThisMonth: Boolean, billIncomplete: Boolean = false): ContractListItem {
    return ContractListItem(
        contract = this,
        paymentDue = paymentDue && !billIncomplete,
        expiresSoon = finishingMonth == month || finishingMonth == month.plusMonths(1),
        hikeThisMonth = hikeThisMonth,
        billIncomplete = billIncomplete
    )
}

@Composable
fun ContractListCard(
    contract: Contract,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    item: ContractListItem? = null
) {
    val dates = rememberDateFormatters()
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = contract.property.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.tenant_name_format, contract.tenant.name),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.duration_to_format, dates.month(contract.finishingMonth)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (item != null && (item.billIncomplete || item.paymentDue || item.expiresSoon || item.hikeThisMonth)) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (item.billIncomplete) {
                            ContractStatusBadge(
                                icon = Icons.Default.Edit,
                                label = stringResource(R.string.status_bill_incomplete),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else if (item.paymentDue) {
                            ContractStatusBadge(
                                icon = Icons.Default.Warning,
                                label = stringResource(R.string.status_payment_due),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        } else {
                            Spacer(Modifier.height(22.dp))
                        }
                    }
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (item.expiresSoon) {
                            ContractStatusBadge(
                                icon = Icons.Default.HourglassEmpty,
                                label = stringResource(R.string.status_expires_soon),
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        } else {
                            Spacer(Modifier.height(22.dp))
                        }
                    }
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (item.hikeThisMonth) {
                            ContractStatusBadge(
                                icon = Icons.Default.KeyboardArrowUp,
                                label = stringResource(R.string.status_hike_this_month),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Spacer(Modifier.height(22.dp))
                        }
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
