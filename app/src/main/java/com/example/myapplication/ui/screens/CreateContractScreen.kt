package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.creator.PropertyCreator
import com.example.myapplication.ui.creator.TenantCreator

@Composable
fun CreateContractScreen(
    tenants: List<Tenant>,
    properties: List<Property>
    //onCreateContract: (ContractDraft) -> Unit,
) {
    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "New rental contract",
            style = MaterialTheme.typography.headlineSmall
        )

        Form(
            submitText = "Create Contract",
            onSubmit = { },
            cancelText = "Back",
            onCancel = {  },
            modifier = Modifier
        ) {
            PickerItem(
                label = "Property",
                items = properties,
                selectedItem = selectedProperty,
                itemLabel = { it.toString() },
                createContent = { onCreated, onCanceled ->
                    PropertyCreator(
                        owners = listOf(Owner("pepito"), Owner("Jose")),
                        onCreated = onCreated,
                        canceledText = "Cancel",
                        onCanceled = onCanceled,
                    )
                },
                onItemSelected = { selectedProperty = it },
                onItemCreated = { selectedProperty = it}
            )

            PickerItem(
                label = "Tenant",
                items = tenants,
                selectedItem = selectedTenant,
                itemLabel = { it.name },
                createContent = { onCreated, onCanceled ->
                    TenantCreator(
                        onCreated = onCreated,
                        cancelText = "Cancel",
                        onCancel = onCanceled,
                    )
                },
                onItemSelected = { selectedTenant = it },
                onItemCreated = { selectedTenant = it }
            )
        }
    }
}