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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Form(
    submitText: String,
    onSubmit: () -> Unit,
    cancel: Cancel? = null,
    modifier: Modifier = Modifier,
    showActions: Boolean = true,
    enabled: Boolean = true,
    builder: @Composable FormScope.() -> Unit
) {
    val scope = remember { FormScope() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        scope.builder()

        if (showActions) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (cancel != null) {
                    Button(
                        onClick = cancel.onClick,
                        enabled = enabled,
                    ) {
                        Text(text = cancel.text)
                    }

                }
                Button(
                    onClick = onSubmit,
                    enabled = enabled && scope.isValid,
                ) {
                    Text(text = submitText)
                }
            }
        }
    }
}
