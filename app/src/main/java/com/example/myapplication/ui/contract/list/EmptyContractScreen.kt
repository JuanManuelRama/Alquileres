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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun EmptyContracts(onCreateContract: () -> Unit, modifier: Modifier = Modifier) {
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
