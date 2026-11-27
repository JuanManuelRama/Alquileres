package ar.controlalq.ui.property

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.controlalq.R
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.navigation.RootNavigationBar
import ar.controlalq.navigation.RootTab
import ar.controlalq.ui.SaveStatus
import ar.controlalq.ui.components.form.DiscardChangesDialog
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
    var filters by rememberSerializable { mutableStateOf(PropertyListFilterState()) }
    val owners = remember(ready?.rows) {
        ready?.rows.orEmpty().map { it.property.owner }.distinctBy { it.id }.sortedBy { it.name.lowercase() }
    }
    val filteredRows = ready?.rows.orEmpty().filter(filters::matches)
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
                        item(key = "filters") {
                            PropertyListFilters(filters, { filters = it }, owners, Modifier.fillMaxWidth())
                        }
                        if (filteredRows.isEmpty()) {
                            item(key = "no_matches") {
                                Text(stringResource(R.string.no_matching_properties), Modifier.padding(vertical = 16.dp))
                            }
                        }
                        items(filteredRows, key = { it.property.id }) { row ->
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
    val canToggleActive = !busy && (!row.isActive || !row.hasActiveContracts)
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(property.toString(), style = MaterialTheme.typography.titleMedium)
                Text(property.owner.name, style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = row.isActive,
                    onClick = onSetActive,
                    enabled = canToggleActive,
                    label = {
                        Text(
                            text = stringResource(if (row.isActive) R.string.property_active else R.string.property_inactive),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = canToggleActive,
                        selected = row.isActive,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                IconButton(onClick = onEdit, enabled = !busy) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                    )
                }
                IconButton(onClick = onDelete, enabled = !busy) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
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
