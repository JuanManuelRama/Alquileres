package com.example.myapplication.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.creator.TenantCreator

@Composable
fun TenantPicker(
    tenants: List<Tenant>,
    selectedTenant: Tenant?,
    onTenantSelected: (Tenant) -> Unit,
    onTenantCreated: (Tenant) -> Unit,
    modifier: Modifier = Modifier,
) {
    EntityPicker(
        items = tenants,
        selectedItem = selectedTenant,
        onItemSelected = onTenantSelected,
        onItemCreated = onTenantCreated,

        itemLabel = { tenant ->
            tenant.name
        },

        label = "Tenant",
        placeholder = "Search tenant...",

        createContent = { onCreated, onCancel ->
            TenantCreator(
                onCreated = onCreated,
                cancelText = "Cancel",
                onCancel = onCancel,
            )
        },

        modifier = modifier,
    )
}