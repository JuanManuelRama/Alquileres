package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Property

@Composable
fun DetallePropiedadScreen(
    property: Property,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Dirección: ${property.address}")
        Text(text = "Propietario: ${property.owner.name}")

        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Volver")
        }
    }
}