package ar.controlalq.ui.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.controlalq.R
import ar.controlalq.model.Owner
import ar.controlalq.ui.components.filter.ListSearchField
import ar.controlalq.ui.components.filter.OwnerFilterChip
import kotlinx.serialization.Serializable

@Serializable
internal data class PropertyListFilterState(
    val query: String = "",
    val ownerId: Long? = null,
    val isActive: Boolean? = null,
    val hasActiveContracts: Boolean? = null,
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || ownerId != null || isActive != null || hasActiveContracts != null

    fun matches(item: PropertyListItem): Boolean =
        item.property.toString().contains(query.trim(), ignoreCase = true) &&
            (ownerId == null || item.property.owner.id == ownerId) &&
            (isActive == null || item.isActive == isActive) &&
            (hasActiveContracts == null || item.hasActiveContracts == hasActiveContracts)
}

@Composable
internal fun PropertyListFilters(
    state: PropertyListFilterState,
    onChanged: (PropertyListFilterState) -> Unit,
    owners: List<Owner>,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ListSearchField(state.query, { onChanged(state.copy(query = it)) }, stringResource(R.string.search_properties))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PropertyBooleanFilter(
                value = state.isActive,
                allLabel = stringResource(R.string.filter_property_status),
                trueLabel = stringResource(R.string.filter_active_properties),
                falseLabel = stringResource(R.string.filter_inactive_properties),
                onSelected = { onChanged(state.copy(isActive = it)) },
            )
            PropertyBooleanFilter(
                value = state.hasActiveContracts,
                allLabel = stringResource(R.string.filter_property_contracts),
                trueLabel = stringResource(R.string.filter_with_active_contracts),
                falseLabel = stringResource(R.string.filter_without_active_contracts),
                onSelected = { onChanged(state.copy(hasActiveContracts = it)) },
            )
            OwnerFilterChip(state.ownerId, owners) { onChanged(state.copy(ownerId = it)) }
            if (state.hasActiveFilters) {
                TextButton(onClick = { onChanged(PropertyListFilterState()) }) {
                    Text(stringResource(R.string.action_clear_all))
                }
            }
        }
    }
}

@Composable
private fun PropertyBooleanFilter(
    value: Boolean?,
    allLabel: String,
    trueLabel: String,
    falseLabel: String,
    onSelected: (Boolean?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = value != null,
            onClick = { expanded = true },
            label = { Text(when (value) { true -> trueLabel; false -> falseLabel; null -> allLabel }) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(null to allLabel, true to trueLabel, false to falseLabel).forEach { (option, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}
