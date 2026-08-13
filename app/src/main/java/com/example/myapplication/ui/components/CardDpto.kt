package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.Property

@Composable
fun CardDpto(
    property: Property,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.baseline_house_24),
                contentDescription = "photo of house",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
            Text(
                text = property.address.toString(),
                modifier = Modifier.padding(12.dp)
            )
            Text(
                text = property.owner.name,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}