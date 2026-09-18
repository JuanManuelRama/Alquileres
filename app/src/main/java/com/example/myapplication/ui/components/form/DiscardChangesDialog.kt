package com.example.myapplication.ui.components.form

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R

@Composable
fun DiscardChangesDialog(onDiscard: () -> Unit, onKeepEditing: () -> Unit) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = { Text(stringResource(R.string.discard_changes_title)) },
        text = { Text(stringResource(R.string.discard_changes_message)) },
        confirmButton = {
            TextButton(onClick = onDiscard) { Text(text = stringResource(R.string.action_discard), color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing) { Text(stringResource(R.string.action_keep_editing)) }
        }
    )
}
