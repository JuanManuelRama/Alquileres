package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Propiedad
import com.example.myapplication.ui.components.form.Form

@Composable
fun AddPropiedadScreen(
    onPropiedadCreada: (Propiedad) -> Unit,
    onBackClick: () -> Unit
) {
    var calle by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var piso by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Nueva Propiedad",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))


        Form(
            submitText = "Crear Propiedad",
            onSubmit = {
                //onPropiedadCreada(Propiedad(0, calle, numero, piso))
            },
        ) {
            Item(
                field = "Calle",
                value = calle,
                mandatory = true,
                onValueChange = { calle = it }
            )

            Item(
                field = "Número",
                value = numero,
                mandatory = true,
                keyboardType = KeyboardType.Number,
                validate = { it.toLongOrNull() != null },
                onValueChange = { numero = it }
            )

            Item(
                field = "Piso",
                value = piso,
                mandatory = false,
                onValueChange = { piso = it }
            )
        }
    }
}