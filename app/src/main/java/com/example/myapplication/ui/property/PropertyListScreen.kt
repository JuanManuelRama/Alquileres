package com.example.myapplication.ui.property

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.navigation.RootNavigationBar
import com.example.myapplication.navigation.RootTab
import com.example.myapplication.ui.SaveStatus
import com.example.myapplication.ui.components.form.DiscardChangesDialog
import kotlinx.serialization.Serializable

@Serializable
private data class PropertySelection(val property: Property? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListScreen(
    viewModel: PropertyListViewModel,
    onTabSelected: (RootTab) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val status by viewModel.saveStatus.collectAsStateWithLifecycle()
    val busy = status == SaveStatus.Saving || status == SaveStatus.Saved
    var selection by rememberSerializable { mutableStateOf(PropertySelection()) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val ready = state as? PropertyListState.Ready
    LaunchedEffect(status) {
        if (status == SaveStatus.Saved) {
            selection = PropertySelection()
            deletingId = null
            viewModel.resetStatus()
        }
    }
    val editedProperty = selection.property
    if (editedProperty != null) {
        PropertyEditScreen(
            property = editedProperty, owners = ready?.owners.orEmpty(), status = status,
            onSave = viewModel::save,
            onBack = { selection = PropertySelection(); viewModel.resetStatus() },
        )
        return
    }
    val deleting = ready?.rows?.find { it.property.id == deletingId }
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { if (!busy) { deletingId = null; viewModel.resetStatus() } },
            title = { Text(stringResource(R.string.delete_property_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.delete_property_message, deleting.property.toString()))
                    if (status == SaveStatus.Failed) PropertyMutationError()
                }
            },
            confirmButton = {
                TextButton(enabled = !busy, onClick = { viewModel.delete(deleting.property.id) }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { deletingId = null; viewModel.resetStatus() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.title_properties)) }, actions = {
                IconButton(onClick = onSettingsClick, enabled = !busy) {
                    Icon(Icons.Default.Settings, stringResource(R.string.title_settings))
                }
            })
        },
        bottomBar = { RootNavigationBar(RootTab.Properties) { if (!busy) onTabSelected(it) } },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (status == SaveStatus.Failed && deleting == null) PropertyMutationError()
            when (val current = state) {
                PropertyListState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                PropertyListState.Failed -> Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.err_loading_properties))
                    TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.action_retry)) }
                }
                is PropertyListState.Ready -> {
                    if (current.rows.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.empty_properties), Modifier.padding(24.dp))
                    } else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(current.rows, key = { it.property.id }) { row ->
                            PropertyRow(row, busy,
                                onEdit = { viewModel.resetStatus(); selection = PropertySelection(row.property) },
                                onDelete = { viewModel.resetStatus(); deletingId = row.property.id },
                                onSetActive = { viewModel.setActive(row.property.id, !row.isActive) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyRow(row: PropertyListItem, busy: Boolean, onEdit: () -> Unit, onDelete: () -> Unit, onSetActive: () -> Unit) {
    val property = row.property
    var expanded by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(property.toString(), style = MaterialTheme.typography.titleMedium)
                Text(property.owner.name, style = MaterialTheme.typography.bodyMedium)
                Surface(shape = MaterialTheme.shapes.small, color = if (row.isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                    Text(stringResource(if (row.isActive) R.string.property_active else R.string.property_inactive), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
            Box {
                IconButton(onClick = { expanded = true }, enabled = !busy) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.property_actions, property.toString()))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_edit)) }, onClick = { expanded = false; onEdit() })
                    DropdownMenuItem(
                        text = { Text(stringResource(if (row.isActive) R.string.action_deactivate_property else R.string.action_reactivate_property)) },
                        enabled = !row.isActive || !row.hasActiveContracts,
                        onClick = { expanded = false; onSetActive() },
                    )
                    if (row.isActive && row.hasActiveContracts) {
                        Text(stringResource(R.string.property_has_active_contracts), Modifier.widthIn(max = 280.dp).padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall)
                    }
                    DropdownMenuItem(text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }, onClick = { expanded = false; onDelete() })
                }
            }
        }
    }
}

@Composable
private fun PropertyMutationError() {
    Text(stringResource(R.string.err_property_change), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyEditScreen(property: Property, owners: List<Owner>, status: SaveStatus, onSave: (Property) -> Unit, onBack: () -> Unit) {
    val initial by rememberSerializable(property.id) {
        mutableStateOf(PropertyInputs(property.address.street, property.address.number, property.address.unit.orEmpty(), property.owner))
    }
    var inputs by rememberSerializable(property.id) { mutableStateOf(initial) }
    var discard by rememberSaveable { mutableStateOf(false) }
    val busy = status == SaveStatus.Saving || status == SaveStatus.Saved
    val back = { if (!busy) { if (inputs != initial) discard = true else onBack() } }
    BackHandler(onBack = back)
    if (discard) DiscardChangesDialog(onDiscard = onBack, onKeepEditing = { discard = false })
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.edit_property_title)) }, navigationIcon = {
            IconButton(onClick = back, enabled = !busy) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back)) }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.edit_property_hint), style = MaterialTheme.typography.bodyMedium)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (status == SaveStatus.Failed) PropertyMutationError()
            PropertyCreator(
                inputs = inputs, onInputsChanged = { if (!busy) inputs = it }, owners = owners,
                onCreated = { onSave(property.copy(address = it.address, owner = it.owner)) },
                submitText = stringResource(R.string.action_save), enabled = !busy,
            )
        }
    }
}
