package com.example.myapplication.ui.components.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
internal fun MonthFormItemContent(item: MonthFormItemState, error: Error, modifier: Modifier) {
    var open by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = item.value?.format(DateTimeFormatter.ofPattern("MMM yyyy")) ?: "",
        onValueChange = {},
        label = { Text(item.displayLabel) },
        supportingText = { Text(error.message) },
        isError = error.hasMessage,
        readOnly = true,
        enabled = false,
        modifier = modifier.clickable(role = Role.Button) { open = true },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledBorderColor = if (error.hasMessage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            disabledSupportingTextColor = if (error.hasMessage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
    if (open) {
        var yearText by remember { mutableStateOf((item.value ?: YearMonth.now()).year.toString()) }
        val year = yearText.toIntOrNull()?.takeIf { it in 1..9999 }
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(item.label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(enabled = year != null && year > 1, onClick = { yearText = (year!! - 1).toString() }, modifier = Modifier.semantics { contentDescription = "Previous year" }) { Text("<") }
                        OutlinedTextField(value = yearText, onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) yearText = it }, label = { Text("Year") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, isError = year == null, modifier = Modifier.weight(1f))
                        TextButton(enabled = year != null && year < 9999, onClick = { yearText = (year!! + 1).toString() }, modifier = Modifier.semantics { contentDescription = "Next year" }) { Text(">") }
                    }
                    Month.entries.chunked(3).forEach { months ->
                        Row(Modifier.fillMaxWidth()) {
                            months.forEach { month ->
                                TextButton(
                                    enabled = year != null,
                                    onClick = { item.onValueChange(YearMonth.of(year!!, month)); open = false },
                                    modifier = Modifier.weight(1f).semantics { selected = item.value?.let { it.year == year && it.month == month } == true }
                                ) { Text(month.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale)) }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } }
        )
    }
}
