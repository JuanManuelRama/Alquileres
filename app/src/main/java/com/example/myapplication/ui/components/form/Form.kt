package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Form(
    submitText: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    builder: FormScope.() -> Unit
) {
    val scope = FormScope().apply(builder)
    val items = scope.items
    val isFormValid = items.all { it.isValidated() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { FormItem(item = it) }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSubmit,
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = submitText)
        }
    }
}
