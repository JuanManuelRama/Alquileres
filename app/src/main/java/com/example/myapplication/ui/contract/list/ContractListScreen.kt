package com.example.myapplication.ui.contract.list

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Contract

@Composable
fun ContractListScreen(
    state: ContractListUiState,
    onContractClick: (Contract) -> Unit,
    addContractClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contracts = (state as? ContractListUiState.Loaded)?.contracts
    Box(modifier = modifier.fillMaxSize()) {
        if (contracts == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()
                Text("Loading contracts...", style = MaterialTheme.typography.bodyMedium)
            }
        } else if (contracts.isEmpty()) {
            EmptyContracts(
                onCreateContract = addContractClick,
                modifier = Modifier.fillMaxSize().padding(bottom = 88.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(contracts, key = { it.contract.id }) { item ->
                    val contract = item.contract
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        onClick = { onContractClick(contract) }
                    ) {
                        ListItem(
                            headlineContent = { Text(contract.property.toString()) },
                            supportingContent = { Text("Tenant: ${contract.tenant.name}") },
                            trailingContent = { Text("$${contract.currentValue().stripTrailingZeros().toPlainString()}") }
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

@Composable
private fun EmptyContracts(onCreateContract: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "Floating contract")
    val floatOffset by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "Contract height"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .widthIn(max = 360.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .clickable(
                    role = Role.Button,
                    onClickLabel = "Create your first contract",
                    onClick = onCreateContract
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(208.dp)) {
                Box(
                    Modifier.size(192.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f), CircleShape)
                )
                Box(
                    Modifier.align(Alignment.BottomCenter)
                        .size(width = 88.dp, height = 10.dp)
                        .graphicsLayer {
                            scaleX = 1f + floatOffset * 0.025f
                            alpha = 0.10f + floatOffset * 0.005f
                        }
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_contract),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(144.dp).graphicsLayer {
                        translationY = floatOffset.dp.toPx()
                        rotationZ = -6f
                    }
                )
            }
            Text(
                "A fresh start, on paper",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                "No contracts yet. Add your first one and give your rental details a place to call home.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Create your first contract",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
