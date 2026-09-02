package com.example.myapplication.ui.components.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
    cancelText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    builder: FormScope.() -> Unit
) {
    val scope = FormScope().apply(builder)
    val fields = scope.elements.filterIsInstance<FormElement.Field>()
    val errors = fields.map { it.state.error() }
    val isFormValid = errors.none { it.isError }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        scope.elements.forEach { element ->
            when (element) {
                is FormElement.Field -> FormItem(
                    item = element.state,
                    modifier = Modifier.fillMaxWidth(),
                    error = element.state.error()
                )
                is FormElement.Content -> element.content()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onCancel,
            ) {
                Text(text = cancelText)
            }
            Button(
                onClick = onSubmit,
                enabled = isFormValid,
            ) {
                Text(text = submitText)
            }
        }
    }
}
